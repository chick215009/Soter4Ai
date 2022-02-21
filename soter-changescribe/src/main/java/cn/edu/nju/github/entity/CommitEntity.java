package cn.edu.nju.github.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class CommitEntity {
    private String sha;
    private Date committedDate;
    private String message;
    private String htmlUrl;
    private GithubRepoTree tree;
}
