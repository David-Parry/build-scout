package com.davidparry.mcp.buildscout.tools.template;


import java.util.List;

public class SearchParameters {
  private List<String> codeElements;
  private List<String> fileTypes;
  private Integer maxResults = 5;  // Default value

  public List<String> getCodeElements() {
    return codeElements;
  }

  public void setCodeElements(List<String> codeElements) {
    this.codeElements = codeElements;
  }

  public List<String> getFileTypes() {
    return fileTypes;
  }

  public void setFileTypes(List<String> fileTypes) {
    this.fileTypes = fileTypes;
  }

  public Integer getMaxResults() {
    return maxResults;
  }

  public void setMaxResults(Integer maxResults) {
    this.maxResults = maxResults;
  }
}