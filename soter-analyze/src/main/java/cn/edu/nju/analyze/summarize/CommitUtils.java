package cn.edu.nju.analyze.summarize;

import cn.edu.nju.common.config.RuoYiConfig;
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
            nameToSha.put(tc.getName(), tc.getCommit().getSha());
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
    public static List<String> recentCommitsPath(String username, String projectName, String curSha, String preSha) throws GitAPIException, IOException {

        String localPath = "/Users/chengleming/MasterThesis/Soter/tmp/compare/" + projectName;
        if (new File(localPath).exists()) {
            FileUtils.deleteDirectory(new File(localPath));
        }

        String basePath = localPath + "/base";
        baseCommitPath(username,projectName);
        CredentialsProvider provider = new UsernamePasswordCredentialsProvider("zztu", "QianPeng2");
        Git git = Git.open(new File(basePath));

        List<String> resPathList = new LinkedList<>();

        String curPath = localPath + "/" + curSha;
        File curFile = new File(curPath);
        if (!curFile.exists()) {
            curFile.mkdir();
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(curSha).call();
            FileUtils.copyDirectory(new File(basePath), new File(curPath));
            resPathList.add(curPath);
        } else {
            resPathList.add(curPath);
            if (curFile.list().length > 0) {
                System.out.println(curFile + "存在且不为空！");
            } else {
                System.out.println(curFile + "存在且为空！");
            }
        }

        String prePath = localPath + "/" + preSha;
        File preFile = new File(prePath);
        if (!preFile.exists()) {
            preFile.mkdir();
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(preSha).call();
            FileUtils.copyDirectory(new File(basePath), new File(prePath));
            resPathList.add(prePath);
        } else {
            resPathList.add(prePath);
            if (preFile.list().length > 0) {
                System.out.println(preFile + "存在且不为空！");
            } else {
                System.out.println(preFile + "存在且为空！");
            }
        }

        return resPathList;
    }

    public static String baseCommitPath(String username, String projectName) throws GitAPIException {
        String localPath = "/Users/chengleming/MasterThesis/Soter/tmp/compare/" + projectName;
        String githubPath = "https://github.com/" + username + "/" + projectName + ".git";

        System.out.println(localPath);
        System.out.println(githubPath);
        String basePath = localPath + "/base";
        File file = new File(basePath);
        if (!file.exists()) {
            file.mkdir();
            System.out.println("下载开始······");
            CredentialsProvider provider = new UsernamePasswordCredentialsProvider("zztu", "QianPeng2");
            Git git = Git.cloneRepository()
                    .setCredentialsProvider(provider)
                    .setURI(githubPath)
                    .setDirectory(new File(basePath))
                    .setCloneAllBranches(true)
                    .call();
            System.out.println("下载完成······");
        } else {
            if (file.list().length > 0) {
                System.out.println(basePath + "存在且不为空！");
            } else {
                System.out.println(basePath + "存在且为空！");
            }
        }
        return basePath;
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

    public static List<CommitShortInfo> getCommitsListBetweenAdjacentTags(String userName, String projectName, String util) throws IOException, ParseException {
        TaggedCommit[] list = DownloadCodeBySha.getTagList(userName, projectName);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.length-1; i++) {
            if (list[i].getName().equals(util)) {
                sb.append(list[i+1].getName());
                break;
            }
        }
        String since = sb.toString();
        return getCommitsListBetweenTags(userName, projectName, since, util);
    }

    public static void main(String[] args) throws IOException, ParseException, GitAPIException {
        // CommitUtils.getCommitsListBetweenTags("docmirror", "dev-sidecar", "v1.7.2", "v1.7.3");
        // System.out.println(getTagDate("docmirror", "dev-sidecar", "ade3470ed07475a6c1707f7fa3b2b5b44ccefbfd").toString());
        // CommitUtils.getRecentCommitsList("docmirror", "dev-sidecar");
        // recentCommitsPath("docmirror", "dev-sidecar", "ade3470ed07475a6c1707f7fa3b2b5b44ccefbfd");
        baseCommitPath("yangzongzhuan", "RuoYi");
//        recentCommitsPath("yangzongzhuan", "RuoYi", "4613984fb4c02ff4eb5b506bcaf47d37a45cf4ed", "893b29cae849c1b027e115d6c8560ce1c489418c");;
    }
}
