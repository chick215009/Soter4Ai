package cn.edu.nju.core.integration;

import cn.edu.nju.core.git.ChangedFile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ScribeContent {
    List<ChangedFile> addedFiles;
    List<ChangedFile> missingFiles;
    List<ChangedFile> modifiedFiles;

    public ScribeContent() {
        this.addedFiles = new ArrayList<>();
        this.missingFiles = new ArrayList<>();
        this.modifiedFiles = new ArrayList<>();
    }
}
