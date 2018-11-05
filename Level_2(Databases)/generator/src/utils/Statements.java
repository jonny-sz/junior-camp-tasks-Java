package utils;

public final class Statements {
    public static final String TRIGGER =
             "CREATE FUNCTION update_%1$s_timestamp()\n" +
             "RETURNS TRIGGER AS $$\n" +
             "    BEGIN\n" +
             "        NEW.%1$s_updated = now();\n" +
             "        RETURN NEW;\n" +
             "    END;\n" +
             "$$ LANGUAGE 'plpgsql';\n" +
             "CREATE TRIGGER \"tr_%1$s_updated\"\n" +
             "BEFORE UPDATE ON \"%1$s\"\n" +
             "FOR EACH ROW\n" +
             "EXECUTE PROCEDURE update_%1$s_timestamp();\n";

    public static final String CREATE_TABLE =
            "CREATE TABLE \"%1$s\" (\n" +
            "    \"%1$s_id\" SERIAL PRIMARY KEY,\n" +
            "    %2$s,\n" +
            "    \"%1$s_created\" TIMESTAMP(0) NOT NULL DEFAULT now(),\n" +
            "    \"%1$s_updated\" TIMESTAMP(0) NOT NULL DEFAULT now()\n" +
            ");\n";

    public static final String CREATE_TABLE_MM =
            "CREATE TABLE \"%1$s__%2$s\" (\n" +
            "    \"%1$s_id\" INTEGER NOT NULL,\n" +
            "    \"%2$s_id\" INTEGER NOT NULL,\n" +
            "    PRIMARY KEY (\"%1$s_id\", \"%2$s_id\")\n" +
            ");\n";

    public static final String ADD_FIELD_CONSTRAINT =
            "ALTER TABLE \"%1$s\"\n" +
            "ADD \"%2$s_id\" INTEGER NOT NULL,\n" +
            "ADD CONSTRAINT \"fk_%1$s_%2$s_id\"\n" +
            "FOREIGN KEY (\"%2$s_id\")\n" +
            "REFERENCES \"%2$s\" (\"%2$s_id\");\n";

    public static final String ADD_CONSTRAINT =
            "ALTER TABLE \"%1$s\"\n" +
            "ADD CONSTRAINT \"fk_%1$s_%2$s_id\"\n" +
            "FOREIGN KEY (\"%2$s_id\")\n" +
            "REFERENCES \"%2$s\" (\"%2$s_id\");\n";
}
