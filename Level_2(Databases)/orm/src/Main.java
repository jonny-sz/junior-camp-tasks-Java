import java.sql.*;

public class Main {
    private static Connection connection = null;

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        initDatabase();
        Entity.setDatabase(connection);

//        Section all = createSection("all");
//
//        Category phone = createCategory("phone", all);
//        Category tv = createCategory("tv", all);
//
//        String iphoneContent = "I am not sure iphone is the best phone in the world!";
//        Post iphone = createPost("iphone", iphoneContent, phone);
//
//        String bbContent = "Passport is the best phone in the world!";
//        Post blackberry = createPost("blackberry", bbContent, phone);
//
//        String samsungContent = "Samsung is the best TV in the world!";
//        Post samsung = createPost("samsung", samsungContent, tv);
//
//        String orionContent = "I doubt Orion the best TV in the world!";
//        Post orion = createPost("orion", orionContent, tv);
//
//        Tag best = createTag("best");
//        Tag worst = createTag("worst");

//        for ( Post category: Post.all() ) {
//            System.out.println(category.getId() + ": " + category.getContent());
//        }
//
//        for ( Post post : Post.all() ) {
//            System.out.println(post.getId() + ": " + post.getTitle());
//
//            for ( Tag tag : post.getTags() ) {
//                System.out.println("  " + tag.getName());
//
//                for ( Post p : tag.getPosts() ) {
//                    System.out.println("    " + p.getId() + ": " + p.getTitle());
//                }
//            }
//        }
    }

    private static void initDatabase() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");

        connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost/test", "admin", "admin");
    }

    private static Section createSection(String name) throws SQLException {
        Section section = new Section();
        section.setTitle(name);
        section.save();

        return section;
    }

    private static Category createCategory(String title, Section section) throws SQLException {
        Category category = new Category();
        category.setTitle(title);
        category.setSection(section);
        category.save();

        return category;
    }

    private static Post createPost(String title, String content, Category category) throws SQLException {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setCategory(category);
        post.save();

        return post;
    }

    private static Tag createTag(String name) throws SQLException {
        Tag tag = new Tag();
        tag.setName(name);
        tag.save();

        return tag;
    }
}
