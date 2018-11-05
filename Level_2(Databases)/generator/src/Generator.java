import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.lang.String.format;
import static utils.Statements.*;

public class Generator {
    private Map tables;
    private List<String> allStatements;
    private List<String> tableStatements;
    private List<String> constraintStatements;
    private List<String> triggerStatements;

    public Generator() {}

    public List<String> getStatements(String fileName) {
        this.allStatements = new ArrayList<>();
        this.tableStatements = new ArrayList<>();
        this.constraintStatements = new ArrayList<>();
        this.triggerStatements = new ArrayList<>();

        this.readYaml(fileName);
        this.genTables();
        this.allStatements.addAll(this.tableStatements);
        this.allStatements.addAll(this.constraintStatements);
        this.allStatements.addAll(this.triggerStatements);

        return this.allStatements;
    }

    private void readYaml(String fileName) {
        try {
            YamlReader yamlReader = new YamlReader(new FileReader(fileName));
            this.tables = (Map) yamlReader.read();
            yamlReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void genTables() {
        for (Object table: this.tables.keySet()) {
            String tableName = ((String) table).toLowerCase();
            List<String> formatedFields = this.genFields(table, tableName);

            this.genTable(formatedFields, tableName);
            this.genRelations(table, tableName);
        }
    }
    
    private List<String> genFields(Object table, String tableName) {
        Map fields = (Map) ((Map) (this.tables.get(table))).get("fields");
        List<String> formatedFields = new ArrayList<>();

        fields.forEach((fieldName, fieldType) -> {
            formatedFields.add(format("\"%s_%s\" %s",
                    tableName, fieldName, ((String) fieldType).toUpperCase()));
        });

        return formatedFields;
    }

    private void genTable(List<String> formatedFields, String tableName) {
        String placeholders = String.join(",\n    ", Collections.nCopies(formatedFields.size(), "%s"));
        String template = format(CREATE_TABLE, tableName, placeholders);

        this.tableStatements.add(format(template, formatedFields.toArray()));
        this.triggerStatements.add(format(TRIGGER, tableName));
    }

    private void genRelations(Object table, String leftTableName) {
        Map leftRelations = (Map) ((Map) (this.tables.get(table))).get("relations");

        try {
            if ( leftRelations != null ) {
                leftRelations.forEach((tableObject, relationObject) -> {
                    Map rightRelations = (Map) ((Map) (this.tables.get(tableObject))).get("relations");
                    String leftRelation = (String) relationObject;
                    String rightRelation = (String) rightRelations.get(table);
                    String rightTableName = tableObject.toString().toLowerCase();

                    switch ( leftRelation ) {
                        case "one":
                            if ( "many".equals(rightRelation) ) {
                                this.genOneToMany(leftTableName, rightTableName);
                            }
                            break;
                        case "many":
                            if ( "many".equals(rightRelation) ) {
                                this.genManyToMany(leftTableName, rightTableName);
                                rightRelations.remove(table);
                            }
                            break;
                    }
                });
            }
        } catch (NullPointerException e) {
            throw new RuntimeException("Not found relation!");
        }
    }

    private void genOneToMany(String leftTable, String rightTable) {
        String statement = format(ADD_FIELD_CONSTRAINT, leftTable, rightTable);
        this.constraintStatements.add(statement);
    }

    private void genManyToMany(String leftTable, String rightTable) {
        Set<String> args = new TreeSet<>(Arrays.asList(leftTable, rightTable));
        Object[] params = args.toArray();
        String joinTableName = format("%s__%s", params);
        String statement;

        statement = format(CREATE_TABLE_MM, params);
        this.tableStatements.add(statement);
        statement = format(ADD_CONSTRAINT, joinTableName, params[0]);
        this.constraintStatements.add(statement);
        statement = format(ADD_CONSTRAINT, joinTableName, params[1]);
        this.constraintStatements.add(statement);
    }

    public void dump(String fileName) {
        try ( BufferedWriter out = new BufferedWriter(new FileWriter(fileName)) ) {
            for ( String st: this.allStatements ) {
                out.write(st);
                out.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
