package com.hnue.english.response;

import com.hnue.english.model.Vocabulary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponse {
    private int type;
    private Vocabulary correct;
    private List<Vocabulary> incorrect;
}
