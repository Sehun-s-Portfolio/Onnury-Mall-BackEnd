package com.onnury.common.util;

import com.onnury.common.base.AbstractVO;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j(topic = "errorLog") // <- Slf4j 어노테이션에 topic 옵션으로 로그 명을 지정해주면 Logback.xml 파일에 이 topic 명과 동일한 이름을 가진 appender와 매칭되어 로깅이 처리된다.
public class LogUtil {
	public enum LOG_TYPE { TRACE, DEBUG, INFO, WARN, ERROR, FATAL }
	
	// {} 형식으로 남긴 로그에서 {}를 arg로 채우고 {}는 지운다.
	private static String getFmtString(String format, Object... arg) {
		if(StringUtil.isNull(format)) {
			return "";
		}
		
		// HttpServletRequest, Exception, AbstractVO 는 별도의 로그형식을 따르므로 skip한다.
		List<Object> listArg = new ArrayList<Object>();
		for(Object obj : arg) {
			if(!(obj instanceof Exception)
					&& !(obj instanceof HttpServletRequest)
					&& !(obj instanceof AbstractVO)
				) {
				listArg.add(obj);
			}
		}

		// {} 표시 안에 arg를 넣어준다.
		String[] arrFormat = format.split("\\{}");
        StringBuilder sb = new StringBuilder();
        for( int i=0; i<arrFormat.length; i++) {
			sb.append(arrFormat[i]);
        	if(i < listArg.size()) {
        		sb.append(listArg.get(i).toString());
        	}
        }

        return sb.toString();
	}

	// msg와 Exception, HttpServletRequest, AbstractVO 클래스를 로그로 남긴다.
	// logType: LOG_TYPE { TRACE, DEBUG, INFO, WARN, ERROR }
	// msg 형식: "msg [{}] {}"	
	private static void logMsg(LOG_TYPE logType, String msg, Object... arg) {
        StringBuilder sb = new StringBuilder();
        // {} 형식으로 남긴 로그에서 {}를 arg로 채우고 {}는 지운다.
        sb.append(getFmtString(msg, arg));
        
        // HttpServletRequest, Exception, AbstractVO 는 별도의 로그형식으로 작성한다.
        for(int i=0; i<arg.length; i++) {
			if(arg[i] instanceof Exception) { // 로그 유형이 예외일 경우
				Exception e = (Exception)arg[i];
				sb.append("\n");
				sb.append(ExceptionUtil.getStackTrace(e));

			} else if(arg[i] instanceof HttpServletRequest) { // 로그 유형이 Servlet일 경우
				HttpServletRequest request = (HttpServletRequest)arg[i]; 
	        	String url = request.getRequestURI();
	        	Map<String, String[]> reqMap = request.getParameterMap();
	        	sb.append("\n[URL] : ").append(url);
	        	sb.append("\n[Parameter] ");
	        	if( reqMap != null && !reqMap.isEmpty()) {
	        		sb.append("\n").append(VoUtil.toJsonFromMapStringArray(reqMap));
	        	} else {
	        		sb.append(": None!!");
	        	}

			} else if(arg[i] instanceof AbstractVO) { // 로그 유형이 객체일 경우 (RequestBody 혹은 Response 객체)
				AbstractVO vo = (AbstractVO)arg[i];
	        	sb.append("\n[VO] ");
				sb.append(arg[i].getClass().getName());
	        	sb.append("\n").append(VoUtil.toJson(vo));

			} else if(arg[i] instanceof HashMap){ // 로그 유현이 HashMap일 경우 (RequestParam)
				@SuppressWarnings("unchecked")
				HashMap<String, String> hm = (HashMap<String, String>) arg[i];
				sb.append("\n[PARAM]\n");
				sb.append("{");
				hm.forEach((key, value) -> sb.append("\n").append(key + " - " + value));
				sb.append("}\n");

			}else if(arg[i] instanceof List){ // 로그 유형이 List 형식의 AbstractVO 객체일 경우
				@SuppressWarnings("unchecked")
				List<AbstractVO> voList = (List<AbstractVO>)arg[i];
				for(AbstractVO vo : voList){
					sb.append("\n[VO] ");
					sb.append(arg[i].getClass().getName());
					sb.append("\n").append(VoUtil.toJson(vo));
				}
			}
		}
        
        if(logType == LOG_TYPE.TRACE) {
        	log.trace(sb.toString());
        } else if(logType == LOG_TYPE.DEBUG) {
        	log.debug(sb.toString());
        } else if(logType == LOG_TYPE.INFO) {
        	log.info(sb.toString());        	
        } else if(logType == LOG_TYPE.WARN) {
        	log.warn(sb.toString());
        } else {
        	log.error(sb.toString());
        }
	}

	private static String setLRightPad( String strContext, int iLen, String strChar ) {
		String strResult;
		StringBuilder sbAddChar = new StringBuilder();
		for( int i = strContext.length(); i < iLen; i++ ) {
			// iLen길이 만큼 strChar문자로 채운다.
			sbAddChar.append( strChar );
		}
		strResult = strContext + sbAddChar; // RPAD이므로, 원래문자열 + 채울문자열로 Concate한다.
		return strResult;
	}
	
	private static String getStringRandom(int num) {
		StringBuilder temp = new StringBuilder();
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

	// Exception, HttpServletRequest, AbstractVO 클래스를 에러 로그로 남긴다.
	public static void logException(Object... arg) {
		StringBuilder sb = new StringBuilder();

		// HttpServletRequest, Exception, AbstractVO 는 별도의 로그형식으로 작성한다.
		for(int i=0; i<arg.length; i++) {
			if(arg[i] instanceof Exception) { // 로그 유형이 예외일 경우
				Exception e = (Exception)arg[i];
				sb.append(ExceptionUtil.getStackTrace(e));

			} else if(arg[i] instanceof HttpServletRequest) { // 로그 유형이 Servlet일 경우
				HttpServletRequest request = (HttpServletRequest)arg[i];
				String url = request.getRequestURI();
				Map<String, String[]> reqMap = request.getParameterMap();
				sb.append("\n[URL] : ").append(url);
				sb.append("\n[Parameter] ");
				if( reqMap != null && !reqMap.isEmpty()) {
					sb.append("\n").append(VoUtil.toJsonFromMapStringArray(reqMap));
				} else {
					sb.append(": None!!");
				}

			} else if(arg[i] instanceof AbstractVO) { // 로그 유형이 객체일 경우 (RequestBody 혹은 Response 객체)
				AbstractVO vo = (AbstractVO)arg[i];
				sb.append("\n[VO] ");
				sb.append(arg[i].getClass().getName());
				sb.append("\n").append(VoUtil.toJson(vo));

			}else if(arg[i] instanceof HashMap){ // 로그 유형이 HashMap일 경우 (RequestParam)
				@SuppressWarnings("unchecked")
				HashMap<String, String> hm = (HashMap<String, String>) arg[i];
				sb.append("\n[PARAM]\n");
				sb.append("{");
				hm.forEach((key, value) -> sb.append("\n").append(key + " - " + value));
				sb.append("}\n");

			}else if(arg[i] instanceof List){ // 로그 유형이 List 형식의 AbstractVO 객체일 경우
				@SuppressWarnings("unchecked")
				List<AbstractVO> voList = (List<AbstractVO>)arg[i];
				for(AbstractVO vo : voList){
					sb.append("\n[VO] ");
					sb.append(arg[i].getClass().getName());
					sb.append("\n").append(VoUtil.toJson(vo));
				}
			}
		}

		log.error(sb.toString());
	}

	// 경고 메시지
	public static void logWarning(String msg, Object... arg) {
		logMsg(LOG_TYPE.WARN, msg, arg);
	}

	// 에러 메시지
	public static void logError(String msg, Object... arg) {
		logMsg(LOG_TYPE.ERROR, msg, arg);
	}

	// 치명적인 에러 메시지
	public static void logFatal(String msg, Object... arg) {
		logMsg(LOG_TYPE.FATAL, msg, arg);
	}

}