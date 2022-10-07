package cn.edu.nju.analyze.service.impl;

import cn.edu.nju.analyze.domain.SummaryEntity;
import cn.edu.nju.analyze.domain.vo.*;
import cn.edu.nju.analyze.mapper.GeneratedCommitMapper;
import cn.edu.nju.analyze.mapper.GitHubStereotypeMapper;
import cn.edu.nju.analyze.service.ICodeAnalyzeService;
import cn.edu.nju.analyze.summarize.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CodeAnalyzeServiceImpl implements ICodeAnalyzeService {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Autowired
    GeneratedCommitMapper analyzeResultMapper;

    @Autowired
    GitHubStereotypeMapper gitHubStereotypeMapper;

    @Override
    public SummaryEntity getSummaryEntity(String localProjectPath) {
        ChangeAnalyzer changeAnalyzer = new ChangeAnalyzer(localProjectPath);
        boolean hasRes = changeAnalyzer.analyze();
        SummaryEntity summaryEntity = null;
        if (!hasRes) {
            summaryEntity = new SummaryEntity();
            summaryEntity.setCommitStereotype("无明显变化");
            summaryEntity.setSimpleDescribe("无明显变化");
        } else {
            summaryEntity = changeAnalyzer.getSummaryEntity();
            if (summaryEntity.getIsInitialCommit()) {
                summaryEntity.setCommitStereotype("INITIAL COMMIT");
            }
        }

        //String summaryEntityJSON = JSON.toJSONString(summaryEntity, SerializerFeature.WriteClassName);
        //analyzeResultMapper.insertGeneratedCommit(new GeneratedCommitVO(new Date(),localProjectPath, summaryEntityJSON));
        return summaryEntity;
    }

    @Override
    public SummaryEntity getSummaryEntity(String localProjectPath, String username, String repoName) {
        ChangeAnalyzer changeAnalyzer = new ChangeAnalyzer(localProjectPath);
        Map<String, Set<String>> stereotypeMap = new HashMap<>();
        boolean hasRes = changeAnalyzer.analyze(username, repoName, stereotypeMap);
        for (Map.Entry<String, Set<String>> entry : stereotypeMap.entrySet()) {
            String stereotype = entry.getKey();
            Set<String> paths = entry.getValue();
            for (String path : paths) {
                gitHubStereotypeMapper.insertStereotype(new GithubStereotypeVO(username, repoName, stereotype, path));
            }
        }

        if (!hasRes) {
            SummaryEntity summaryEntity = new SummaryEntity();
            summaryEntity.setCommitStereotype("无明显变化");
            summaryEntity.setSimpleDescribe("无明显变化");
        }
        SummaryEntity summaryEntity = changeAnalyzer.getSummaryEntity();
        if (summaryEntity.getIsInitialCommit()) {
            summaryEntity.setCommitStereotype("INITIAL COMMIT");
        }
        String summaryEntityJSON = JSON.toJSONString(summaryEntity, SerializerFeature.WriteClassName);
        SummaryEntity summaryEntity1 = JSON.parseObject(summaryEntityJSON, SummaryEntity.class);
        analyzeResultMapper.insertGeneratedCommit(new GeneratedCommitVO(new Date(),localProjectPath, summaryEntityJSON));
        return summaryEntity;
    }

//    @Override
//    public CommitGenerateVO getCommitGenerateVO(String localProjectPath) {
//        ChangeAnalyzer changeAnalyzer = new ChangeAnalyzer(localProjectPath);
//        changeAnalyzer.analyze();
//        SummaryEntity summaryEntity = changeAnalyzer.getSummaryEntity();
//        String describe = changeAnalyzer.getDescribe(summaryEntity);
//        String summaryEntityJSON = JSON.toJSONString(summaryEntity);
//        return new CommitGenerateVO(summaryEntity.getMethodStatisticJson(), describe, summaryEntity.getCommitStereotype(),summaryEntityJSON);
//    }

    @Override
    public List<GeneratedCommitVO> getCommitGeneratedHistoryByLocalProjectPath(String localProjectPath) {
        return analyzeResultMapper.selectGeneratedCommitListByLocalProjectPath(localProjectPath);
    }

    @Override
    public CommitGeneratedStatisticsVO getCommitGeneratedStatisticsVOByProjectPath(String localProjectPath) {
        List<GeneratedCommitVO> generatedCommitVOList = analyzeResultMapper.selectGeneratedCommitListByLocalProjectPath(localProjectPath);
        generatedCommitVOList.sort(new Comparator<GeneratedCommitVO>() {
            @Override
            public int compare(GeneratedCommitVO o1, GeneratedCommitVO o2) {
                return o1.getGeneratedDate().compareTo(o2.getGeneratedDate());
            }
        });
        CommitGeneratedStatisticsVO statisticsVO = new CommitGeneratedStatisticsVO();
        int frequency = 1;
        for (GeneratedCommitVO generatedCommitVO : generatedCommitVOList) {
            frequency++;
            Date generatedDate = generatedCommitVO.getGeneratedDate();
            statisticsVO.getRes().
                    get(statisticsVO.getMap().get("product")).
                    add(sdf.format(generatedDate));
            SummaryEntity summaryEntity = JSON.parseObject(generatedCommitVO.getSummaryEntityJSON(), SummaryEntity.class);
            HashMap<String, Integer> hashMap = JSON.parseObject(summaryEntity.getMethodStatisticJson(), HashMap.class);
            if (hashMap == null || hashMap.size() == 0) {
                return statisticsVO;
            }
            for (String key : hashMap.keySet()) {
                Integer num = hashMap.get(key);
                statisticsVO.getRes().get(statisticsVO.getMap().get(key)).add(String.valueOf(num));
            }

            for (int i = 1; i < statisticsVO.getRes().size(); i++) {
                if (statisticsVO.getRes().get(i).size() < frequency) {
                    statisticsVO.getRes().get(i).add("0");
                }
            }
        }


        return statisticsVO;
    }

    public List<String> getProjectPathInHistory() {
        return analyzeResultMapper.getProjectPathList();
    }

    public String analyzeProjectInGithub(String username, String repoName) {
        try {
            List<String> paths = DownloadCodeBySha.historyProjectPath(username, repoName);
            String projectPath = System.getProperty("user.dir") + "/tmp/" + repoName + "/base";

            FileServiceImpl fileService = new FileServiceImpl();
            GitServiceImpl gitService = new GitServiceImpl();
            fileService.clearDirectory(projectPath);
            gitService.gitInit(projectPath);


            for (int i = 0; i < paths.size() - 1; i++) {
                fileService.updateProject(projectPath, paths.get(i));
                gitService.gitAddAll(projectPath);
                SummaryEntity summaryEntity = getSummaryEntity(projectPath);
                gitService.gitCommit(projectPath, String.valueOf(i));
            }

            fileService.updateProject(projectPath, paths.get(paths.size() - 1));
            gitService.gitAddAll(projectPath);
            SummaryEntity summaryEntity = getSummaryEntity(projectPath, username, repoName);
            gitService.gitCommit(projectPath, String.valueOf(paths.size() - 1));


        } catch (Exception e) {
            e.printStackTrace();
        }

        return username + "/" + repoName + "分析完毕";


    }

    public CommitGeneratedStatisticsVO analyzeProjectInGithub(String username, String repoName, List<String> tags) {
        CommitGeneratedStatisticsVO statisticsVO = new CommitGeneratedStatisticsVO();
        try {
            TaggedCommit[] taggedCommits = DownloadCodeBySha.getTagList(username, repoName);
            Map<String, TaggedCommit> taggedCommitIndex = new HashMap<>();
            List<TaggedCommit> taggedCommitList = new ArrayList<>();
            int n = taggedCommits.length;
            int m = tags.size();
            for (int i = 0; i < n; i++) {
                taggedCommitIndex.put(String.valueOf(n - i - 1), taggedCommits[i]);
            }

            Collections.sort(tags, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return Integer.valueOf(o2) - Integer.valueOf(o1);
                }
            });
            TaggedCommit[] selectedTags = new TaggedCommit[tags.size()];
            int i = 0;
            while (i < tags.size()) {
                selectedTags[i] = taggedCommitIndex.get(tags.get(i));
                i++;
            }


            String localPath = "C:/Soter3/Soter/tmp/" + repoName;
            String githubPath = "https://github.com/" + username + "/" + repoName + ".git";

            File projectFile = new File(localPath);
            if (projectFile.exists()) {
                FileUtils.deleteDirectory(projectFile);
            }


            System.out.println(localPath);
            System.out.println(githubPath);
            String basePath = localPath + "/base";
            File file = new File(basePath);
            if (!file.exists()) {
                file.mkdir();
            } else {
                file.delete();
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

            List<String> historyProjectPathList = new LinkedList<>();
            for (TaggedCommit tc : selectedTags) {
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

            FileServiceImpl fileService = new FileServiceImpl();
            GitServiceImpl gitService = new GitServiceImpl();
            fileService.clearDirectory(basePath);
            gitService.gitInit(basePath);

            Collections.reverse(historyProjectPathList);
            fileService.updateProject(basePath, historyProjectPathList.get(0));
            gitService.gitAddAll(basePath);
            gitService.gitCommit(basePath, "Initial commit");



            List<String> list = statisticsVO.getRes().get(0);
            Collections.reverse(tags);
            for (int j = 0; j < tags.size(); j++) {
                list.add(taggedCommitIndex.get(tags.get(j)).getName());
            }
            for (int j = 1; j < statisticsVO.getRes().size(); j++) {
                statisticsVO.getRes().get(j).add("0");
            }

            int frequency = 2;
            for (int l = 1; l < historyProjectPathList.size() - 1; l++) {
                frequency++;
                fileService.updateProject(basePath, historyProjectPathList.get(l));
                gitService.gitAddAll(basePath);
                SummaryEntity summaryEntity = getSummaryEntity(basePath);
                String methodStatisticJson = summaryEntity.getMethodStatisticJson();
                HashMap<String, Integer> hashMap = JSON.parseObject(summaryEntity.getMethodStatisticJson(), HashMap.class);
                if (hashMap == null || hashMap.size() == 0) {
                    continue;
                }
                for (String key : hashMap.keySet()) {
                    Integer num = hashMap.get(key);
                    statisticsVO.getRes().get(statisticsVO.getMap().get(key)).add(String.valueOf(num));
                }
                for (int j = 1; j < statisticsVO.getRes().size(); j++) {
                    if (statisticsVO.getRes().get(j).size() < frequency) {
                        statisticsVO.getRes().get(j).add("0");
                    }
                }
                gitService.gitCommit(basePath, String.valueOf(l));
            }

            frequency++;
            fileService.updateProject(basePath, historyProjectPathList.get(historyProjectPathList.size() - 1));
            gitService.gitAddAll(basePath);
            SummaryEntity summaryEntity = getSummaryEntity(basePath, username, repoName);
            String methodStatisticJson = summaryEntity.getMethodStatisticJson();
            HashMap<String, Integer> hashMap = JSON.parseObject(summaryEntity.getMethodStatisticJson(), HashMap.class);
            if (hashMap == null || hashMap.size() == 0) {
                return statisticsVO;
            }
            for (String key : hashMap.keySet()) {
                Integer num = hashMap.get(key);
                statisticsVO.getRes().get(statisticsVO.getMap().get(key)).add(String.valueOf(num));
            }
            for (int j = 1; j < statisticsVO.getRes().size(); j++) {
                if (statisticsVO.getRes().get(j).size() < frequency) {
                    statisticsVO.getRes().get(j).add("0");
                }
            }
            gitService.gitCommit(basePath, String.valueOf(historyProjectPathList.size() - 1));



        } catch (Exception e) {
            e.printStackTrace();
        }

        return statisticsVO;


    }

    @Override
    public List<TagsVO> displayTags(String username, String repoName) {
        TaggedCommit[] taggedCommits = DownloadCodeBySha.getTagList(username, repoName);
        List<TagsVO> tagsVOList = new ArrayList<>();
        int n = taggedCommits.length;
        for (int i = 0; i < n; i++) {
            tagsVOList.add(new TagsVO(String.valueOf(n - i - 1), taggedCommits[i].getName()));
        }
        return tagsVOList;
    }

    @Override
    public List<String> getPathsByGithubStereotype(String username, String repoName, String stereotype) {
        List<String> paths = gitHubStereotypeMapper.selectFileList(new GithubStereotypeVO(username, repoName, stereotype, null));
        return paths;
    }

    @Override
    public List<CommitInfoVO> getRecentCommit(String username, String repoName) {
        try {
            List<CommitShortInfo> commitsList = CommitUtils.getRecentCommitsList(username, repoName);
            List<CommitInfoVO> res = new ArrayList<>();
            for (CommitShortInfo commitShortInfo : commitsList) {
                res.add(new CommitInfoVO(commitShortInfo.getSha(), commitShortInfo.getMessage(), commitShortInfo.getDate()));
            }
            return res;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<CommitInfoVO> getRecentCommitBeforeTag(String username, String repoName, String tag) {
        try {
            List<CommitShortInfo> commitsList = CommitUtils.getCommitsListBetweenAdjacentTags(username, repoName, tag);
            List<CommitInfoVO> res = new ArrayList<>();
            for (CommitShortInfo commitShortInfo : commitsList) {
                res.add(new CommitInfoVO(commitShortInfo.getSha(), commitShortInfo.getMessage(), commitShortInfo.getDate()));
            }
            return res;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public SummaryEntity compareCommit(String username, String repoName, String preCommit, String nextCommit) throws GitAPIException, IOException {

        List<String> paths = CommitUtils.recentCommitsPath(username, repoName, nextCommit, preCommit);
        String preCommitPath = paths.get(1);
        String nextCommitPath = paths.get(0);

        String projectPath = "/Users/chengleming/MasterThesis/Soter/tmp/compare/" + repoName + "/base1";

        File projectFile = new File(projectPath);
        if (!projectFile.exists()) {
            projectFile.mkdir();
        }


        FileServiceImpl fileService = new FileServiceImpl();

        GitServiceImpl gitService = new GitServiceImpl();

        fileService.clearDirectory(projectPath);
        gitService.gitInit(projectPath);
        fileService.updateProject(projectPath, preCommitPath);
        gitService.gitAddAll(projectPath);
        gitService.gitCommit(projectPath, "first commit");


        fileService.updateProject(projectPath, nextCommitPath);
        gitService.gitAddAll(projectPath);
        SummaryEntity summaryEntity = getSummaryEntity(projectPath);

        String summaryEntityJSON = JSON.toJSONString(summaryEntity, SerializerFeature.WriteClassName);
        SummaryEntity summaryEntity1 = JSON.parseObject(summaryEntityJSON, SummaryEntity.class);
        analyzeResultMapper.insertGeneratedCommit(new GeneratedCommitVO(new Date(),preCommitPath, summaryEntityJSON));
        return summaryEntity;
    }


    public static void main(String[] args) {
        CodeAnalyzeServiceImpl codeAnalyzeService = new CodeAnalyzeServiceImpl();
//        codeAnalyzeService.analyzeProjectInGithub("apache", "maven");
        codeAnalyzeService.displayTags("apache", "maven");

    }


}
