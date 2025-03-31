package com.davidparry.mcp.buildscout.tools.template;


import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeSearchRequest {
  @JsonProperty(required = true)
  private String query;
  private String codeContext;
  private String path;
  private SearchParameters searchParameters;

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getCodeContext() {
    return codeContext;
  }

  public void setCodeContext(String codeContext) {
    this.codeContext = codeContext;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public SearchParameters getSearchParameters() {
    return searchParameters;
  }

  public void setSearchParameters(SearchParameters searchParameters) {
    this.searchParameters = searchParameters;
  }
}

