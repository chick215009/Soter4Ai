package cn.edu.nju.traning;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

import java.io.File;
import java.util.List;

public class MainTest {
    public static void main(String[] args) {
        File left = new File("/Users/chengleming/Downloads/data_zip/data/cs/input/dataset/dataset_split_train_test/testset/android/platform_frameworks_base/a8ae3e94c4b26ec0f1ee6deb1e41abe1a0697a94/A@WebViewClassic.java");

        File right = new File("/Users/chengleming/Downloads/data_zip/data/cs/input/dataset/dataset_split_train_test/testset/android/platform_frameworks_base/a8ae3e94c4b26ec0f1ee6deb1e41abe1a0697a94/B@WebViewClassic.java");

        FileDistiller distiller = ChangeDistiller.createFileDistiller(ChangeDistiller.Language.JAVA);

        try {
            distiller.extractClassifiedSourceCodeChanges(left, right);
        } catch(Exception e) {
    /* An exception most likely indicates a bug in ChangeDistiller. Please file a
       bug report at https://bitbucket.org/sealuzh/tools-changedistiller/issues and
       attach the full stack trace along with the two files that you tried to distill. */
            System.err.println("Warning: error while change distilling. " + e.getMessage());
        }

        List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
        if(changes != null) {
            for(SourceCodeChange change : changes) {
                // see Javadocs for more information
                System.out.println(change.toString());
            }
        }
    }
}
