package telran.net;
import java.net.*;
public class TcpServer implements Runnable{
Protocol protocol;
int port;
public TcpServer(Protocol protocol, int port) {
    this.protocol = protocol;
    this.port = port;
}
    @Override
    public void run() {
        //FIXME add SocketTimeOut handling for shutdown 
       try (ServerSocket serverSocket = new ServerSocket(port)) {
         System.out.println("Server is listening on the port "+ port);
            while(true) {
                Socket socket = serverSocket.accept();
                var session = new TcpClientServerSession(protocol, socket);
                Thread thread = new Thread(session);

                thread.start();
            }
       } catch (Exception e) {
        System.out.println(e);
       }
    }
    public void shutdown() {
        //TODO
        //In the ExecutorService framework to provide shutdownNow (to ignore all not processing client sessions)
    }

}