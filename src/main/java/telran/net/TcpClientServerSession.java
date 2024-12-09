package telran.net;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.json.JSONObject;

public class TcpClientServerSession implements Runnable {
    private final Protocol protocol;
    private final Socket socket;
    private final DosProtection dosProtection = new DosProtection();

    public TcpClientServerSession(Protocol protocol, Socket socket) {
        this.protocol = protocol;
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             PrintStream writer = new PrintStream(socket.getOutputStream(), true, "UTF-8")) {

            socket.setSoTimeout(TcpConfigurationProperties.SOCKET_INACTIVITY_TIMEOUT);

            String request;
            while (true) {
                try {
                    request = reader.readLine();
                    if (request == null) break; 

                    if (dosProtection.isRateLimitExceeded()) {
                        System.out.println("Rate limit exceeded for client: " + socket.getRemoteSocketAddress());
                        break;
                    }

                    String responseJSON = protocol.getResponseWithJSON(request);
                    writer.println(responseJSON);

                    Response response = parseResponse(responseJSON);
                    if (dosProtection.isNotOkLimitExceeded(response.responseCode())) {
                        System.out.println("Too many not OK responses for client: " + socket.getRemoteSocketAddress());
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Socket timeout for client: " + socket.getRemoteSocketAddress());
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            closeSocket();
        }
    }

    private Response parseResponse(String responseJSON) {
        JSONObject jsonObj = new JSONObject(responseJSON);
        ResponseCode responseCode = ResponseCode.valueOf(jsonObj.getString(TcpConfigurationProperties.RESPONSE_CODE_FIELD));
        String responseData = jsonObj.getString(TcpConfigurationProperties.RESPONSE_DATA_FIELD);
        return new Response(responseCode, responseData);
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.getMessage());
        }
    }
}
