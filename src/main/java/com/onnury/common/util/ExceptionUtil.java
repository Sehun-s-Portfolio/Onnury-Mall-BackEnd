package com.onnury.common.util;


import com.onnury.common.exception.AppException;

public class ExceptionUtil {

    public static String getStackTrace(Exception e) {
        StackTraceElement[] elemArr = e.getStackTrace();
        StringBuilder sb = new StringBuilder();

        sb.append("[Exception] ");
        if (e instanceof AppException) {
            sb.append("[").append(((AppException) e).getEnumError().getErrCode()).append("] ");
        }
        sb.append(e.getMessage());

        for (StackTraceElement elem : elemArr) {
            sb.append("\n");
            sb.append(elem.getClassName()).append(".").append(elem.getMethodName()).append(" [").append(elem.getLineNumber()).append("]");
        }
        return sb.toString();
    }
    
    public static String getOneLineStackTrace(Exception e) {
    	StackTraceElement[] elemArr = e.getStackTrace();
        StringBuilder sb = new StringBuilder();

        sb.append("1_line_err_msg : ");
        if (e instanceof AppException) {
            sb.append("[").append(((AppException) e).getEnumError().getErrCode()).append("]");
        } else {
            sb.append("[").append(e.getClass().getSimpleName()).append("]");
        }

        sb.append(e.getMessage());
        sb.append(" [S : Exception Log] : ");
        for (StackTraceElement elem : elemArr) {
            sb.append(" ");
            sb.append(elem.getClassName()).append(".").append(elem.getMethodName()).append(" [").append(elem.getLineNumber()).append("]");
        }
        sb.append(" [E : Exception Log]");
        
        return sb.toString();
    }
	
}
