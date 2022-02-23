//package cn.edu.nju.core;
//
//import cn.edu.nju.core.entity.CommitField;
//import cn.edu.nju.core.git.ChangedFile;
//import cn.edu.nju.core.git.SCMRepository;
//import cn.edu.nju.core.summarizer.SummarizeChanges;
//import org.eclipse.jgit.api.Git;
//import org.eclipse.jgit.api.Status;
//import org.springframework.stereotype.Service;
//
//import java.util.Set;
//
//@Service
//public class CommitGenerate {
//    private String projectPath;
//    private CommitField commitField;
//
//
//    public CommitGenerate(String projectPath) {
//        try {
//            this.projectPath = projectPath;
//
//            SCMRepository scmRepository = new SCMRepository(projectPath);
//            Git git = scmRepository.getGit();
//            if (git == null) {
//                return;
//            }
//            Status status = scmRepository.getStatus();
//            Set<ChangedFile> differences = SCMRepository.getDifferences(status, projectPath);
//
//            SummarizeChanges summarizeChanges = new SummarizeChanges(git, projectPath);
//            ChangedFile[] changedFiles = new ChangedFile[differences.size()];
//            summarizeChanges.summarize(differences.toArray(changedFiles));
//            commitField = new CommitField(summarizeChanges.getSimpleDescribe().toString(), summarizeChanges.getDetailDescribe().toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public CommitField getCommitField() {
//        return commitField;
//    }
//}
