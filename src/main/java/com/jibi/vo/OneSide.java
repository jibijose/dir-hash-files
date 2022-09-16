package com.jibi.vo;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OneSide {

    private String status;
    private String hash;
    private long size = -1;
    private Date lastModified;

    public boolean exists() {
        if (size >= 0 && lastModified != null) {
            return true;
        }
        return false;
    }

    public boolean compare(OneSide otherSide) {
        if (!exists() || !otherSide.exists()) {
            return false;
        }
        if (hash.equals(otherSide.getHash()) && size == otherSide.getSize()
                && (lastModified != null && lastModified.compareTo(otherSide.getLastModified()) == 0)) {
            return true;
        }
        return false;
    }

}