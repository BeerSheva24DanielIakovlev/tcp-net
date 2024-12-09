package telran.net;
import java.net.*;
import java.io.*;
public class TcpClientServerSession implements Runnable{
    Protocol protocol;
    Socket socket;
    //TODO new fields 
    public TcpClientServerSession(Protocol protocol, Socket socket) {
        this.protocol = protocol;
        this.socket = socket;
    }
    @Override
    public void run() {
        //FIXME add SocketTimeoutException handler for both graceful shutdown and DoS attacks prevention
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintStream writer = new PrintStream(socket.getOutputStream())) {
            String request = null;
            while((request = reader.readLine()) != null) {
                String response = protocol.getResponseWithJSON(request);
                writer.println(response);
            }
            socket.close();
        } catch (Exception e) {
           System.out.println(e);
        }
    }

}