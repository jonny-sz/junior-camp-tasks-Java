import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class WazzupClient {
    public static void main(String[] args) {
        String hostName;
        int port;

        if ( args.length != 2 ) {
            System.err.println("Usage: java WazzupClient <host name> <port number>");
            System.exit(1);
        }

        hostName = args[0];
        port = Integer.parseInt(args[1]);

        try (
            Socket socket = new Socket(hostName, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        )   {
                BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
                String fromServer;
                String userInput;

                System.out.println("Connected to " + hostName);

                while ( (userInput = systemIn.readLine()) != null ) {
                    out.println(userInput);
                    fromServer = in.readLine();

                    if ( fromServer.equals("disconnect") ) {
                        break;
                    }
                    System.out.println(fromServer);
                }
        } catch (UnknownHostException e) {
            System.err.println("Incorrect host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
