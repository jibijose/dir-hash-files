package com.jibi.vo;

import lombok.*;

import java.util.Date;

@Getter
@Setter
public class HashStatusTwo {

    public static String MATCHED = "Matched";
    public static String NOTMATCHED = "Not Matched";
    public static String MISSINGFILE = "Missing File";
    public static String NEWFILE = "New File";


    private String filename;
    private String status;
    private OneSide left;
    private OneSide right;

    public HashStatusTwo() {
        left = new OneSide();
        right = new OneSide();
    }

    public HashStatusTwo(String filename, String status) {
        super();
        this.filename = filename;
        this.status = status;
    }

    public static HashStatusTwo buildWithLeftHash(String filename, String status, FileInfo fileInfoLeft) {
        HashStatusTwo hashStatus = new HashStatusTwo();
        hashStatus.setFilename(filename);
        hashStatus.setStatus(status);
        hashStatus.getLeft().setHash(fileInfoLeft.getHash());
        hashStatus.getLeft().setSize(fileInfoLeft.getSize());
        hashStatus.getLeft().setLastModified(fileInfoLeft.getLastModified());
        return hashStatus;
    }

    public static HashStatusTwo buildWithRightHash(String filename, String status, FileInfo fileInfoRight) {
        HashStatusTwo hashStatus = new HashStatusTwo();
        hashStatus.setFilename(filename);
        hashStatus.setStatus(status);
        hashStatus.getRight().setHash(fileInfoRight.getHash());
        hashStatus.getRight().setSize(fileInfoRight.getSize());
        hashStatus.getRight().setLastModified(fileInfoRight.getLastModified());
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

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class OneSide {
        private String hash;
        private long size = -1;
        private Date lastModified;
    }
}


