package com.jibi.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class HashStatus {
    private String filename;
    private String status;

    public HashStatus(String filename, String status) {
        this();
        this.filename = filename;
        this.status = status;
    }
}
