package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.annotation.AutoFill;
import org.example.entity.judge.CaseJudgeResult;
import org.example.entity.judge.Judge;
import org.example.enumeration.OperationType;

import java.util.List;


@Mapper
public interface JudgeMapper {

    @AutoFill(value = OperationType.INSERT)
    void addJudgeResult(Judge judge);

    void addCaseResult(List<CaseJudgeResult> caseJudgeResults);

    Judge getJudgeBySubmitId(Long submitId);

    @AutoFill(value = OperationType.UPDATE)
    void updateJudgeResult(Judge judge);
}
