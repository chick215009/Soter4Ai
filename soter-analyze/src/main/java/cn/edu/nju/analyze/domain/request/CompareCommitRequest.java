package cn.edu.nju.analyze.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompareCommitRequest {
    String username;
    String repoName;
    String preCommit;
    String nextCommit;
}
