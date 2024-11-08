package com.hnue.english.service;

import com.hnue.english.dto.ListVocab;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExternalApiService {
    private final RestTemplate restTemplate;
    private static final String EXTERNAL_API_URL = "https://audio.easyvocab.click/api/dictionary/en/";

    public Map<String, Object> getWordDefinitionAsMap(String word){
        String url = EXTERNAL_API_URL + word;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        if (response.getStatusCode() == HttpStatus.OK){
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("error")) {
                throw new RuntimeException(word + " không hợp lệ");
            }
            return responseBody;
        }else {
            throw new RuntimeException(word + " không hợp lệ");
        }
    }

    public List<String> checkValidWords(List<ListVocab> list) {
        List<String> invalidWords = new ArrayList<>();
        for (ListVocab vocab : list) {
            try {
                getWordDefinitionAsMap(vocab.getWord());
            } catch (RuntimeException e) {
                invalidWords.add(vocab.getWord());
            }
        }
        return invalidWords;
    }
}
