package com.jibi.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class HashStatus {
    public final static String NEWFILE = "NewFile";
    public final static String MATCH = "Match";
    public final static String MISMATCH = "Mismatch";
    public final static String MISSING = "Missing";

    public final static String INSYNC = "InSync";
    public final static String NOTSYNCED = "NotSynced";

    private String filename;
    private String status;

    public HashStatus(String filename, String status) {
        this();
        this.filename = filename;
        this.status = status;
    }
}
