package cn.edu.nju.analyze.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyzeRequest {
    String username;
    String repoName;
    String[] tags;
}
