import java.util.Date;

/**
 * @ClassName: CommitShortInfo
 * @Description: TODO
 * @Author panpan
 */
public class CommitShortInfo {
    String sha;
    String message;
    Date date;

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
