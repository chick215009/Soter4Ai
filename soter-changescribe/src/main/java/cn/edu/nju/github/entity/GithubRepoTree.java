package cn.edu.nju.github.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GithubRepoTree {
    private String sha;
    private String url;
}
