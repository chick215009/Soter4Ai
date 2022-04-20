package cn.edu.nju.github.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.edu.nju.common.annotation.Excel;
import cn.edu.nju.common.core.domain.BaseEntity;

/**
 * git commit信息对象 git_commit
 *
 * @author clm
 * @date 2022-02-14
 */
@NoArgsConstructor
public class GitCommit extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** commit id */
    private Long id;

    /** commit SHA */
    @Excel(name = "commit SHA")
    private String sha;

    /** 提交信息 */
    @Excel(name = "提交信息")
    private String message;

    /** 提交日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "提交日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date date;

    /** 提交页面 */
    @Excel(name = "提交页面")
    private String htmlUrl;

    /** tree SHA */
    @Excel(name = "tree SHA")
    private String treeSha;

    /** tree URL */
    @Excel(name = "tree URL")
    private String treeUrl;

    /** 用户名 */
    private String username;

    /** 仓库名 */
    private String repoName;

    public GitCommit(Long id, String sha, String message, Date date, String htmlUrl, String treeSha, String treeUrl, String username, String repoName) {
        this.id = id;
        this.sha = sha;
        this.message = message;
        this.date = date;
        this.htmlUrl = htmlUrl;
        this.treeSha = treeSha;
        this.treeUrl = treeUrl;
        this.username = username;
        this.repoName = repoName;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setSha(String sha)
    {
        this.sha = sha;
    }

    public String getSha()
    {
        return sha;
    }
    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }
    public void setDate(Date date)
    {
        this.date = date;
    }

    public Date getDate()
    {
        return date;
    }
    public void setHtmlUrl(String htmlUrl)
    {
        this.htmlUrl = htmlUrl;
    }

    public String getHtmlUrl()
    {
        return htmlUrl;
    }
    public void setTreeSha(String treeSha)
    {
        this.treeSha = treeSha;
    }

    public String getTreeSha()
    {
        return treeSha;
    }
    public void setTreeUrl(String treeUrl)
    {
        this.treeUrl = treeUrl;
    }

    public String getTreeUrl()
    {
        return treeUrl;
    }
    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }
    public void setRepoName(String repoName)
    {
        this.repoName = repoName;
    }

    public String getRepoName()
    {
        return repoName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("sha", getSha())
            .append("message", getMessage())
            .append("date", getDate())
            .append("htmlUrl", getHtmlUrl())
            .append("treeSha", getTreeSha())
            .append("treeUrl", getTreeUrl())
            .append("username", getUsername())
            .append("repoName", getRepoName())
            .toString();
    }
}
