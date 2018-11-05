import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Downloader {
    public static void main(String[] args) {
        if ( args.length != 1 ) {
            System.err.println("Usage: java Downloader <url>");
            System.exit(1);
        }

        try {
            URL url = new URL(args[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String fileName = args[0].substring(args[0].lastIndexOf("/") + 1);

            if (connection.getHeaderField("Location") != null) {
                url = new URL(connection.getHeaderField("Location"));
            }

            connection.disconnect();

            InputStream in = url.openStream();
            Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
