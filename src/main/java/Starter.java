import org.kohsuke.github.*;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class Starter extends JFrame {
    static GHRepository ghRepository;
    static ArrayList<Problem> SelectedFiles = new ArrayList<>();
    static String userId;

    public Starter (String repoName, GitHub github) throws IOException {


        try {
            userId = github.getMyself().getLogin();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "로그인 과정에서 문제가 발생하였습니다.");
            e.printStackTrace();
        }

        JFrame starterFrame = new JFrame("실행 중");
        starterFrame.setSize(500, 300);
        starterFrame.setResizable(false);
        starterFrame.setLocationRelativeTo(null);
        starterFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        starterFrame.setVisible(true);

        makeTxt(repoName, github);

    }

    private static void makeTxt(String repoName, GitHub github) throws IOException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // 해당 레포의 이전 기록이 있는지 체크
        File dic = new File("./");
        File[] files = dic.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.contains(repoName);
            }
        });
        if (files != null && files.length != 0) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File t1, File t2) {
                    Date date1 = null;
                    Date date2 = null;
                    try {
                        date1 = sdf.parse(t1.getName().split("_")[2]);
                    } catch (ParseException e) {
                        date1 = new Date(0);
                    }
                    try {
                        date2 = sdf.parse(t2.getName().split("_")[2]);
                    } catch (ParseException e) {
                        date2 = new Date(0);
                    }
                    return date2.compareTo(date1);
                }
            });

            File selectedFile = files[0];
            ghRepository = github.getRepository(userId + "/" + repoName);
            // 파일 이름의 날짜 전날부터 확인
            String fileName = selectedFile.getName();
            String[] sDate = fileName.substring(0, fileName.length() - 4 ).split("_")[2].split("-");
            LocalDate date = LocalDate.of(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]), Integer.parseInt(sDate[2]));
            date = date.minusDays(1);
            Date since = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            PagedIterable<GHCommit> commits = ghRepository.queryCommits().since(since).list();

            // 최근 커밋부터
            Set<String> visitedFile = new HashSet<>();
            Set<String> visitedDic = new HashSet<>();
            for (GHCommit commit : commits) {
                List<GHCommit.File> contents = commit.listFiles().toList();
                for (GHCommit.File f : contents) {
                    // visited 확인
                    System.out.println(f.getFileName());
                    String d = "";
                    try {
                        d = f.getFileName().substring(0, f.getFileName().lastIndexOf("/"));
                    } catch (StringIndexOutOfBoundsException e) {
                        continue;
                    }

                    // 폴더 검사 했는지
                    if (!visitedDic.contains(d)) {
                        visitedDic.add(d);
                        searchProblems(d);
                    }
                    else {
                        visitedFile.add(f.getFileName().substring(f.getFileName().lastIndexOf("/") + 1));
                    }
                }
            }

            // 선택된 최신 파일에 문제들 읽기
            try {
                // 파일 경로를 Path 객체로 변환
                Path filePath = selectedFile.toPath();

                // 전체 파일 읽기
                List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

                for (String line : lines) {
                    String[] pros = line.split("\t");

                    if (!visitedFile.contains(pros[0])) {
                        SelectedFiles.add(new Problem(pros[0], pros[1]));
                    }
                }

            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "기존 파일읽기에 실패하였습니다.");
            }

            // txt파일로 반환
            // 날짜순 정렬
            SelectedFiles.sort(Comparator.comparing(Problem::getCommitDate));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(repoName + "_problems_" + sdf.format(new Date()) + ".txt"))) {
                for (Problem problem : SelectedFiles) {
                    String format = problem.getFileName() + "\t" + problem.getCommitDate();
                    writer.write(format);
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "txt파일 저장에 실패했습니다.");
            }
            JOptionPane.showMessageDialog(null, repoName+"_problems_" + sdf.format(new Date()) + ".txt" + "성공적으로 저장했습니다.");
            // 종료
            System.exit(0);

        }
        // 파일 없을 경우
        else {
            try {
                ghRepository = github.getRepository(userId + "/" + repoName);
                // 레포지토리 파일 중 첫줄에 특정 단어를 포함하는 문제들 반환
                searchProblems("");

            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "해당 레포지토리가 존재하지 않습니다.");
            }

            // txt파일로 반환
            // 날짜순 정렬
            SelectedFiles.sort(Comparator.comparing(Problem::getCommitDate));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(repoName + "_problems_" + sdf.format(new Date()) + ".txt"))) {
                for (Problem problem : SelectedFiles) {
                    String format = problem.getFileName() + "\t" + problem.getCommitDate();
                    writer.write(format);
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "txt파일 저장에 실패했습니다.");
            }
            JOptionPane.showMessageDialog(null, repoName+"_problems_date.txt" + "성공적으로 저장했습니다.");
            // 종료
            System.exit(0);
        }
    }

    private static void searchProblems(String path) {
        try {
            List<GHContent> contents = ghRepository.getDirectoryContent(path);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (GHContent content : contents) {
                // 디렉토리와 파일 구별하여 로직 진행
                //Directory
                if (content.isDirectory()) {
                    searchProblems(content.getPath() );
                }
                // file
                else {
                    // 첫줄에 "다시" 있으면 이름 저장
                    try (InputStream inputStream = content.read();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                        String firstLine = reader.readLine(); // 첫 줄 읽기
                        if (firstLine != null && firstLine.contains("다시")) {

                            // 특정 파일의 가장 최근 커밋 가져오기
                            PagedIterable<GHCommit> commits = ghRepository.queryCommits().path(content.getPath()).list();
                            if (commits.iterator().hasNext()) {
                                GHCommit latestCommit = commits.iterator().next();
                                SelectedFiles.add(new Problem(content.getName(), sdf.format(latestCommit.getCommitDate())));
                            }
                        }
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "파일 기록을 가져오는 과정에서 오류가 발생하였습니다.");
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "파일 기록을 가져오는 과정에서 오류가 발생하였습니다.");
            throw new RuntimeException(e);
        }
    }
}
