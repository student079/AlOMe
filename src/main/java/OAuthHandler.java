import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OAuthHandler implements HttpHandler {
    private String accessCode;

    public String getAccessCode() {
        return accessCode;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String query = exchange.getRequestURI().getQuery();
        Pattern pattern = Pattern.compile("code=([^&]+)");
        Matcher matcher = pattern.matcher(query);

        if (matcher.find()) {
            this.accessCode = matcher.group(1);

            String response = "<html><body><h2>Access Code Received Successfully</h2>"
                    + "<p>You can close this window and return to your application.</p></body></html>";

            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            String response = "<html><body><h2>Access Code NOT Received Successfully</h2>"
                    + "<p>I recommend you try again.</p></body></html>";

            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

    }
}
