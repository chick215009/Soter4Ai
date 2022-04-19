import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @ClassName: DownloadCodeBySha
 * @Description: TODO
 * @Author panpan
 */
public class DownloadCodeBySha {
    public static List<String> getShaList(String userName, String projectName) throws IOException {
        List<String> res = new LinkedList<>();
        GitHub github = GitHub.connectAnonymously();
        List<GHCommit> commits = github.getUser(userName)
                .getRepository(projectName)
                .queryCommits()
                .until(new Date())
                .list()
                .toList();
        for (GHCommit commit : commits) {
            res.add(commit.getSHA1());
        }
        return res;
    }

    public static List<String> historyProjectPath(String username, String projectName) throws GitAPIException, IOException {
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

        List<String> shaList = DownloadCodeBySha.getShaList(username, projectName);

        List<String> historyProjectPathList = new LinkedList<>();
        for (String sha : shaList) {
            String historyPath = localPath + "\\" + sha;
            File historyFile = new File(historyPath);
            if (!historyFile.exists()) {
                historyFile.mkdir();
            }

            System.out.println("切换到" + sha + "分支前······");

            git.checkout()
                    .setName(sha) //设置历史版本的sha
                    .call();

            System.out.println("切换到" + sha + "分支后······");

            FileUtils.copyDirectory(new File(basePath), new File(historyPath));

            historyProjectPathList.add(historyPath);

            System.out.println("路径为" + historyPath);
        }

        return historyProjectPathList;
    }

    public static void main(String[] args) throws GitAPIException, IOException {
        // DownloadCodeBySha.getSha("zztu", "AgileDevBlog");
        System.out.println(DownloadCodeBySha.historyProjectPath("zztu", "AgileDevBlog"));
    }
}
