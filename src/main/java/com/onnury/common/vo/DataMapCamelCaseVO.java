package com.onnury.common.vo;


import com.onnury.common.util.StringUtil;

public class DataMapCamelCaseVO extends DataMapVO{
	private static final long serialVersionUID = 1512568704260345606L;
	
    @Override
    public Object put(String key, Object value) {
        return super.put(StringUtil.toCamelCase(key), value);
    }
}
