import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Test;

import java.io.File;

public class TestGitClone {
    @Test
    public void test01() {
        String username = "docmirror";
        String repoName = "dev-sidecar";
        String githubPath = "https://github.com/" + username + "/" + repoName + ".git";
        String localPath = System.getProperty("user.dir")+ "/tmp/" + repoName;
        String basePath = localPath + "/base";
        CredentialsProvider provider = new UsernamePasswordCredentialsProvider("zztu", "QianPeng2");
        try {
            Git git = Git.cloneRepository()
                    .setCredentialsProvider(provider)
                    .setURI(githubPath)
                    .setDirectory(new File(basePath))
                    .setCloneAllBranches(true)
                    .call();
        } catch (InvalidRemoteException e) {
            e.printStackTrace();
        } catch (TransportException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }


    }
}
