import java.util.*;
import java.sql.*;
import java.lang.reflect.Constructor;

import static java.lang.String.join;
import static java.lang.String.format;
import static java.util.Collections.nCopies;

public abstract class Entity {
    private static String DELETE_QUERY      = "DELETE FROM \"%1$s\" WHERE %1$s_id=?";
    private static String INSERT_QUERY      = "INSERT INTO \"%1$s\" (%2$s) VALUES (%3$s) RETURNING %1$s_id";
    private static String LIST_QUERY        = "SELECT * FROM \"%1$s\" ORDER BY %1$s_id";
    private static String SELECT_QUERY      = "SELECT * FROM \"%1$s\" WHERE %1$s_id=?";
    private static String CHILDREN_QUERY    = "SELECT * FROM \"%1$s\" WHERE %2$s_id=?";
    private static String SIBLINGS_QUERY    = "SELECT * FROM \"%1$s\" NATURAL JOIN \"%2$s\" WHERE %3$s_id=?";
    private static String UPDATE_QUERY      = "UPDATE \"%1$s\" SET %2$s WHERE %1$s_id=?";

    private static Connection db = null;

    protected boolean isLoaded = false;
    protected boolean isModified = false;
    private String table = null;
    private int id = 0;
    protected Map<String, Object> fields = new HashMap<>();

    public Entity() {
        this.table = this.getClass().getSimpleName().toLowerCase();
    }

    public Entity(Integer id) {
        this();
        this.id = id;
    }

    public static final void setDatabase(Connection connection) {
        if ( connection == null ) {
            throw new NullPointerException();
        }

        db = connection;

        // throws NullPointerException
    }

    public final int getId() {
        return this.id;
    }

    public final java.util.Date getCreated() {
        return this.getDate("created");
    }

    public final java.util.Date getUpdated() {
        return this.getDate("updated");
    }

    public final Object getColumn(String name) {
        this.load();

        return this.fields.get(format("%s_%s", this.table, name));
        // return column name from fields by key
    }

    public final <T extends Entity> T getParent(Class<T> cls) {
        this.load();

        String className = cls.getSimpleName().toLowerCase();
        int id = (int) fields.get(format("%s_id", className));
        T parent = null;

        try {
            Constructor<T> constructor = cls.getConstructor(Integer.class);
            parent = constructor.newInstance(id);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return parent;
        // get parent id from fields as <classname>_id, create and return an instance of class T with that id
    }

    public final <T extends Entity> List<T> getChildren(Class<T> cls) {
        String childrenTable = cls.getSimpleName().toLowerCase();
        String query = format(CHILDREN_QUERY, childrenTable, this.table);

        return getObjectsList(cls, query, this.id);

        // select needed rows and ALL columns from corresponding table
        // convert each row from ResultSet to instance of class T with appropriate id
        // fill each of new instances with column data
        // return list of children instances
    }

    public final <T extends Entity> List<T> getSiblings(Class<T> cls) {
        String siblingsTable = cls.getSimpleName().toLowerCase();
        String joinTable = getJoinTableName(this.table, siblingsTable);
        String query = format(SIBLINGS_QUERY, siblingsTable, joinTable, this.table);

        return getObjectsList(cls, query, this.id);

        // select needed rows and ALL columns from corresponding table
        // convert each row from ResultSet to instance of class T with appropriate id
        // fill each of new instances with column data
        // return list of sibling instances
    }

    protected final void setColumn(String name, Object value) {
        this.fields.put(format("%s_%s", this.table, name), value);
        this.isModified = true;
        // put a value into fields with <table>_<name> as a key
    }

    public final void setParent(String name, Integer id) {
        this.fields.put(format("%s_id", name), id);
        this.isModified = true;
        // put parent id into fields with <name>_<id> as a key
    }

    private void load() {
        if ( !this.isLoaded ) {
            String query = format(SELECT_QUERY, this.table);
            ResultSet row;
            ResultSetMetaData meta;
            int size;

            try {
                PreparedStatement preparedStatement = db.prepareStatement(query);
                preparedStatement.setInt(1, this.id);
                row = preparedStatement.executeQuery();
                meta = row.getMetaData();
                size = meta.getColumnCount();

                if (row.next()) {
                    rowToFields(this, row, meta, size);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // check, if current object is already loaded
        // get a single row from corresponding table by id
        // store columns as object fields with unchanged column names as keys
    }

    private void insert() throws SQLException {
        int position = 1;
        String fieldNames = join(", ", this.fields.keySet());
        String placeholders = join(", ", nCopies(this.fields.size(), "?"));
        String query = format(INSERT_QUERY, this.table, fieldNames, placeholders);
        PreparedStatement ps = db.prepareStatement(query);

        for ( Object value: fields.values() ) {
            ps.setObject(position, value);
            position += 1;
        }

        ResultSet id = ps.executeQuery();
        if (id.next()) {
            this.id = id.getInt(1);
        }

        // execute an insert query, built from fields keys and values
    }

    private void update() throws SQLException {
        int position = 1;
        String placeholders = join(" = ?, ", fields.keySet()) + " = ?";
        String query = format(UPDATE_QUERY, this.table, placeholders);
        PreparedStatement ps = db.prepareStatement(query);

        for ( Object value: fields.values() ) {
            ps.setObject(position, value);
            position += 1;
        }

        ps.setInt(position, this.id);

        ps.executeUpdate();

        // execute an update query, built from fields keys and values
    }

    public final void delete() throws SQLException {
        if ( this.id == 0 ) {
            return;
        }
        String query = format(DELETE_QUERY, this.table);
        PreparedStatement ps = db.prepareStatement(query);
        ps.setInt(1, this.id);
        ps.executeUpdate();
        this.fields.clear();
        this.id = 0;
        this.isLoaded = false;
        this.isModified = false;

        // execute a delete query with current instance id
    }

    public final void save() throws SQLException {
        if ( !this.isModified ) {
            throw new RuntimeException("Row is not modified!");
        }
        if ( this.id == 0 ) {
            this.insert();
        } else {
            this.update();
        }
        // execute either insert or update query, depending on instance id
    }

    protected static <T extends Entity> List<T> all(Class<T> cls) {
        String query = format(LIST_QUERY, cls.getSimpleName().toLowerCase());

        return getObjectsList(cls, query, null);
        // select ALL rows and ALL columns from corresponding table
        // convert each row from ResultSet to instance of class T with appropriate id
        // fill each of new instances with column data
        // aggregate all new instances into a single List<T> and return it
    }

    private static String getJoinTableName(String leftTable, String rightTable) {
        Set<String> tables = new TreeSet<>(Arrays.asList(leftTable, rightTable));

        return format("%s__%s", tables.toArray());
        // generate the name of associative table for many-to-many relation
        // sort left and right tables alphabetically
        // return table name using format <table>__<table>
    }

    private static <T extends Entity> List<T> getObjectsList(Class<T> cls, String query, Integer id) {
        ResultSet rows = null;

        try {
            PreparedStatement ps = db.prepareStatement(query);

            if ( id != null ) {
                ps.setInt(1, id);
            }

            rows = ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsToEntities(cls, rows);
    }

    private java.util.Date getDate(String column) {
        return (java.util.Date) this.getColumn(column);
    }

    private static <T extends Entity> List<T> rowsToEntities(Class<T> cls, ResultSet rows) {
        List<T> instances = new ArrayList<>();

        try {
            ResultSetMetaData meta = rows.getMetaData();
            int size = meta.getColumnCount();

            while (rows.next()) {
                T instance = cls.newInstance();

                rowToFields(instance, rows, meta, size);

                instances.add(instance);
            }
        } catch ( SQLException | ReflectiveOperationException e ) {
            e.printStackTrace();
        }

        return instances;

        // convert a ResultSet of database rows to list of instances of corresponding class
        // each instance must be filled with its data so that it must not produce additional queries to database to get it's fields
    }

    private static void rowToFields (Entity obj, ResultSet rs, ResultSetMetaData meta, int size) {
        try {
            for (int i = 1; i <= size; i++) {
                String column = meta.getColumnName(i);
                Object value = rs.getObject(column);

                obj.fields.put(column, value);
            }

            obj.id = rs.getInt(1);
            obj.isLoaded = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
