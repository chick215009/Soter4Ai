package cn.edu.nju.github.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.edu.nju.github.mapper.GitCommitMapper;
import cn.edu.nju.github.domain.GitCommit;
import cn.edu.nju.github.service.IGitCommitService;

/**
 * git commit信息Service业务层处理
 *
 * @author clm
 * @date 2022-02-14
 */
@Service
public class GitCommitServiceImpl implements IGitCommitService
{
    @Autowired
    private GitCommitMapper gitCommitMapper;

    private static SimpleDateFormat TZDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 查询git commit信息
     *
     * @param id git commit信息主键
     * @return git commit信息
     */
    @Override
    public GitCommit selectGitCommitById(Long id)
    {
        return gitCommitMapper.selectGitCommitById(id);
    }

    /**
     * 查询git commit信息列表
     *
     * @param gitCommit git commit信息
     * @return git commit信息
     */
    @Override
    public ArrayList<GitCommit> selectGitCommitList(GitCommit gitCommit) {
        ArrayList<GitCommit> gitCommits = new ArrayList<>();
        String username = gitCommit.getUsername();
        String repoName = gitCommit.getRepoName();
        if (username == null || repoName == null) {
            return gitCommits;
        }

        String urlStr = "https://api.github.com/repos" + "/" + username + "/" + repoName + "/commits";
        try {
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
            Long id = 1L;


            for (Object o : JSONArray.parseArray(line)) {
                JSONObject jsonObject = (JSONObject) o;

                String sha = jsonObject.getString("sha");
                String dateStr = jsonObject.getJSONObject("commit").getJSONObject("author").getString("date");
                String message = jsonObject.getJSONObject("commit").getString("message");
                String htmlUrl = jsonObject.getString("html_url");
                String treeSha = jsonObject.getJSONObject("commit").getJSONObject("tree").getString("sha");
                String treeUrl = jsonObject.getJSONObject("commit").getJSONObject("tree").getString("url");

                gitCommits.add(new GitCommit(id++, sha, message, TZDateFormat.parse(dateStr), htmlUrl, treeSha, treeUrl, username, repoName));
            }

            try {
                if ( gitCommit.getParams().get("beginDate") != null && gitCommit.getParams().get("endDate") != null) {
                    Date beginDate = sdf.parse((String) gitCommit.getParams().get("beginDate"));
                    Date endDate = sdf.parse((String) gitCommit.getParams().get("endDate"));

                    Iterator<GitCommit> iterator = gitCommits.iterator();
                    while (iterator.hasNext()) {
                        GitCommit commit = iterator.next();
                        if (commit.getDate().after(endDate) || commit.getDate().before(beginDate)) {
                            iterator.remove();
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String sha = gitCommit.getSha();
            if (sha != null && !"".equals(sha)) {
                for (GitCommit commit : gitCommits) {
                    if (commit.getSha().equals(sha)) {
                        return new ArrayList<GitCommit>(){{
                            add(commit);
                        }};
                    }
                }
                return new ArrayList<>();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return gitCommits;
    }

    /**
     * 新增git commit信息
     *
     * @param gitCommit git commit信息
     * @return 结果
     */
    @Override
    public int insertGitCommit(GitCommit gitCommit)
    {
        return gitCommitMapper.insertGitCommit(gitCommit);
    }

    /**
     * 修改git commit信息
     *
     * @param gitCommit git commit信息
     * @return 结果
     */
    @Override
    public int updateGitCommit(GitCommit gitCommit)
    {
        return gitCommitMapper.updateGitCommit(gitCommit);
    }

    /**
     * 批量删除git commit信息
     *
     * @param ids 需要删除的git commit信息主键
     * @return 结果
     */
    @Override
    public int deleteGitCommitByIds(Long[] ids)
    {
        return gitCommitMapper.deleteGitCommitByIds(ids);
    }

    /**
     * 删除git commit信息信息
     *
     * @param id git commit信息主键
     * @return 结果
     */
    @Override
    public int deleteGitCommitById(Long id)
    {
        return gitCommitMapper.deleteGitCommitById(id);
    }
}
