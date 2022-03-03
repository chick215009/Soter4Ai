package cn.edu.nju.analyze.service.impl;

import cn.edu.nju.analyze.domain.AnalyzeResult;
import cn.edu.nju.analyze.domain.SummaryEntity;
import cn.edu.nju.analyze.domain.vo.CommitGenerateVO;
import cn.edu.nju.analyze.domain.vo.CommitGeneratedStatisticsVO;
import cn.edu.nju.analyze.domain.vo.GeneratedCommitVO;
import cn.edu.nju.analyze.mapper.GeneratedCommitMapper;
import cn.edu.nju.analyze.service.ICodeAnalyzeService;
import cn.edu.nju.analyze.summarize.ChangeAnalyzer;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class CodeAnalyzeServiceImpl implements ICodeAnalyzeService {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Autowired
    GeneratedCommitMapper analyzeResultMapper;

    @Override
    public SummaryEntity getSummaryEntity(String localProjectPath) {
        ChangeAnalyzer changeAnalyzer = new ChangeAnalyzer(localProjectPath);
        changeAnalyzer.analyze();
        SummaryEntity summaryEntity = changeAnalyzer.getSummaryEntity();
        if (summaryEntity.getIsInitialCommit()) {
            summaryEntity.setCommitStereotype("INITIAL COMMIT");
        }
        String summaryEntityJSON = JSON.toJSONString(summaryEntity, SerializerFeature.WriteClassName);
        SummaryEntity summaryEntity1 = JSON.parseObject(summaryEntityJSON, SummaryEntity.class);
        analyzeResultMapper.insertGeneratedCommit(new GeneratedCommitVO(new Date(),localProjectPath, summaryEntityJSON));
        return summaryEntity;
    }

    @Override
    public CommitGenerateVO getCommitGenerateVO(String localProjectPath) {
        ChangeAnalyzer changeAnalyzer = new ChangeAnalyzer(localProjectPath);
        changeAnalyzer.analyze();
        SummaryEntity summaryEntity = changeAnalyzer.getSummaryEntity();
        String describe = changeAnalyzer.getDescribe(summaryEntity);
        String summaryEntityJSON = JSON.toJSONString(summaryEntity);
        return new CommitGenerateVO(summaryEntity.getMethodStatisticJson(), describe, summaryEntity.getCommitStereotype(),summaryEntityJSON);
    }

    @Override
    public List<GeneratedCommitVO> getCommitGeneratedHistory() {
        return analyzeResultMapper.selectGeneratedCommitList();
    }

    @Override
    public CommitGeneratedStatisticsVO getCommitGeneratedStatisticsVO() {
        List<GeneratedCommitVO> generatedCommitVOList = analyzeResultMapper.selectGeneratedCommitList();
        generatedCommitVOList.sort(new Comparator<GeneratedCommitVO>() {
            @Override
            public int compare(GeneratedCommitVO o1, GeneratedCommitVO o2) {
                return o1.getGeneratedDate().compareTo(o2.getGeneratedDate());
            }
        });
        CommitGeneratedStatisticsVO statisticsVO = new CommitGeneratedStatisticsVO();
        int frequency = 1;
        for (GeneratedCommitVO generatedCommitVO : generatedCommitVOList) {
            frequency++;
            Date generatedDate = generatedCommitVO.getGeneratedDate();
            statisticsVO.getRes().
                    get(statisticsVO.getMap().get("product")).
                    add(sdf.format(generatedDate));
            SummaryEntity summaryEntity = JSON.parseObject(generatedCommitVO.getSummaryEntityJSON(), SummaryEntity.class);
            HashMap<String, Integer> hashMap = JSON.parseObject(summaryEntity.getMethodStatisticJson(), HashMap.class);
            for (String key : hashMap.keySet()) {
                Integer num = hashMap.get(key);
                statisticsVO.getRes().get(statisticsVO.getMap().get(key)).add(String.valueOf(num));
            }

            for (int i = 1; i < statisticsVO.getRes().size(); i++) {
                if (statisticsVO.getRes().get(i).size() < frequency) {
                    statisticsVO.getRes().get(i).add("0");
                }
            }
        }


        return statisticsVO;
    }

}
