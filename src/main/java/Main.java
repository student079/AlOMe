import com.github.scribejava.core.model.OAuth2AccessToken;
import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ExecutionException;

public class Main extends JFrame{
    public static void main(String[] args) throws IOException {
        JFrame starter = new JFrame("Problem Searcher");
        starter.setSize(500,300);
        starter.setResizable(false);
        starter.setLocationRelativeTo(null);
        starter.setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        JButton startBtn = new JButton("GitHub Login");
        mainPanel.add(startBtn);
        starter.add(mainPanel);

        starter.setVisible(true);

        //Oauth2.0 생성
        OAuthService oAuthService = OAuthService.getInstance();

        //응답을 http서버로 받기
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        OAuthHandler oAuthHandler = new OAuthHandler();
        httpServer.createContext("/login/oauth2/code/github", oAuthHandler);
        httpServer.setExecutor(null);
        httpServer.start();

        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    Desktop.getDesktop().browse(new URI(oAuthService.getService().getAuthorizationUrl()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "로그인 과정에서 문제가 발생하였습니다.");
                    ex.printStackTrace();
                }
                // accessCode 설정
                String accessCode = null;
                while (accessCode == null) {
                    accessCode = oAuthHandler.getAccessCode();
                    try {
                        Thread.sleep(2000); // 2초 대기
                    } catch (InterruptedException e) {
                        JOptionPane.showMessageDialog(null, "로그인 과정에서 문제가 발생하였습니다.");
                        e.printStackTrace();
                    }
                }

                try {
                    new MainFrame(accessCode, oAuthService);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "로그인 과정에서 문제가 발생하였습니다.");
                    throw new RuntimeException(e);
                }
                starter.dispose();
            }
        });
    }
}
