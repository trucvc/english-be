package com.hnue.english.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportFromJson {
    private int countSuccess;
    private int countError;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> error;
}
