package com.onnury.common.util;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class StringUtil {

    public static boolean isEmptyOrNull(String str) {
        if (str != null && !str.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * 설정한 수보다 큰 글자느 자른다.
     * @param strContext
     * @param iLen
     * @return
     */
    public static String setSubStr( String strContext, int iLen) {
		String strResult = strContext;
		
		if(strContext.length() > iLen) {
			strResult = strContext.substring(0,iLen);
		}
		
		return strResult;
	}
    
    /**
     * 글자 길이를 설정하고 여백 왼쪽에 원하는 문자를 붙인다. 0012
     * 
     * @param strContext
     * @param iLen
     * @param strChar
     * @return
     */
    public static String setLeftPad( String strContext, int iLen, String strChar ) {
		String strResult = "";
		StringBuilder sbAddChar = new StringBuilder();
		for( int i = strContext.length(); i < iLen; i++ ) {
            // iLen길이 만큼 strChar문자로 채운다.
            sbAddChar.append( strChar );
		}

		strResult = sbAddChar + strContext; // LPAD이므로, 채울문자열 + 원래문자열로 Concate한다.
		return strResult;
	}
	
	/**
	 * 글자 길이를 설정하고 여백 오른쪽에 원하는 문자를 붙인다. 1200
	 * @param strContext
	 * @param iLen
	 * @param strChar
	 * @return
	 */
    public static String setLRightPad( String strContext, int iLen, String strChar ) {
		String strResult = "";
		StringBuilder sbAddChar = new StringBuilder();
		for( int i = strContext.length(); i < iLen; i++ ) {
            // iLen길이 만큼 strChar문자로 채운다.
            sbAddChar.append( strChar );
		}

		strResult = strContext + sbAddChar; // RPAD이므로, 원래문자열 + 채울문자열로 Concate한다.
		return strResult;
	}
    
    /**
     * 문자열이 null이거나 ""면 true를 반환한다.
     * @param s Check할 문자열
     * @return boolean
     */


    public static boolean isNull(String s) {
        if (s == null) {
            return true;
        }
        String rVal = s.trim();
        return rVal.isEmpty();
    }

    public static boolean isNull(String... ss){
        for (String s : ss){
            if(isNull(s)){
                return true;
            }
        }

        return  false;
    }
    
    public static String refineFolderPath(String folderPath) {
        if (folderPath == null) {
            return "";
        }
        
        return folderPath.replaceAll("\\\\", "/");
    }
	
    public static String padLZero(String value, int length) {
        String ret = "";
        
        if (value == null) {
            return ret;
        }
        
        try {
            value = value.trim();
            
            if (value.length() >= length) {
                return value;
            }
            
            for (int i = 0; i < (length - value.length()); i++) {
                ret += "0";
            }
            
            ret += value;
        } catch(Exception e) {
            ret = "";
            LogUtil.logException(e);
        }

        return ret;
    }
    
    /**
     * null -> empty String, 그 외 trim해서 리턴
     * @param str
     * @return String
     */
    public static String nullToStrAndTrim( String str ){
        if(str == null){
            return "";
        }else{
            return str.trim();
        }
    }
    
    /**
     * null -> initial Value, 그 외 trim해서 리턴
     * @param str
     * @param initValue
     * @return String
     */
    public static String nullToStrAndTrim( String str, String initValue ){
        if(str == null){
            return initValue;
        }else{
            str = str.trim();
            if(str.length() == 0){
                return initValue;
            }else{
                return str;
            }
        }
    }
    
    /**
     * 문자열을 파싱하고, null일 경우 initVale으로 초기 대입한다.
     * @param parameterValue
     * @param initVal
     * @return
     */
    public static String nullToStr(String parameterValue, String initVale ){
        try{
            return ( (parameterValue == null) || (parameterValue.trim().equals("")) )? initVale: parameterValue;
        } catch(NullPointerException e) {
            return "";
        } catch(Exception e) {
            return "";
        }
    }
    
    /**
     * 
     * 배열의 첫번째 문자열을 반환한다. 만약 배열이 null이거나 길이가 0이면 초기값을 반환한다.
     *
     * @param parameterArr
     * @param initVale
     * @return
     *
     */
    public static String nullToStr(String[] parameterArr, String initVale ){
        try{
            if (parameterArr == null 
                    || parameterArr.length == 0) {
                return initVale;
            }
            String parameterValue = parameterArr[0].trim();
            return ((parameterValue == null) || parameterValue.isEmpty()) ? initVale: parameterValue;
        } catch(NullPointerException e) {
            return "";
        } catch(Exception e) {
            return "";
        }
    }
    
    /**
     * 
     * 리스트의 첫번째 문자열을 반환한다. 만약 배열이 null이거나 길이가 0이면 초기값을 반환한다.
     *
     * @param parameterLit
     * @param initVale
     * @return
     *
     */
    public static String nullToStr(List<String> parameterLit, String initVale ){
        try{
            if (parameterLit == null 
                    || parameterLit.size() == 0) {
                return initVale;
            }
            String parameterValue = parameterLit.get(0).trim();
            return ((parameterValue == null) || parameterValue.isEmpty()) ? initVale: parameterValue;
        } catch(NullPointerException e) {
            return "";
        } catch(Exception e) {
            return "";
        }
    }
    
    public static String toCamelCase(String value) {
        String[] arr = value.split("_");
        
        StringBuffer sb = new StringBuffer();
        String str = null;
        for (int i = 0; i < arr.length; i++) {
            str = arr[i];
            if (i > 0) {
                str = str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
            } else {
                str = str.toLowerCase();
            }
            sb.append(str);
        }
        
        return sb.toString();
    }
    
    public static String toSnakeCase(String value) {
        StringBuffer sb = new StringBuffer();
        
        char ch = value.charAt(0);
        sb.append(Character.toUpperCase(ch));
        
        for (int i = 1; i < value.length(); i++) {
            ch = value.charAt(i);
            
            if (Character.isUpperCase(ch)) {
                sb.append("_");
            }
            
            sb.append(Character.toUpperCase(ch));
        }
        
        return sb.toString();
    }
    
    public static String getStringRandom(int num) {
		StringBuilder temp = new StringBuilder();
		//temp.append(Long.parseLong(String.valueOf(System.currentTimeMillis() / 1000L)));

		Random rnd = new Random();

		for (int i = 0; i < num; i++) {
		    int rIndex = rnd.nextInt(3);
		    switch (rIndex) {
		    case 0:
		        // a-z
		        temp.append((char) ((int) (rnd.nextInt(26)) + 97));
		        break;
		    case 1:
		        // A-Z
		        temp.append((char) ((int) (rnd.nextInt(26)) + 65));
		        break;
		    case 2:
		        // 0-9
		        temp.append((rnd.nextInt(10)));
		        break;
		    }
		}

		return temp.toString();
	}
    
    public static String generateUinqueId14(int addRndStrCnt) {
		String uniqueId = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar dateTime = Calendar.getInstance();
		uniqueId = sdf.format(dateTime.getTime());

		return uniqueId+getStringRandom(addRndStrCnt);
	}

    public static String removeExtDot(String ext) {
        String result =ext;
        if (ext.charAt(0) == '.') {
            result = ext.substring(1);
        }
        return result;
    }
    
    // byte -> Hex문자열 변환
    public static String bytesToHex(final byte[] bytes) {
    	StringBuilder sb = new StringBuilder();
    	String hex = null;
    	for(int i=0; i<bytes.length; i++) {
    		hex = Integer.toHexString(0xff & bytes[i]);
    		if(hex.length() == 1) {
    			sb.append("0");
    		} else {
    			sb.append(hex);
    		}
    	}
    	
    	return sb.toString();
    }
    
    // 주어진 문자열을 Hex문자열로 변환
    public static String toHexString(final String str) {
    	StringBuilder sb = new StringBuilder();
    	try {
    		byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        	for(int i=0; i<bytes.length; i++) {
        		sb.append(String.format("%02X", bytes[i] & 0xff));
        	}
    	} catch (Exception e) {
    		LogUtil.logException(e);
    	}
    	
    	return sb.toString();
    }
    
    // Hex문자열 -> 문자열 변환
    public static String toStringFromHexString(final String hexString) {
    	String retString = "";
    	try {   		
    		int len = hexString.length();
    		byte[] data = new byte[len / 2];
    		for(int i=0; i<len; i += 2) {
    			data[i/2] = (byte)((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i+1), 16));
    		}
    		
    		retString = new String(data, StandardCharsets.UTF_8);
    	} catch (Exception e) {
    		LogUtil.logException(e);
    	}

    	return retString;
    }
    
    // XSS 문자열 치환
    public static String replaceXss(final String param) {
    	if(isNull(param)) {
    		return "";
    	}
    	
    	String xss = param;
    	xss = xss.replaceAll("<",  "&lt;");
    	xss = xss.replaceAll(">",  "&gt;");
    	xss = xss.replaceAll("\"",  "&quot;");    	
    	xss = xss.replaceAll("\'",  "&#x27;");
    	xss = xss.replaceAll("\\(",  "&#x40;");
    	xss = xss.replaceAll("\\)",  "&#x41;");
    	xss = xss.replaceAll("\\\\",  "&#x2F;");
    	
    	return xss;
    }

    // String -> int 변환
    public static int StrToInt(String string, int defaultValue) {
        try {
            if (isNull(string))
                return defaultValue;

            return Integer.parseInt(string);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    // int -> String 변환
    public static String IntToStr(int intVal, String defaultValue) {
        try {
            return Integer.toString(intVal);
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

}
