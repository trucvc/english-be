package com.hnue.english.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListVocab {
    private String word;
    private String meaning;
    private int topicId;
    private String exampleSentence;
    private String pronunciation;
    private String audio;
}
