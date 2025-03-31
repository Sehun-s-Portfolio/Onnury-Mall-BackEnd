package com.onnury.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;
import java.net.*;

@Slf4j
public class WasUtil {

    // S : WAS Server Information.
    public static final String WAS_UNKNOWN = "UNKNOWN";
    
    /**
     * WAS Tomcat<br>
     * [참고] Apache Tomcat/8.0.53
     */
    public static final String WAS_TOMCAT = "TOMCAT";
    
    /**
     * WAS WebSphere<br>
     * [참고] IBM WebSphere Application Server/9.0.0.11
     */
    public static final String WAS_WEBSPHERE = "WEBSPHERE";

    /**
     * WAS JEUS<br>
     * [참고] JEUS 8 Fix#1
     */
    public static final String WAS_JEUS = "JEUS";
    
    /**
     * Web Logic<br>
     * [참고] Web Logic 14c
     */
    public static final String WAS_WEBLOGIC = "WEBLOGIC";
    // E : WAS Server Information.
	
    public static String getServerInfo(ServletContext servletContext) {
        String serverInfo = servletContext.getServerInfo().toUpperCase();

        if (serverInfo.indexOf(WAS_TOMCAT) >= 0) {
            return WAS_TOMCAT;
        } else if (serverInfo.indexOf(WAS_WEBSPHERE) >= 0) {
            return WAS_WEBSPHERE;
        } else if (serverInfo.indexOf(WAS_JEUS) >= 0) {
            return WAS_JEUS;
        } else if (serverInfo.indexOf(WAS_WEBLOGIC) >= 0) {
            return WAS_WEBLOGIC;
        }
        
        return WAS_UNKNOWN;
    }
    
    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        
        if (log.isDebugEnabled()) {
        	log.debug("[OS] : " + os);
        }
        
        if (!StringUtil.isNull(os)
                && os.toLowerCase().indexOf("windows") >= 0
                ) {
            return true;
        }
        
        return false;
    }

    /*
    public static String getServerName(ServletContext context) {
        String serverNm = StringUtil.nullToStr(System.getProperty("edms.srv.nm"), "");
        if (StringUtil.isNull(serverNm)) {
            try {
                String env = Core.getEnv();
                String serverIp = getServerIp();
                
                String[] split = serverIp.split("\\.");                
                serverNm = env + split[split.length -1];
            } catch(Exception e) {
                LogUtil.logException(e);
                serverNm = "";
            }
        }
        
        return serverNm;
    }
    */

    public static String getServerIp() {
        String serverIp = "";
        try {
            Inet4Address inetAddres = (Inet4Address) Inet4Address.getLocalHost();
            serverIp = inetAddres.getHostAddress();
        } catch (UnknownHostException e) {
            serverIp = "";
            LogUtil.logException(e);
        }
        
        return serverIp;
    }
    
    public static String getMacAddr() {
        String macAddr = null;
        
        try {
            InetAddress inetAddr = InetAddress.getLocalHost();
            
            NetworkInterface ni = NetworkInterface.getByInetAddress(inetAddr);
            if (ni != null) {
                byte[] mac = ni.getHardwareAddress();
                
                macAddr = "";
                for (int i = 0; i < mac.length; i++) {
                    macAddr += String.format("%02X", mac[i]);
                }
            }
        } catch (SocketException e) {
            LogUtil.logException(e);
            macAddr = "";
        } catch (UnknownHostException e) {
            LogUtil.logException(e);
            macAddr = "";
        } catch (Exception e) {
            LogUtil.logException(e);
            macAddr = "";
        }
        
        return macAddr;
    }

    /*
    public static String getTimestamp() {
        
        long tid = Thread.currentThread().getId();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.KOREA);
        StringBuffer sb = new StringBuffer();
        sb.append(Core.getServerName());
        sb.append("_");
        // 너무 길어서 그냥 넣는다.
        // sb.append(StringUtil.padLZero(tid + "", 20));
        sb.append(Long.toString(tid));
        sb.append("_");
        sb.append(sdf.format(new Date()));
        
        return sb.toString();
    }

	public static void setTrscId(final HttpServletRequest request) {
		///// 트랜잭션ID: 업무구분코드(3) + ip주소(12) + 전문생성일시 + 랜덤번호(4)
		TrscVO trscVO = Core.getTrsc();
		StringBuilder sb = new StringBuilder();
		
		///// 업무구분코드(3)
		sb.append("IMA");
		
		///// IP주소(12)
		// 로컬호스트는 0:0:0:0:0:0:1 형태로 온다.
		// 클라이언트에서 전송한 ip주소를 넣어준다.
		String userIp = request.getParameter("userIpAddr");
		if( StringUtil.isNull(userIp)) {
			// 클라이언트에서 오는 패킷이 아니면 trscVO의 IP를 넣어준다.
			userIp = trscVO.getUserIpAddr();
			if( StringUtil.isNull(userIp)) {
				// 그래도 없으면 가상의 IP를 넣어준다.
				userIp = "123123123123";
			}
		}
		
		String strBuf;
		String[] arrUserIp = userIp.split("[:\\.]");
		int nSize = arrUserIp.length;
		// ESB에서 받은 것은 123123123123 형식으로 온다.
		if(nSize == 1) {
			if(userIp.length() > 12) {
				userIp = userIp.substring(0, 11);
			}
			
			sb.append(userIp);
		} else {
			// IP4, IP6형태의 IP주소를 잘라서 12자리로 만들어준다.
			int intBuf = 0;
			for(int i=0; i<nSize; i++) {
				strBuf = arrUserIp[i];
				// 숫자인지 체크
				if(strBuf.chars().allMatch(Character::isDigit)) {
					intBuf = Integer.parseInt(strBuf);
					userIp = String.format("%03d",  intBuf);
				} else {
					// 숫자가 아니고 3자리보다 크면 3자리로 자른다.
					if(strBuf.length() > 3) {
						strBuf = strBuf.substring(0, 2);
					}
					
					userIp = strBuf;
				}
				
				sb.append(strBuf);
				// 0.0.0.0의 형식이므로 3이면 나간다.
				if(i==3) {
					break;
				}
			}
		}
		
		///// 전문생성일시(17)
		Date timeTr = new Date();
		SimpleDateFormat sdfTr = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.KOREA);
		sb.append(sdfTr.format(timeTr));
		
		///// 랜덤번호(4)
		SecureRandom secRandom = new SecureRandom();
		int random = secRandom.nextInt(9999)+1;
		sb.append(String.format("%04d", random));
		
		trscVO.setTrscId(sb.toString());;
		Core.setTrsc(trscVO);
	}
     */
}
