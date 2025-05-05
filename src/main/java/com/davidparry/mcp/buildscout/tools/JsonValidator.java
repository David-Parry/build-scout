package com.davidparry.mcp.buildscout.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A tool for validating and debugging JSON messages in the MCP Explorer.
 * This can help identify issues with malformed JSON that might be causing parsing errors.
 */
public class JsonValidator extends BuildTool {
    private static final Logger logger = LoggerFactory.getLogger(JsonValidator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public CallToolResult handle(Object args) {
        logger.debug("JsonValidator tool called with args: {}", args);
        return validateJson(args);
    }

    @Override
    public String name() {
        return "json_validator";
    }

    @Override
    public String description() {
        return "Validates and debugs JSON messages, helping to identify parsing issues";
    }

    @Override
    public McpSchema.JsonSchema schema() {
        addProperty("jsonString", "string", "The JSON string to validate.", true);
        return new McpSchema.JsonSchema("object", getProperties(), getRequired(), null);
    }

    private CallToolResult validateJson(Object args) {
        List<Content> results = new ArrayList<>();
        boolean error = false;
        try {
            // Extract the JSON string from args
            JsonNode argsNode = objectMapper.valueToTree(args);
            String jsonString = argsNode.has("jsonString") ? argsNode.get("jsonString").asText() : null;
            boolean verbose = argsNode.has("verbose") && argsNode.get("verbose").asBoolean();

            if (jsonString == null || jsonString.trim().isEmpty()) {
                results.add(new TextContent("Error: jsonString parameter is required"));
                return new CallToolResult(results, true);
            }

            // Check for BOM and other common issues
            StringBuilder diagnostics = new StringBuilder();

            // Check for BOM at the beginning
            if (jsonString.startsWith("\uFEFF")) {
                diagnostics.append("- Found Byte Order Mark (BOM) at the beginning of the string\n");
                jsonString = jsonString.substring(1);
            }

            // Check for common JSON syntax issues
            checkCommonIssues(jsonString, diagnostics);

            try {
                // Try to parse the JSON
                JsonNode parsedJson = objectMapper.readTree(jsonString);

                results.add(new TextContent("✅ JSON is valid"));

                if (verbose) {
                    results.add(new TextContent("\nJSON Structure:\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsedJson)));
                }

                if (diagnostics.length() > 0) {
                    results.add(new TextContent("\nDiagnostic Information:\n" + diagnostics));
                }

            } catch (JsonProcessingException e) {
                // JSON parsing failed
                results.add(new TextContent("❌ Invalid JSON: " + e.getMessage()));

                // Add position information
                long position = e.getLocation().getCharOffset();
                int lineNr = e.getLocation().getLineNr();
                int columnNr = e.getLocation().getColumnNr();

                results.add(new TextContent(String.format("Error at position %d (line %d, column %d)", position, lineNr, columnNr)));

                // Show the problematic part of the JSON
                if (position >= 0 && position < jsonString.length()) {
                    long contextStart = Math.max(0, position - 20);
                    long contextEnd = Math.min(jsonString.length(), position + 20);
                    String context = jsonString.substring((int) contextStart, (int) contextEnd);

                    // Mark the error position
                    StringBuilder markedContext = new StringBuilder();
                    markedContext.append(context).append("\n");
                    for (int i = 0; i < (position - contextStart); i++) {
                        markedContext.append(" ");
                    }
                    markedContext.append("^");

                    results.add(new TextContent("\nContext:\n" + markedContext));
                }

                // Add diagnostic information
                if (diagnostics.length() > 0) {
                    results.add(new TextContent("\nDiagnostic Information:\n" + diagnostics));
                }

                // Suggest fixes
                results.add(new TextContent("\nPossible fixes:\n" + suggestFixes(jsonString, e)));
                error = true;
            }

        } catch (Exception e) {
            logger.error("Error in JsonValidator tool", e);
            results.add(new TextContent("Error processing request: " + e.getMessage()));
            error = true;
        }

        return new CallToolResult(results, error);
    }

    private void checkCommonIssues(String jsonString, StringBuilder diagnostics) {
        // Check for unescaped control characters
        for (int i = 0; i < jsonString.length(); i++) {
            char c = jsonString.charAt(i);
            if (c < 32 && c != '\t' && c != '\n' && c != '\r') {
                diagnostics.append(String.format("- Found unescaped control character (ASCII %d) at position %d\n", (int) c, i));
            }
        }

        // Check for unbalanced quotes
        int quoteCount = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < jsonString.length(); i++) {
            char c = jsonString.charAt(i);

            if (c == '\\' && !escaped) {
                escaped = true;
                continue;
            }

            if (c == '"' && !escaped) {
                inString = !inString;
                quoteCount++;
            }

            escaped = false;
        }

        if (quoteCount % 2 != 0) {
            diagnostics.append("- Unbalanced quotes: found odd number of quote characters\n");
        }

        // Check for common syntax errors
        if (jsonString.contains(",]") || jsonString.contains(",}")) {
            diagnostics.append("- Found trailing comma in array or object\n");
        }

        if (jsonString.contains(",,")) {
            diagnostics.append("- Found consecutive commas\n");
        }

        // Check for missing quotes around keys
        if (jsonString.matches(".*[{,]\\s*\\w+\\s*:.*")) {
            diagnostics.append("- Possible missing quotes around object keys\n");
        }
    }

    private String suggestFixes(String jsonString, JsonProcessingException e) {
        StringBuilder suggestions = new StringBuilder();

        // Based on the error message, suggest specific fixes
        String errorMessage = e.getMessage().toLowerCase();

        if (errorMessage.contains("unexpected character")) {
            suggestions.append("- Check for invalid characters in your JSON\n");
            suggestions.append("- Ensure all strings are properly quoted with double quotes\n");
            suggestions.append("- Make sure special characters are properly escaped\n");
        }

        if (errorMessage.contains("unexpected end-of-input")) {
            suggestions.append("- Your JSON appears to be incomplete\n");
            suggestions.append("- Check for missing closing brackets or braces\n");
        }

        if (errorMessage.contains("trailing comma")) {
            suggestions.append("- Remove trailing commas in arrays or objects\n");
        }

        // General suggestions
        suggestions.append("- Use a JSON validator tool like jsonlint.com to check your JSON\n");
        suggestions.append("- Ensure your JSON is properly formatted\n");
        suggestions.append("- Check for invisible characters or BOM markers\n");

        return suggestions.toString();
    }
}