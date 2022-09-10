package com.jibi;

import lombok.*;

import java.util.Date;

@Getter
@Setter
public class HashStatus {

    public static String MATCHED = "Matched";
    public static String NOTMATCHED = "Not Matched";
    public static String MISSINGFILE = "Missing File";
    public static String NEWFILE = "New File";


    private String filename;
    private String status;
    private OneSide left;
    private OneSide right;

    public HashStatus() {
        left = new OneSide();
        right = new OneSide();
    }

    public HashStatus(String filename, String status) {
        super();
        this.filename = filename;
        this.status = status;
    }

    public static HashStatus buildWithLeftHash(String filename, String status, FileInfo fileInfoLeft) {
        HashStatus hashStatus = new HashStatus();
        hashStatus.setFilename(filename);
        hashStatus.setStatus(status);
        hashStatus.getLeft().setHash(fileInfoLeft.getHash());
        hashStatus.getLeft().setSize(fileInfoLeft.getSize());
        hashStatus.getLeft().setLastModified(fileInfoLeft.getLastModified());
        return hashStatus;
    }

    public static HashStatus buildWithRightHash(String filename, String status, FileInfo fileInfoRight) {
        HashStatus hashStatus = new HashStatus();
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
        private long size;
        private Date lastModified;
    }
}


