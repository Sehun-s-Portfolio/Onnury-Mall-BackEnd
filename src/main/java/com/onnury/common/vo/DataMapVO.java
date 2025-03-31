package com.onnury.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataMapVO extends HashMap<String, Object> {	
    private static final long serialVersionUID = 2813468004260345606L;
    
	ObjectMapper json = new ObjectMapper();

    public DataMapVO() {

    }

    public String getString(String key) {
        return (String) super.get(key);
    }
    public Integer getInt(String key) {
        return (Integer) super.get(key);
    }

    @JsonIgnore
    public String toJsonString() throws JsonProcessingException{
        return json.writeValueAsString(this);
    }
}
