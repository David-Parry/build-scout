package com.davidparry.scout.common;

import com.davidparry.scout.io.ApplicationLogger;
import com.davidparry.scout.io.LogFileWriter;
import com.davidparry.scout.io.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JacocoGradleAnalyzer {
    private static final String DEFAULT_JACOCO_XML_PATH = "build/reports/jacoco/test/jacocoTestReport.xml";
    private final Logger logger = new ApplicationLogger().getLogger(LogFileWriter.getInstance(new LogFactory()));

    public JacocoConfig analyzeGradleFile(String gradleFilePath) throws IOException {
        String content = Files.readString(Paths.get(gradleFilePath));
        return analyzeGradleContent(content);
    }

    public JacocoConfig analyzeGradleContent(String content) {
        boolean hasJacocoTestReport = false;
        boolean xmlRequired = false;
        String xmlOutputLocation = DEFAULT_JACOCO_XML_PATH;

        // Check if jacocoTestReport block exists
        Pattern jacocoPattern = Pattern.compile("jacocoTestReport\\s*\\{", Pattern.MULTILINE);
        Matcher jacocoMatcher = jacocoPattern.matcher(content);

        if (jacocoMatcher.find()) {
            hasJacocoTestReport = true;

            // Extract the jacocoTestReport block
            String jacocoBlock = extractBlock(content, jacocoMatcher.start());

            // Check for xml.required = true
            Pattern xmlRequiredPattern = Pattern.compile("xml\\.required\\s*=\\s*(true|false)");
            Matcher xmlRequiredMatcher = xmlRequiredPattern.matcher(jacocoBlock);
            if (xmlRequiredMatcher.find()) {
                xmlRequired = Boolean.parseBoolean(xmlRequiredMatcher.group(1));
            }

            // Check for xml.enabled = true (older Gradle versions)
            if (!xmlRequired) {
                Pattern xmlEnabledPattern = Pattern.compile("xml\\.enabled\\s*=\\s*(true|false)");
                Matcher xmlEnabledMatcher = xmlEnabledPattern.matcher(jacocoBlock);
                if (xmlEnabledMatcher.find()) {
                    xmlRequired = Boolean.parseBoolean(xmlEnabledMatcher.group(1));
                }
            }

            // Check for xml.outputLocation
            Pattern xmlOutputPattern = Pattern.compile("xml\\.outputLocation\\s*=\\s*file\\s*\\(\\s*[\"']([^\"']+)[\"']\\s*\\)");
            Matcher xmlOutputMatcher = xmlOutputPattern.matcher(jacocoBlock);
            if (xmlOutputMatcher.find()) {
                xmlOutputLocation = xmlOutputMatcher.group(1);
                // Handle ${buildDir} variable
                xmlOutputLocation = xmlOutputLocation.replace("${buildDir}", "build");
                xmlOutputLocation = xmlOutputLocation.replace("$buildDir", "build");
            } else {
                // Check for xml.destination (older Gradle versions)
                Pattern xmlDestPattern = Pattern.compile("xml\\.destination\\s*(?:=\\s*)?file\\s*\\(\\s*[\"']([^\"']+)[\"']\\s*\\)");
                Matcher xmlDestMatcher = xmlDestPattern.matcher(jacocoBlock);
                if (xmlDestMatcher.find()) {
                    xmlOutputLocation = xmlDestMatcher.group(1);
                    xmlOutputLocation = xmlOutputLocation.replace("${buildDir}", "build");
                    xmlOutputLocation = xmlOutputLocation.replace("$buildDir", "build");
                }
            }
        }

        return new JacocoConfig(hasJacocoTestReport, xmlRequired, xmlOutputLocation);
    }

    private String extractBlock(String content, int startPos) {
        int braceCount = 0;
        int blockStart = -1;
        int blockEnd = -1;

        for (int i = startPos; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                if (braceCount == 0) {
                    blockStart = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    blockEnd = i + 1;
                    break;
                }
            }
        }

        if (blockStart != -1 && blockEnd != -1) {
            return content.substring(blockStart, blockEnd);
        }
        return "";
    }

    public String updateGradleFile(String gradleFilePath) throws IOException {
        String content = Files.readString(Paths.get(gradleFilePath));
        JacocoConfig config = analyzeGradleContent(content);

        if (!config.hasJacocoTestReport()) {
            // Add jacocoTestReport block
            content += "\n\njacocoTestReport {\n    reports {\n        xml.required = true\n    }\n}\n";
        } else if (!config.xmlRequired()) {
            // Update existing block to set xml.required = true
            content = updateXmlRequired(content);
        }

        // Write updated content back to file
        Files.writeString(Paths.get(gradleFilePath), content);

        // Re-analyze to get the final output location
        config = analyzeGradleContent(content);
        return config.xmlOutputLocation();
    }

    private String updateXmlRequired(String content) {
        // First try to update xml.required
        Pattern xmlRequiredPattern = Pattern.compile("(xml\\.required\\s*=\\s*)(false)");
        Matcher xmlRequiredMatcher = xmlRequiredPattern.matcher(content);
        if (xmlRequiredMatcher.find()) {
            return xmlRequiredMatcher.replaceFirst("$1true");
        }

        // Try to update xml.enabled for older versions
        Pattern xmlEnabledPattern = Pattern.compile("(xml\\.enabled\\s*=\\s*)(false)");
        Matcher xmlEnabledMatcher = xmlEnabledPattern.matcher(content);
        if (xmlEnabledMatcher.find()) {
            return xmlEnabledMatcher.replaceFirst("$1true");
        }

        // If no xml configuration exists, add it to the reports block
        Pattern reportsPattern = Pattern.compile("(reports\\s*\\{)([^}]*)(\\})");
        Matcher reportsMatcher = reportsPattern.matcher(content);
        if (reportsMatcher.find()) {
            String reportsBlock = reportsMatcher.group(2);
            if (!reportsBlock.contains("xml.required") && !reportsBlock.contains("xml.enabled")) {
                String updatedReports = reportsMatcher.group(1) + reportsBlock + "\n        xml.required = true\n    " + reportsMatcher.group(3);
                return reportsMatcher.replaceFirst(Matcher.quoteReplacement(updatedReports));
            }
        }

        // If no reports block exists, add it to jacocoTestReport
        Pattern jacocoPattern = Pattern.compile("(jacocoTestReport\\s*\\{)([^}]*)(\\})");
        Matcher jacocoMatcher = jacocoPattern.matcher(content);
        if (jacocoMatcher.find()) {
            String jacocoBlock = jacocoMatcher.group(2);
            if (!jacocoBlock.contains("reports")) {
                String updatedJacoco = jacocoMatcher.group(1) + jacocoBlock + "\n    reports {\n        xml.required = true\n    }\n" + jacocoMatcher.group(3);
                return jacocoMatcher.replaceFirst(Matcher.quoteReplacement(updatedJacoco));
            }
        }

        return content;
    }

}