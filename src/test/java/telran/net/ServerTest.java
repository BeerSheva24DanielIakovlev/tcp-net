package telran.net;

public class ServerTest {

    public static void main(String[] args) throws InterruptedException {
        // Создание и запуск сервера
        TcpServer server = new TcpServer(new TestProtocol(), 5000);
        Thread serverThread = new Thread(server);
        serverThread.start();

        // Ожидание запуска сервера
        Thread.sleep(1000);

        // Тестирование защиты от DoS атак (лимит запросов в секунду)
        testRateLimit();

        // Тестирование защиты от DoS атак (лимит неудачных ответов)
        testNotOkLimit();

        // Тестирование тайм-аута неактивных соединений
        testInactivityTimeout();

        // Тестирование корректной обработки запросов
        testRequestProcessing();

        // Тестирование Graceful Shutdown
        testGracefulShutdown(server);

        // Завершение работы сервера
        server.shutdown();
        serverThread.join();        
    }

    private static void testRateLimit() {
        System.out.println("Testing Rate Limit...");
        TcpClient client = new TcpClient("localhost", 5000);

        for (int i = 0; i < 10; i++) {
            try {
                client.sendAndReceive("TEST", "Request " + i);
                Thread.sleep(100); // Лимит 10 запросов в секунду (отправляем быстрее)
            } catch (Exception e) {
                System.out.println("Connection closed due to rate limit: " + e.getMessage());
                break;
            }
        }
    }

    private static void testNotOkLimit() {
        System.out.println("Testing Not-OK Limit...");
        TcpClient client = new TcpClient("localhost", 5000);

        for (int i = 0; i < 5; i++) {
            try {
                client.sendAndReceive("INVALID", "Invalid data");
            } catch (Exception e) {
                System.out.println("Error response received: " + e.getMessage());
            }
        }
    }

    private static void testInactivityTimeout() throws InterruptedException {
        System.out.println("Testing Inactivity Timeout...");
        TcpClient client = new TcpClient("localhost", 5000);
        Thread.sleep(6000); // Лимит времени бездействия 5 секунд

        try {
            client.sendAndReceive("TEST", "Test after timeout");
        } catch (Exception e) {
            System.out.println("Connection closed due to inactivity: " + e.getMessage());
        }
    }

    private static void testRequestProcessing() {
        System.out.println("Testing Request Processing...");
        TcpClient client = new TcpClient("localhost", 5000);

        String response = client.sendAndReceive("VALID", "Valid data");
        System.out.println("Response: " + response);
    }

    private static void testGracefulShutdown(TcpServer server) {
        System.out.println("Testing Graceful Shutdown...");
        TcpClient client = new TcpClient("localhost", 5000);

        // Отправка запроса
        String response = client.sendAndReceive("TEST", "Long running request");
        System.out.println("Response before shutdown: " + response);

        // Завершение работы сервера
        server.shutdown();

        try {
            TcpClient newClient = new TcpClient("localhost", 5000);
            newClient.sendAndReceive("TEST", "Test request");
        } catch (Exception e) {
            System.out.println("Server correctly rejected new connections after shutdown.");
        }
    }
}