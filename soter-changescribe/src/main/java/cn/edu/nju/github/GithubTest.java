package cn.edu.nju.github;

import cn.edu.nju.github.entity.CommitEntity;
import cn.edu.nju.github.entity.GithubRepo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

public class GithubTest {
    public static void main(String[] args) throws IOException, ParseException {
        List<CommitEntity> repoCommits = MyGithubApi.getRepoCommits(new GithubRepo("Netflix", "Hystrix"));
        System.out.println(repoCommits.toArray());
    }
}
