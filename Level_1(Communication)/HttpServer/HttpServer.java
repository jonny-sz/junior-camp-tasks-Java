import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpServer {
    private static int port;
    private static Pattern headerPattern;
    private static Properties httpConf = new Properties();
    private static Properties mimeTypes = new Properties();
    private static String address;

    public static void main(String[] args) {
        fileConfLoad("http.conf", httpConf);
        fileConfLoad("mime.types", mimeTypes);
        address = httpConf.getProperty("address");
        port = Integer.parseInt(httpConf.getProperty("port"));
        headerPattern = Pattern.compile("^[A-Z]+ /[\\s\\S]* HTTP/1.1\nHost: " + address + ":" + port + "[\\s\\S]*$");
        startServer();
    }

    private static void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("HttpServer " + address + " on port " + port + " started");

            while (true) {
                HttpServerThread thread = new HttpServerThread(serverSocket.accept());
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fileConfLoad(String fileName, Properties property) {
        try (BufferedReader fileInput = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))) {
            property.load(fileInput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class HttpServerThread extends Thread {
        private Socket clientSocket;
        BufferedReader in;
        OutputStream socketOutputStream;
        PrintStream out;

        HttpServerThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            String header = this.readRequestHeader();
            Matcher headerMatcher = headerPattern.matcher(header);

            if ( headerMatcher.matches() ) {
                String method = header.substring(0, header.indexOf(" "));
                String uri = this.getUri(header);
                String url = this.getUrlFromRequest(uri);
                int code = this.sendResponse(url, method);

                System.out.print(header);
                System.out.println(this.getResult(code, uri));

                try {
                    this.out.close();
                    this.socketOutputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            try {
                this.clientSocket.close();
                this.in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String readRequestHeader() {
            StringBuilder header = new StringBuilder();

            try {
                this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                String inputLine;

                while (true) {
                    inputLine = this.in.readLine();

                    if (inputLine == null || inputLine.isEmpty()) {
                        break;
                    }

                    header.append(inputLine).append(System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return header.toString();
        }

        private String getUri(String header) {
            int fistIndex = header.indexOf("/");
            int lastIndex = header.indexOf(" ", fistIndex);
            int paramIndex = header.indexOf("?");

            if (paramIndex != -1) {
                lastIndex = paramIndex;
            }

            return header.substring(fistIndex, lastIndex);
        }

        private String getUrlFromRequest(String uri) {
            if (uri.equals("/")) {
                uri += "index.html";
            }

            return httpConf.getProperty("root_dir") + uri;
        }

        private String getStatus(int code) {
            switch (code) {
                case HttpURLConnection.HTTP_OK:
                    return "200 OK";
                case HttpURLConnection.HTTP_BAD_METHOD:
                    return "405 Method Not Allowed";
                case HttpURLConnection.HTTP_NOT_FOUND:
                    return "404 Not Found";
                default:
                    return "Server Error";
            }
        }

        private String getResponseHeader(int code, String url) {
            StringBuilder header = new StringBuilder();
            String status = this.getStatus(code);
            SimpleDateFormat date = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss z");

            header.append("HTTP/1.1 ").append(status).append("\n");
            header.append("Connection: close\n");

            if (code == HttpURLConnection.HTTP_OK) {
                String type = mimeTypes.getProperty(url.substring(url.lastIndexOf('.') + 1));

                if (type == null) {
                    type = "application/octet-stream";
                }

                header.append("Content-Length: ").append(new File(url).length()).append("\n");
                header.append("Content-Type: ").append(type).append("\n");
            }

            date.setTimeZone(TimeZone.getTimeZone("GMT"));

            header.append("Date: ").append(date.format(new Date())).append("\n");
            header.append("\n");

            return header.toString();
        }

        private int sendResponse(String url, String method) {
            int code;
            String header;
            InputStream fileIn = null;

            if (!method.equals("GET")) {
                code = HttpURLConnection.HTTP_BAD_METHOD;
            } else {
                code = HttpURLConnection.HTTP_OK;
                try {
                    fileIn = new FileInputStream(url);
                } catch (FileNotFoundException | NullPointerException e) {
                    code = HttpURLConnection.HTTP_NOT_FOUND;
                }
            }

            header = getResponseHeader(code, url);

            try {
                this.socketOutputStream = this.clientSocket.getOutputStream();
                this.out = new PrintStream(this.socketOutputStream, true, "UTF-8");

                out.print(header);

                if (code == HttpURLConnection.HTTP_OK) {
                    int count;
                    byte[] buffer = new byte[4096];

                    while ((count = fileIn.read(buffer)) != -1) {
                        this.socketOutputStream.write(buffer, 0, count);
                        this.socketOutputStream.flush();
                    }
                    fileIn.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return code;
        }

        private String getResult(int code, String uri) {
            if (code == HttpURLConnection.HTTP_OK) {
                return String.format("%s %s", code, uri);
            }
            if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                return String.format("%s %s.notfound", code, uri);
            }
            return String.format("%s method not allowed", code);
        }
    }
}
