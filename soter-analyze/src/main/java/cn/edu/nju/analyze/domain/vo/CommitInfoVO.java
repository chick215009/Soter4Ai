package cn.edu.nju.analyze.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommitInfoVO {
    String sha;
    String message;
    Date date;
}
