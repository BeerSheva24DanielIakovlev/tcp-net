package telran.net;

import java.net.*;
import java.io.*;

public class TcpServer implements Runnable {
    private final Protocol protocol;
    private final int port;
    private volatile boolean isShuttingDown = false;

    public TcpServer(Protocol protocol, int port) {
        this.protocol = protocol;
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (!isShuttingDown) {
                try {
                    serverSocket.setSoTimeout(1000);
                    Socket socket = serverSocket.accept();

                    if (isShuttingDown) {
                        socket.close();
                        break;
                    }

                    Thread thread = new Thread(new TcpClientServerSession(protocol, socket));
                    thread.start();

                } catch (SocketTimeoutException e) {
                    
                }
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }

        System.out.println("Server has shut down.");
    }

    public void shutdown() {
        isShuttingDown = true;
        System.out.println("Shutdown initiated.");
    }
}
