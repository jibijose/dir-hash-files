package com.jibi.vo;

import lombok.*;

import java.util.Date;

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
}
