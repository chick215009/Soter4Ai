package cn.edu.nju.github;

import cn.edu.nju.github.entity.CommitEntity;
import cn.edu.nju.github.entity.GithubRepo;
import cn.edu.nju.github.entity.GithubRepoTree;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MyGithubApi {
    private static SimpleDateFormat TZDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static List<CommitEntity> getRepoCommits(GithubRepo repo) throws IOException, ParseException {
        List<CommitEntity> commitEntities = new ArrayList<>();

        String urlStr = "https://api.github.com/repos" + "/" + repo.getUsername() + "/" + repo.getRepoName() + "/commits";
        URL url = new URL(urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));

        String line = br.readLine();
        for (Object o : JSONArray.parseArray(line)) {
            JSONObject jsonObject = (JSONObject) o;

            String sha = jsonObject.getString("sha");
            String dateStr = jsonObject.getJSONObject("commit").getJSONObject("author").getString("date");
            String message = jsonObject.getJSONObject("commit").getString("message");
            String htmlUrl = jsonObject.getString("html_url");
            String treeSha = jsonObject.getJSONObject("commit").getJSONObject("tree").getString("sha");
            String treeUrl = jsonObject.getJSONObject("commit").getJSONObject("tree").getString("url");

            commitEntities.add(new CommitEntity(sha, TZDateFormat.parse(dateStr), message, htmlUrl, new GithubRepoTree(treeSha, treeUrl)));

        }


        return commitEntities;
    }

    public void getFileHistory() {
        //https://api.github.com/repos/Netflix/Hystrix/commits?path=OSSMETADATA
    }
}
