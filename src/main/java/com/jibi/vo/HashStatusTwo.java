package com.jibi.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HashStatusTwo extends HashStatus {
    private OneSide left;
    private OneSide right;

    public HashStatusTwo() {
        super();
        left = new OneSide();
        right = new OneSide();
    }

    public HashStatusTwo(String filename, String status) {
        super(filename, status);
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
        if (MATCH.equals(getStatus())) {
            return true;
        }
        return false;
    }

    public boolean isNotMatched() {
        if (MISMATCH.equals(getStatus())) {
            return true;
        }
        return false;
    }

    public boolean isMissingFile() {
        if (MISSING.equals(getStatus())) {
            return true;
        }
        return false;
    }

    public boolean isNewFile() {
        if (NEWFILE.equals(getStatus())) {
            return true;
        }
        return false;
    }


    public void updateSideStatus(String status, String leftStatus, String rightStatus) {
        setStatus(status);
        getLeft().setStatus(leftStatus);
        getRight().setStatus(rightStatus);
    }
}


