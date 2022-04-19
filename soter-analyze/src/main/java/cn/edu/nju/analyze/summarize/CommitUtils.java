import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName: CommitUtils
 * @Description: TODO
 * @Author panpan
 */
public class CommitUtils {
    public static String token = "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";

    // 1.获取指定两个版本间的所有commits,默认按时间降序
    public static List<CommitShortInfo> getCommitsListBetweenTags(String userName, String projectName, String since, String util) throws IOException, ParseException {
        TaggedCommit[] list = DownloadCodeBySha.getTagList(userName, projectName);
        HashMap<String, String> nameToSha = new HashMap<>();
        for (TaggedCommit tc : list) {
            nameToSha.put(tc.name, tc.getCommit().sha);
        }
        String sinceSha = nameToSha.get(since);
        String utilSha = nameToSha.get(util);

        Date sinceDate = CommitUtils.getTagDateBySha(userName, projectName, sinceSha);
        Date utilDate = CommitUtils.getTagDateBySha(userName, projectName, utilSha);

        List<CommitShortInfo> res = new LinkedList<>();
        GitHub github = GitHub.connectUsingOAuth(token);
        List<GHCommit> commits = github.getUser(userName)
                .getRepository(projectName)
                .queryCommits()
                .since(sinceDate)
                .until(utilDate)
                .list()
                .toList();

        for (GHCommit commit : commits) {
            CommitShortInfo commitShortInfo = new CommitShortInfo();
            commitShortInfo.setSha(commit.getSHA1());
            commitShortInfo.setDate(commit.getCommitDate());
            commitShortInfo.setMessage(commit.getCommitShortInfo().getMessage());
            res.add(commitShortInfo);
        }
        return res;
    }

    // 对commit列表进行分类，get(0)为有message，get(1)为无message
    public static List<List<CommitShortInfo>> divideByMessage(List<CommitShortInfo> list) {
        List<CommitShortInfo> withMessage = new LinkedList<>();
        List<CommitShortInfo> withoutMessage = new LinkedList<>();
        for (CommitShortInfo csi : list) {
            if (!csi.getMessage().isEmpty()) {
                withMessage.add(csi);
            } else {
                withoutMessage.add(csi);
            }
        }
        List<List<CommitShortInfo>> res = new ArrayList<>();
        res.add(withMessage);
        res.add(withoutMessage);
        return res;
    }

    // 按时间倒序
    public static List<CommitShortInfo> reverse(List<CommitShortInfo> list) {
        List<CommitShortInfo> res = new LinkedList<>(list);
        Collections.reverse(res);
        return res;
    }

    // 2.获取最后一次版本前三个月的commits,默认按时间降序
    public static List<CommitShortInfo> getRecentCommitsList(String userName, String projectName) throws IOException, ParseException {
        TaggedCommit[] list = DownloadCodeBySha.getTagList(userName, projectName);
        String latestSha = list[0].getCommit().getSha();
        Date utilDate = CommitUtils.getTagDateBySha(userName, projectName, latestSha);
        Calendar cal = Calendar.getInstance();
        cal.setTime(utilDate);
        cal.add(Calendar.MONTH, -3);
        Date sinceDate = cal.getTime();

        List<CommitShortInfo> res = new LinkedList<>();
        GitHub github = GitHub.connectUsingOAuth(token);
        List<GHCommit> commits = github.getUser(userName)
                .getRepository(projectName)
                .queryCommits()
                .since(sinceDate)
                .until(utilDate)
                .list()
                .toList();

        for (GHCommit commit : commits) {
            CommitShortInfo commitShortInfo = new CommitShortInfo();
            commitShortInfo.setSha(commit.getSHA1());
            commitShortInfo.setDate(commit.getCommitDate());
            commitShortInfo.setMessage(commit.getCommitShortInfo().getMessage());
            res.add(commitShortInfo);
        }
        return res;
    }

    // 3.获取指定版本及其前一个版本的代码
    public static List<String> recentCommitsPath(String username, String projectName, String curSha) throws GitAPIException, IOException {
        String localPath = "D:\\MyProject\\" + projectName;
        String githubPath = "https://github.com/" + username + "/" + projectName + ".git";

        System.out.println(localPath);
        System.out.println(githubPath);
        String basePath = localPath + "\\base";
        File file = new File(basePath);
        if (!file.exists()) {
            file.mkdir();
        }

        System.out.println("下载开始······");

        CredentialsProvider provider = new UsernamePasswordCredentialsProvider("zztu", "QianPeng2");
        Git git = Git.cloneRepository()
                .setCredentialsProvider(provider)
                .setURI(githubPath)
                .setDirectory(new File(basePath))
                .setCloneAllBranches(true)
                .call();

        System.out.println("下载完成······");

        List<String> resPathList = new LinkedList<>();

        String curPath = localPath + "\\" + curSha;
        File curFile = new File(curPath);
        if (!curFile.exists()) {
            curFile.mkdir();
        }
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef(curSha).call();
        FileUtils.copyDirectory(new File(basePath), new File(curPath));
        resPathList.add(curPath);

        String beforePath = localPath + "\\" + "before" + curSha;
        File beforeFile = new File(beforePath);
        if (!beforeFile.exists()) {
            beforeFile.mkdir();
        }
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef(curSha).call();
        FileUtils.copyDirectory(new File(basePath), new File(beforePath));
        resPathList.add(curPath);

        return resPathList;
    }

    // 获取指定commit的提交时间
    public static Date getTagDateBySha(String userName, String projectName, String sha) throws IOException, ParseException {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.github.com/repos/" + userName + "/" + projectName + "/commits/" + sha;
        Request getTagRequest = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Cookie", "_octo=GH1.1.1298099498.1646817014; logged_in=no")
                .addHeader("Authorization", "token " + token)
                .build();
        Response getTagDateResponse = client.newCall(getTagRequest).execute();
        JSONObject commitObject = JSONObject.parseObject(getTagDateResponse.body().string());
        String commitJson = commitObject.getString("commit");
        JSONObject committerObject = JSONObject.parseObject(commitJson);
        String committerJson = committerObject.getString("committer");
        JSONObject dateObject = JSONObject.parseObject(committerJson);
        String dateJson = dateObject.getString("date");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = df.parse(dateJson);
        return date;
    }

    public static void main(String[] args) throws IOException, ParseException {
        // CommitUtils.getCommitsListBetweenTags("docmirror", "dev-sidecar", "v1.7.2", "v1.7.3");
        // System.out.println(getTagDate("docmirror", "dev-sidecar", "ade3470ed07475a6c1707f7fa3b2b5b44ccefbfd").toString());
        CommitUtils.getRecentCommitsList("docmirror", "dev-sidecar");
    }
}
