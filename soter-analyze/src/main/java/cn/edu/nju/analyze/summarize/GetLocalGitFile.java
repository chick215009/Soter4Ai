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
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GitHub;
import com.squareup.okhttp.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
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

    public String getFinalDescribe(String baseProjectPath){
        ChangeAnalyzer changeAnalyzer = new ChangeAnalyzer(baseProjectPath);
        boolean hasRes = changeAnalyzer.analyze();
        SummaryEntity summaryEntity = null;
        if (!hasRes && changeAnalyzer.getOtherFiles().isEmpty() && changeAnalyzer.getTypesProblem().isEmpty()) {
            summaryEntity = new SummaryEntity();
            summaryEntity.setCommitStereotype("无明显变化");
            summaryEntity.setSimpleDescribe("无明显变化");
        } else {
            summaryEntity = changeAnalyzer.getSummaryEntity();
            if (summaryEntity.getIsInitialCommit()) {
                summaryEntity.setCommitStereotype("INITIAL COMMIT");
            }
        }
        //System.out.println("End analyze.");

        return changeAnalyzer.getDescribe(summaryEntity);
    }

    public String ProjectCommitPath(String shaCode,String baseProjectPath) throws GitAPIException, IOException {
        Git git = openRpo(baseProjectPath);


        git.reset().setMode(ResetCommand.ResetType.HARD).setRef(shaCode).call();
        git.reset().setMode(ResetCommand.ResetType.SOFT).setRef("HEAD~1").call();
        //System.out.println("End After Version Copy.");

        String sf = getFinalDescribe(baseProjectPath);

        System.out.println(sf);
        return sf;
    }

}
