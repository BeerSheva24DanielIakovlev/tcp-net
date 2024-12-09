package telran.net;

public class TestProtocol implements Protocol {
    @Override
    public Response getResponse(Request request) {
        // Простая логика для тестирования:
        if ("test".equalsIgnoreCase(request.requestType())) {
            return new Response(ResponseCode.OK, "Test response for: " + request.requestData());
        } else {
            return new Response(ResponseCode.WRONG_TYPE, "Unsupported request type: " + request.requestType());
        }
    }
}
