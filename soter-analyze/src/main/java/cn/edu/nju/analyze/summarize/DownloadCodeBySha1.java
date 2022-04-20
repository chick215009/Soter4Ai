package cn.edu.nju.analyze.summarize;

import cn.edu.nju.analyze.summarize.TaggedCommit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GitHub;
import com.squareup.okhttp.*;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @ClassName: DownloadCodeBySha1
 * @Description: TODO
 * @Author panpan
 */
public class DownloadCodeBySha1 {
    public static List<String> getShaList(String userName, String projectName) throws IOException {
        List<String> res = new LinkedList<>();
        GitHub github = GitHub.connectAnonymously();
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -12);
        List<GHCommit> commits = github.getUser(userName)
                .getRepository(projectName)
                .queryCommits()
                .since(calendar.getTime())
                .until(new Date())
                .list()
                .toList();
        for (GHCommit commit : commits) {
            res.add(commit.getSHA1());
        }
        return res;
    }

    public static TaggedCommit[] getTagList(String userName, String projectName) {
        String token = "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";
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.github.com/repos/" + userName + "/" + projectName + "/tags";
        Request getTagRequest = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Cookie", "_octo=GH1.1.1298099498.1646817014; logged_in=no")
                .addHeader("Authorization", "token " + token)
                .build();
        try {
            Response getTagResponse = client.newCall(getTagRequest).execute();
            ObjectMapper mapper = new ObjectMapper();
            TaggedCommit[] tcs = mapper.readValue(getTagResponse.body().string(), TaggedCommit[].class);
            return tcs;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> historyProjectPath(String username, String projectName) throws GitAPIException, IOException {
        String localPath = "/Users/chengleming/MasterThesis/Soter/tmp/" + projectName;
        String githubPath = "https://github.com/" + username + "/" + projectName + ".git";

        System.out.println(localPath);
        System.out.println(githubPath);
        String basePath = localPath + "/base";
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

        TaggedCommit[] taggedCommits = DownloadCodeBySha1.getTagList(username, projectName);

        List<String> historyProjectPathList = new LinkedList<>();
        for (TaggedCommit tc : taggedCommits) {
            String name = tc.getName();
            String sha = tc.getCommit().getSha();
            String historyPath = localPath + "/" + name;
            File historyFile = new File(historyPath);
            if (!historyFile.exists()) {
                historyFile.mkdir();
            }

            System.out.println("切换到" + sha + "分支前······");

            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(sha).call();

            System.out.println("切换到" + sha + "分支后······");

            FileUtils.copyDirectory(new File(basePath), new File(historyPath));

            historyProjectPathList.add(historyPath);

            System.out.println("路径为" + historyPath);
        }

        return historyProjectPathList;
    }

    public static void main(String[] args) throws GitAPIException, IOException {
        // DownloadCodeBySha1.getTagList("docmirror", "dev-sidecar");
        // DownloadCodeBySha1.getShaList("zztu", "AgileDevBlog");
        System.out.println(DownloadCodeBySha1.historyProjectPath("apache", "maven"));
    }
}
