package cn.edu.nju.analyze.summarize;

import cn.edu.nju.analyze.domain.SummaryEntity;
import cn.edu.nju.analyze.service.impl.CodeAnalyzeServiceImpl;
import cn.edu.nju.analyze.service.impl.FileServiceImpl;
import cn.edu.nju.analyze.service.impl.GitServiceImpl;
import cn.edu.nju.common.config.RuoYiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GitHub;
import com.squareup.okhttp.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class GetLocalGitFile {
    String tmpPath;
    public GetLocalGitFile(String tmpPath){
        this.tmpPath = tmpPath;
        File tmpFile = new File(tmpPath);
        if (tmpFile.exists()){
            tmpFile.mkdir();
        }
    }
    public static Git openRpo(String dir){
        Git git = null;
        try {
            Repository repository = new FileRepositoryBuilder().setGitDir(Paths.get(dir,".git").toFile()).build();
            git  = new Git(repository);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return git;
    }

    public String ProjectCommitPath(String shaCode,String baseProjectPath) throws GitAPIException, IOException {
        Git git = openRpo(baseProjectPath);
        //new FileServiceImpl().clearDirectory(tmpPath);
        //String latestCommitID = git.getRepository().exactRef("refs/heads/main").getObjectId().name();
        //String latestCommitID = git.getRepository().resolve("HEAD").name();
        //System.out.println(latestCommitID);

        //String nowBranchShaBefore = tmpPath + "/shaBefore";
        //String nowBranchShaAfter = tmpPath + "/shaAfter";

        git.reset().setMode(ResetCommand.ResetType.HARD).setRef(shaCode).call();
        //FileUtils.copyDirectory(new File(baseProjectPath), new File(nowBranchShaAfter));

        git.reset().setMode(ResetCommand.ResetType.SOFT).setRef("HEAD~1").call();
        //FileUtils.copyDirectory(new File(baseProjectPath), new File(nowBranchShaBefore));
        System.out.println("End After Version Copy.");

        /*git.reset().setMode(ResetCommand.ResetType.HARD).setRef("HEAD~1").call();
        FileUtils.copyDirectory(new File(baseProjectPath), new File(nowBranchShaBefore));

        System.out.println("End Before Version Copy.");

        git.reset().setMode(ResetCommand.ResetType.HARD).setRef(latestCommitID).call();

        new FileServiceImpl().updateProject(nowBranchShaBefore,nowBranchShaAfter);
        new GitServiceImpl().gitAddAll(nowBranchShaBefore);

        System.out.println("End git add.");*/

        SummaryEntity summaryEntity = new CodeAnalyzeServiceImpl().getSummaryEntity(baseProjectPath);
        //String sf = summaryEntity.getMethodStatisticJson();

        System.out.println("End analyze.");

        String sf = ChangeAnalyzer.getDescribe(summaryEntity);
        //System.out.println(sf);

        return sf;
    }


}
