package com.hnue.english.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabDTO {
    private String word;
    private String meaning;
    private String exampleSentence;
    private String pronunciation;
    private MultipartFile image;
}
