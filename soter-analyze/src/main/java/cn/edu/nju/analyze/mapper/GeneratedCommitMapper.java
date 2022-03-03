package cn.edu.nju.analyze.mapper;

import cn.edu.nju.analyze.domain.vo.GeneratedCommitVO;

import java.util.List;

public interface GeneratedCommitMapper {
    Integer insertGeneratedCommit(GeneratedCommitVO generatedCommitVO);
    List<GeneratedCommitVO> selectGeneratedCommitList();


}
