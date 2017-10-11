package com.app.apiserver.services;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.inject.Singleton;

/**
 * a service that converts all types of java objects including collecitons and arrays 
 * 
 * @author smijar
 *
 */
@Singleton
public class AppMapperServiceImpl implements AppMapperService {
	private static ObjectMapper mapper = new ObjectMapper() {
        private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
        = new com.fasterxml.jackson.databind.ObjectMapper();

		public  List readValue(String value, CollectionType typeReference) {
            
            jacksonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            jacksonObjectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
            jacksonObjectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
            jacksonObjectMapper.setSerializationInclusion(Include.NON_NULL);

		    try {
		        return jacksonObjectMapper.readValue(value, typeReference);
		    } catch (IOException e) {
		        throw new RuntimeException(e);
		    }
		}

		public <T> T readValue(String value, Class<T> valueType) {
            
            jacksonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            jacksonObjectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
            jacksonObjectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
            jacksonObjectMapper.setSerializationInclusion(Include.NON_NULL);

		    try {
		        return jacksonObjectMapper.readValue(value, valueType);
		    } catch (IOException e) {
		        throw new RuntimeException(e);
		    }
		}
		
		public String writeValue(Object value) {
            
            jacksonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            jacksonObjectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
            jacksonObjectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
            jacksonObjectMapper.setSerializationInclusion(Include.NON_NULL);

		    try {
		        return jacksonObjectMapper.writeValueAsString(value);
		    } catch (JsonProcessingException e) {
		        throw new RuntimeException(e);
		    }
		};
	};

	@Override
	public ObjectMapper getInstance() {
		// TODO Auto-generated method stub
		return null;
	}
}
