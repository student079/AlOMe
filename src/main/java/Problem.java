import java.util.Objects;

public class Problem {
    private final String fileName;
    private final String commitDate;

    public Problem(String fileName, String commitDate) {
        this.fileName = fileName;
        this.commitDate = commitDate;
    }

    public String getFileName() {
        return fileName;
    }

    public String getCommitDate() {
        return commitDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Problem object = (Problem) o;
        return Objects.equals(fileName, object.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName);
    }
}
