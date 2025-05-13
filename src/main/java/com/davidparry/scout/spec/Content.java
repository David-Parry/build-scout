package com.davidparry.scout.spec;

import java.util.List;

public record Content(List<Role> audience, Double priority, String text, String type) {
    /**
     * Constructor that only takes the text parameter.
     * Sets audience to null and priority to null.
     *
     * @param text the content text
     */
    public Content(String text) {
        this(null, null, text, "text");
    }
}
