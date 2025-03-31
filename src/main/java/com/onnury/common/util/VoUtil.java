package com.onnury.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.onnury.common.base.AbstractVO;
import com.onnury.common.vo.DataMapVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.MultilineRecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.BeanUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.stream.FactoryConfigurationError;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public final class VoUtil {
	private VoUtil() {
		// static 클래스이므로 생성자 사용을 금함
	}
	
	// AbstractVO를 서식화된 문자열로 반환
	public static String toString(final AbstractVO vo) {
		if(vo==null) {
			return "";
		}
		
		return ReflectionToStringBuilder.toString(vo, new MultilineRecursiveToStringStyle(), false, false);
	}
	
	// DataMapVO를 서식화된 문자열로 반환
	public static String toString(final DataMapVO vo) {
		if(vo==null) {
			return "";
		}
		
		return ReflectionToStringBuilder.toString(vo, new MultilineRecursiveToStringStyle(), false, false);
	}
	
	// AbstractVO 리스트를 서식화된 문자열로 반환
	public static String toString(final List<? extends AbstractVO> lit) {
		if(lit == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
        for (AbstractVO abstractVO : lit) {
            if (sb.length() > 0) {
                sb.append(",\n");
            }

            sb.append(ReflectionToStringBuilder.toString(abstractVO, new MultilineRecursiveToStringStyle(), false, false));
        }
		
		return sb.toString();
	}
	
	// AbstractVO를 JSON 문자열로 반환
	public static String toJson(final AbstractVO vo) {
		return toJson(vo, true);
	}
	
	public static String toJson(final AbstractVO vo, final boolean isFormatting) {
		String jsonString = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			// 서식화
			if(isFormatting) {
				mapper.enable(SerializationFeature.INDENT_OUTPUT);
			}
			
			jsonString = mapper.writeValueAsString(vo);
		} catch (Exception e) {
			LogUtil.logException(e, vo);
			return jsonString;
		}
		
		return jsonString;
	}

	// Map<String, Sring>을 JSON 문자열로 반환
	public static String StrMapToJson(final Map<String, String> map) {
		Map<String,Object> objMap = map.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> (Object)e.getValue()));
		return toJson(objMap, true);
	}

	// Map<String, Object>를 JSON 문자열로 반환
	public static String toJson(final Map<String, Object> map) {
		return toJson(map, true);
	}
	
	public static String toJson(final Map<String, Object> map, final boolean isFormatting) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "";
		try {
			if(isFormatting) {			
				mapper.enable(SerializationFeature.INDENT_OUTPUT);
			}
			
			jsonString = mapper.writeValueAsString(map);
		} catch (Exception e) {
			LogUtil.logException(e);
			return jsonString;
		}

		return jsonString;		
	}

	// Map<String, String[]>를 JSON 문자열로 반환
	public static String toJsonFromMapStringArray(final Map<String, String[]> arrMap) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "";
		try {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			jsonString = mapper.writeValueAsString(arrMap);
		} catch (Exception e) {
			LogUtil.logException(e);
			return jsonString;
		}

		return jsonString;		
	}

	// Map<String, String>를 JSON 문자열로 반환	
	public static String toJsonFromMapString(final Map<String, String> map) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "";
		try {
			jsonString = mapper.writeValueAsString(map);
		} catch (Exception e) {
			LogUtil.logException(e);
			return jsonString;
		}

		return jsonString;		
	}

	// JSON 문자열을 clazz의 인스턴스 VO로 반환
	// ※ null 체크 필수
	public static <T> T fromJson2(final String jsonInString, final Class<T> clazz) {
		ObjectMapper mapper = new ObjectMapper();
		T retVO = null;
		
		try {
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			retVO = mapper.readValue(jsonInString, clazz);
		} catch (Exception e) {
			LogUtil.logException(e);
			return null;
		}
		
		return retVO;
	}
	
	// JSON 문자열을 clazz의 인스턴스 VO로 반환
	// ※ null 체크 필수
	public static <T extends AbstractVO> T fromJson(final String jsonInString, final Class<T> clazz) {
		ObjectMapper mapper = new ObjectMapper();
		T retVO = null;
		
		try {
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			retVO = mapper.readValue(jsonInString, clazz);
		} catch (Exception e) {
			LogUtil.logException(e);
			return null;
		}
		
		return retVO;
	}
	
	// JSON 문자열 리스트를 clazz의 인스턴스 VO로 반환
	// ※ null 체크 필수
	public static <T extends AbstractVO> List<T> fromJson(final String[] arrJson, final Class<T> clazz) {
		if(arrJson == null || arrJson.length == 0) {
			return null;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		ArrayList<T> voLit = new ArrayList<T>();
		T tempVO = null;
		for(int i=0; i<arrJson.length; i++) {
			try {
				tempVO = mapper.readValue(arrJson[i], clazz);
				voLit.add(tempVO);
			} catch (Exception e) {
				LogUtil.logException(e);
				return null;
			}
		}
		
		return voLit;
	}

	// JSON 문자열을 Map<String, Object>로 반환
	public static Map<String, Object> fromJson(final String strJson) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			map = mapper.readValue(strJson, new TypeReference<Map<String, Object>>(){});
		} catch (Exception e) {
			LogUtil.logException(e);
			return map;
		}
		
		return map;
	}

	// VO를 XML 문자열로 반환
	public static String toXmlByJaxb(final AbstractVO vo) {
		String strXml = "";

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(vo.getClass());
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter sw = new StringWriter();
			jaxbMarshaller.marshal(vo, sw);
			strXml = sw.toString();
		} catch (FactoryConfigurationError e) {
			log.error("toXmlByJaxb exception. {}", e.toString());
			return strXml;
		} catch (Exception e) {
			LogUtil.logException(e);
			return strXml;
		}
		
		return strXml;
	}
	
	// VO를 XML 문자열로 반환(지정된 클래스)
	public static String toXmlByJaxb(final AbstractVO vo, final Class<?> refClazz) {
		String strXml = "";

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(vo.getClass(), refClazz);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter sw = new StringWriter();
			jaxbMarshaller.marshal(vo, sw);
			strXml = sw.toString();
		} catch (FactoryConfigurationError e) {
			log.error("toXmlByJaxb exception. {}", e.toString());
			return strXml;
		} catch (Exception e) {
			LogUtil.logException(e);
			return strXml;
		}
		
		return strXml;
	}

	// XML을 JSON으로 변환.
	public static String xmlToJson(String xml) {
		JSONObject json = XML.toJSONObject(xml);
		return json.toString();
	}

	// AbstractVO를 clazz 인스턴스 VO로 복사
	// ※ null 체크 필수
	public static <T extends AbstractVO> T copy(final AbstractVO srcVO, final Class<T> clazz) {
		ObjectMapper mapper = new ObjectMapper();
		String strJson = "";
		T retVO = null;
		
		try {
			strJson = mapper.writeValueAsString(srcVO);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			
			retVO = mapper.readValue(strJson, clazz);
		} catch (Exception e) {
			LogUtil.logException(e);
			return null;
		}
		
		return retVO;
	}

	// DataMapVO를 clazz 인스턴스 VO로 복사
	// ※ null 체크 필수
	public static <T extends AbstractVO> T copy(final DataMapVO srcVO, final Class<T> clazz) {
		ObjectMapper mapper = new ObjectMapper();
		String strJson = "";
		T retVO = null;
		
		try {
			strJson = mapper.writeValueAsString(srcVO);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			
			retVO = mapper.readValue(strJson, clazz);
		} catch (Exception e) {
			LogUtil.logException(e);
			return null;
		}
		
		return retVO;
	}

	// Map<String, Object>를 clazz 인스턴스 VO로 복사
	// ※ null 체크 필수
	public static <T extends AbstractVO> T copy(final Map<String, Object> srcVO, final Class<T> clazz) {
		ObjectMapper mapper = new ObjectMapper();
		String strJson = "";
		T retVO = null;
		
		try {
			strJson = mapper.writeValueAsString(srcVO);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			
			retVO = mapper.readValue(strJson, clazz);
		} catch (Exception e) {
			LogUtil.logException(e);
			return null;
		}
		
		return retVO;
	}

	// 주어진 VO를 DataMapVO 인스턴스로 복사
	// ※ null 체크 필수
	public static DataMapVO copy(final AbstractVO srcVO) {
		ObjectMapper mapper = new ObjectMapper();
		String strJson = "";
		DataMapVO retVO = null;
		
		try {
			strJson = mapper.writeValueAsString(srcVO);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			
			retVO = mapper.readValue(strJson, new TypeReference<DataMapVO>(){});
		} catch (Exception e) {
			LogUtil.logException(e);
			return null;
		}
		
		return retVO;
	}
	
	// 주어진 DataMapVO를 DataMapVO 인스턴스로 복사
	// ※ null 체크 필수
	public static DataMapVO copy(final DataMapVO srcVO) {
		ObjectMapper mapper = new ObjectMapper();
		String strJson = "";
		DataMapVO retVO = null;
		
		try {
			strJson = mapper.writeValueAsString(srcVO);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			
			retVO = mapper.readValue(strJson, new TypeReference<DataMapVO>(){});
		} catch (Exception e) {
			LogUtil.logException(e);
			return null;
		}
		
		return retVO;
	}

	// 주어진 Map<String, Object>를 DataMapVO 인스턴스로 복사
	// ※ null 체크 필수
	public static DataMapVO copy(final Map<String, Object> srcVO) {
		ObjectMapper mapper = new ObjectMapper();
		String strJson = "";
		DataMapVO retVO = null;
		
		try {
			strJson = mapper.writeValueAsString(srcVO);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			
			retVO = mapper.readValue(strJson, new TypeReference<DataMapVO>(){});
		} catch (Exception e) {
			LogUtil.logException(e);
			return null;
		}
		
		return retVO;
	}
	
	// srcVO를 targetVO와 병합.
	public static void merge(final AbstractVO srcVO, final AbstractVO targetVO) {
		if(srcVO == null || targetVO == null) {
			return;
		}
		
		BeanUtils.copyProperties(srcVO, targetVO);
	}

	public static Map<String, String> parseURLParams(String url) {
		Map<String, String> params = new HashMap<>();
		String[] pairs = url.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			if (idx != -1) {
				String key = pair.substring(0, idx);
				String value = pair.substring(idx + 1);
				params.put(key, value);
			}
		}
		return params;
	}
}