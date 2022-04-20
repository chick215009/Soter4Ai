package cn.edu.nju.analyze.mapper;

import cn.edu.nju.analyze.domain.vo.GithubStereotypeVO;

import java.util.List;

public interface GitHubStereotypeMapper {
    List<String> selectFileList(GithubStereotypeVO githubStereotypeVO);
    void insertStereotype(GithubStereotypeVO githubStereotypeVO);
}
