package com.czh.mianshiha.model.dto.questionbankquestion;

import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionBankQuestionRemoveRequest implements Serializable {

    private static final long serialVersionUID = 1077295666596415720L;

    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 题目 id
     */
    private Long questionId;
}
