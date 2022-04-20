package cn.edu.nju.analyze.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GithubStereotypeVO {
    String username;
    String repoName;
    String stereoType;
    String path;
}
