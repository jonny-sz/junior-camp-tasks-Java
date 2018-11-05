import java.util.List;

public class GenMain {
    public static void main(String[] args) {
        Generator gen = new Generator();
        List<String> statements = gen.getStatements("test.yml");

        statements.forEach(System.out::println);
        gen.dump("statement.sql");
    }
}
