package cn.edu.nju.changescribe.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class ChangedFileStatistic {
    private Integer totalFileNum;
    private Integer addedFileNum;
    private Integer removedFileNum;
    private Integer changedFileNum;

    public ChangedFileStatistic() {
        this.totalFileNum = 0;
        this.addedFileNum = 0;
        this.removedFileNum = 0;
        this.changedFileNum = 0;
    }
}
