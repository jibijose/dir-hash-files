package com.jibi.vo;

import lombok.*;

import java.util.Date;

import static com.jibi.common.Constants.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class FileInfo {

    @EqualsAndHashCode.Include
    private String filename;
    @EqualsAndHashCode.Exclude
    private long size;
    @EqualsAndHashCode.Exclude
    private String hash;
    @EqualsAndHashCode.Exclude
    private Date lastModified;

    public boolean hasSomeHashValue() {
        return (!getHash().equals(CORRUPTED) || !getHash().equals(ACCESS_DENIED) || !getHash().equals(LOCKED));
    }
}
