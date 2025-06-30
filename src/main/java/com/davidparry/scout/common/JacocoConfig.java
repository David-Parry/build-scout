package com.davidparry.scout.common;

public record JacocoConfig(boolean hasJacocoTestReport, boolean xmlRequired, String xmlOutputLocation) {
}
