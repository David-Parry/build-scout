package com.davidparry.scout.io;

import com.davidparry.scout.common.LogFactory;

public interface LogFile {

    void close();

    void write(String message, Throwable exception);

    void write(String message);

    void rawWrite(String message);

    LogFactory getLogFactory();
}
