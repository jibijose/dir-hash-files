package com.jibi.common;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class HashOperation {

    private Algorithm algoSelected;

    public HashOperation(Algorithm algoSelected) {
        this.algoSelected = algoSelected;
    }

    public String getFileChecksum(File file) {
        if (algoSelected == null) {
            return "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(algoSelected.getValue());
            //Get file input stream for reading the file content
            FileInputStream fis = new FileInputStream(file);

            //Create byte array to read data in chunks
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;

            //Read file data and update in message digest
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            ;

            //close the stream; We don't need it now.
            fis.close();

            //Get the hash's bytes
            byte[] bytes = digest.digest();

            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (IOException ioException) {
            log.warn("IOException in hashing file [{}]   {}", file, ioException.getMessage());
            if (ioException.getClass() != null && "java.io.FileNotFoundException".equals(ioException.getClass().getName())) {
                return Constants.ACCESS_DENIED;
            } else if (ioException.getMessage().equals("The process cannot access the file because another process has locked a portion of the file")) {
                return Constants.LOCKED;
            } else {
                return Constants.CORRUPTED;
            }
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            log.error("No such algorithm", noSuchAlgorithmException);
            throw new RuntimeException("No such algorithm");
        }
    }
}
