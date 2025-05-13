package com.davidparry.scout.io;

import java.util.function.Consumer;

public interface IOHandler {
    void addLineListener(Consumer<String> listener);
    void removeLineListener(Consumer<String> listener);
    //void publishLine(String line);
    void writeLine(String text);
    void startInputReader();
    void stopRunning();
    boolean isRunning();
}
