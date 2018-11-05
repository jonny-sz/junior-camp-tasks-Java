import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

class EchoServer {
    private int port;
    private ServerSocket serverSocket;

    private static final int DEFAULT_PORT = 1500;

    public EchoServer() {
        this(DEFAULT_PORT);
    }

    public EchoServer(int port) {
        this.port = port;
        this.serverSocket = null;
    }

    public boolean isRunning() {
        return this.serverSocket != null && !(this.serverSocket.isClosed());
    }

    public int getPort() {
        return this.port;
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            System.out.println("Server " + this.serverSocket.getInetAddress() + " on port " + this.port + " started");
            System.out.println("Waiting for clients...");

            while (true) {
                EchoServerThread thread = new EchoServerThread(this.serverSocket.accept());
                System.out.println("Client " + thread.getClientSocket().getInetAddress() + " accepted");
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + this.port);
            System.exit(-1);
        }
    }

    public void stop() throws IOException {
        this.serverSocket.close();
    }

    private static class EchoServerThread extends Thread {
        private Socket clientSocket;

        EchoServerThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public Socket getClientSocket() {
            return this.clientSocket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            )   {
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {

                        System.out.println(inputLine);
                        out.println(inputLine);

                        if ( inputLine.equals("disconnect") ) {
                            System.out.println("Client " + this.clientSocket.getInetAddress() + " disconnected");
                            break;
                        }
                }
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
