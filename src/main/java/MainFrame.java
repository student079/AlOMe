import com.github.scribejava.core.model.OAuth2AccessToken;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainFrame extends JFrame{
    public MainFrame(String accessCode, OAuthService oAuthService) throws IOException {
        JFrame mainFrame = new JFrame("Problem Searcher");
        mainFrame.setSize(500,300);
        mainFrame.setResizable(false);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // 레포지토리 더블클릭하면 그 레포지토리로 실행
        // accessToken 설정
        OAuth2AccessToken accessToken;
        try {
            accessToken = oAuthService.getService().getAccessToken(accessCode);
        } catch (InterruptedException | ExecutionException | IOException e) {
            JOptionPane.showMessageDialog(null, "accessToken을 받아오는데 문제가 발생하였습니다.");
            throw new RuntimeException(e);
        }

        // 레포지토리 목록 반환
        GitHub github = new GitHubBuilder().withOAuthToken(accessToken.getAccessToken()).build();
        ArrayList<String> repoNames = new ArrayList<>();
        for (GHRepository repo : github.getMyself().listRepositories()) {
            repoNames.add(repo.getName());
        }
        JList<String> jList = new JList<>(repoNames.toArray(new String[0]));
        JScrollPane jScrollPane = new JScrollPane(jList);
        JLabel jLabel = new JLabel("레포지토리를 선택하세요");
        jList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            new Starter(jList.getSelectedValue(), github);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                    mainFrame.dispose();
                }
            }
        });

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(jLabel, BorderLayout.NORTH);
        mainFrame.add(jScrollPane, BorderLayout.CENTER);
        mainFrame.setVisible(true);
    }
}
