package cn.edu.nju.github.mapper;

import java.util.List;
import cn.edu.nju.github.domain.GitCommit;

/**
 * git commit信息Mapper接口
 * 
 * @author clm
 * @date 2022-02-14
 */
public interface GitCommitMapper 
{
    /**
     * 查询git commit信息
     * 
     * @param id git commit信息主键
     * @return git commit信息
     */
    public GitCommit selectGitCommitById(Long id);

    /**
     * 查询git commit信息列表
     * 
     * @param gitCommit git commit信息
     * @return git commit信息集合
     */
    public List<GitCommit> selectGitCommitList(GitCommit gitCommit);

    /**
     * 新增git commit信息
     * 
     * @param gitCommit git commit信息
     * @return 结果
     */
    public int insertGitCommit(GitCommit gitCommit);

    /**
     * 修改git commit信息
     * 
     * @param gitCommit git commit信息
     * @return 结果
     */
    public int updateGitCommit(GitCommit gitCommit);

    /**
     * 删除git commit信息
     * 
     * @param id git commit信息主键
     * @return 结果
     */
    public int deleteGitCommitById(Long id);

    /**
     * 批量删除git commit信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGitCommitByIds(Long[] ids);
}
