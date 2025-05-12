package com.davidparry.scout.common;

import com.google.gson.Gson;

/**
 * Record representing file information for GetFileInfo tool.
 */
public record FileInfoRecord(
    boolean exists,
    boolean isDirectory,
    long size,
    long lastModified,
    boolean canRead,
    boolean canWrite,
    String absolutePath,
    String error // null if no error
) {
    public static FileInfoRecord fromFile(java.io.File file) {
        if (file.exists()) {
            return new FileInfoRecord(
                true,
                file.isDirectory(),
                file.length(),
                file.lastModified(),
                file.canRead(),
                file.canWrite(),
                file.getAbsolutePath(),
                null
            );
        } else {
            return new FileInfoRecord(
                false,
                false,
                0L,
                0L,
                false,
                false,
                null,
                "File does not exist"
            );
        }
    }
    public String toJson() {
        return new Gson().toJson(this);
    }
}