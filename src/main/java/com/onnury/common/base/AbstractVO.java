package com.onnury.common.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnury.common.util.LogUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper=false)
public class AbstractVO {
    protected String rowNum;    // 조회 완료 시 row number
    protected String rowCnt;    // 조회 완료 시 row count

    public String toJsonString() {
        try {
            ObjectMapper json = new ObjectMapper();
            return json.writeValueAsString(this);
        } catch (Exception ex) {
            LogUtil.logException(ex);
        }

        return null;
    }

    @JsonIgnore
    public <T> T JsonStringToObj(String jsonString, Class<T> cls) throws JsonProcessingException {
        ObjectMapper json = new ObjectMapper();
        return json.readValue(jsonString, cls);
    }

    @JsonIgnore
    public Object getField(String name) {
        Object obj;
        try {
            Field field = this.getClass().getDeclaredField(name);
            obj = field.get(this);
        } catch (Exception ex) {
            obj = null;
            LogUtil.logException(ex);
        }

        return obj;
    }

    @JsonIgnore
    public void setField(String name, Object value) {
        try {
            Field field = this.getClass().getDeclaredField(name);
            field.set(this, value);
        } catch (Exception e) {
            LogUtil.logException(e);
        }
    }

    @JsonIgnore
    public <T extends AbstractVO> void copyAllField(T obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            String name = field.getName();
            this.setField(name, obj.getField(name));
        }
    }
}
