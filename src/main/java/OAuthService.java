import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class OAuthService {
    // 싱글톤 패턴
    private static OAuthService instance;
    private OAuth20Service service;

    private OAuthService() throws IOException {
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream("config.properties");
        properties.load(fis);

        this.service = new ServiceBuilder(properties.getProperty("clientId"))
                .apiSecret(properties.getProperty("clientSecret"))
                .callback(properties.getProperty("callbackUrl"))
                .defaultScope("repo")
                .build(GitHubApi.instance());
    }

    public static OAuthService getInstance() throws IOException {
        if (instance == null) {
            synchronized (OAuthService.class) {
                if (instance == null) {
                    instance = new OAuthService();
                }
            }
        }
        return instance;
    }

    public OAuth20Service getService() {
        return service;
    }
}
