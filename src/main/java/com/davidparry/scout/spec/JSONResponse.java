package com.davidparry.scout.spec;


/**
 * Represents a JSON-RPC 2.0 response.
 */
public class JSONResponse<T> {

    private int  id;

    private String jsonrpc;
    
    private T result;
    
    private Object error;
    
    // Default constructor
    public JSONResponse() {
        this.jsonrpc = "2.0";
    }
    
    /**
     * Constructor with result
     * @param id The request ID
     * @param result The response result
     */
    public JSONResponse(String jsonRpc, int id, T result) {
        this.jsonrpc = jsonRpc;
        this.id = id;
        this.result = result;
        this.error = null;
    }

    // Getters and Setters
    public String getJsonppc() {
        return jsonrpc;
    }
    
    public void setJsonrpc(String jsonRpc) {
        this.jsonrpc = jsonRpc;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public T getResult() {
        return result;
    }
    
    public void setResult(T result) {
        this.result = result;
    }
    
    public Object getError() {
        return error;
    }
    
    public void setError(Object error) {
        this.error = error;
    }
    
    @Override
    public String toString() {
        return "JSONResponse[" +
                "jsonrpc=" + jsonrpc +
                ", id=" + id +
                ", result=" + result +
                ", error=" + error +
                "]";
    }
}
