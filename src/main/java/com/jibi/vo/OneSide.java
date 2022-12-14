package com.jibi.vo;

import static com.jibi.common.Constants.CORRUPTED;
import static com.jibi.common.Constants.ACCESS_DENIED;
import static com.jibi.common.Constants.LOCKED;

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

    public boolean compareWithoutHash(OneSide otherSide) {
        if (!exists() || !otherSide.exists()) {
            return false;
        }
        if (size == otherSide.getSize() && (lastModified != null && lastModified.compareTo(otherSide.getLastModified()) == 0)) {
            return true;
        }
        return false;
    }

    public boolean compareWithHash(OneSide otherSide) {
        if (!exists() || !otherSide.exists()) {
            return false;
        }
        if (hasSomeHashValue() && otherSide.hasSomeHashValue()
                && hash.equals(otherSide.getHash()) && size == otherSide.getSize()) {
            return true;
        }
        return false;
    }

    public boolean hasSomeHashValue() {
        return getHash() != null && (!CORRUPTED.equals(getHash()) || !ACCESS_DENIED.equals(getHash()) || !LOCKED.equals(getHash()));
    }

}
