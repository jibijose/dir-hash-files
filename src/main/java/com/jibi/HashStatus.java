package com.jibi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HashStatus {

    public static String MATCHED = "Matched";
    public static String NOTMATCHED = "Not Matched";
    public static String MISSINGFILE = "Missing File";
    public static String NEWFILE = "New File";


    private String filename;
    private String status;
    private String lefthash;
    private String righthash;

    public static HashStatus buildWithLeftHash(String filename, String status, String lefthash) {
        HashStatus hashStatus = new HashStatus();
        hashStatus.setFilename(filename);
        hashStatus.setStatus(status);
        hashStatus.setLefthash(lefthash);
        return hashStatus;
    }

    public boolean isMatched() {
        if (MATCHED.equals(status)) {
            return true;
        }
        return false;
    }

    public boolean isNotMatched() {
        if (NOTMATCHED.equals(status)) {
            return true;
        }
        return false;
    }

    public boolean isMissingFile() {
        if (MISSINGFILE.equals(status)) {
            return true;
        }
        return false;
    }

    public boolean isNewFile() {
        if (NEWFILE.equals(status)) {
            return true;
        }
        return false;
    }
}
