package com.app.apiserver.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

import com.app.apiserver.core.AppConfiguration;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import okhttp3.OkHttpClient;

/**
 * sets up the http client for both management and prospectsApp
 * 
 * @author smijar
 */
public class HttpClientSetup {
	//private static Logger logger = LoggerFactory.getLogger(HttpClientSetup.class);
	private static CloseableHttpClient mgmtHttpClient;
	private static okhttp3.OkHttpClient externalHttpClient;
	
	/**
	 * management http client
	 */
	public static HttpClient getMgmtHttpClient() {
		return mgmtHttpClient;
	}
	
	private static void setMgmtHttpClient(CloseableHttpClient httpClient) {
		mgmtHttpClient = httpClient;
	}
	
	/**
	 * external http client
	 */
	public static OkHttpClient getExternalHttpClient() {
		if(externalHttpClient == null) {
		    HttpClientSetup.externalHttpClient = new OkHttpClient.
    													Builder().
    													connectTimeout(120, TimeUnit.SECONDS).
    													readTimeout(120, TimeUnit.SECONDS).
    													writeTimeout(120, TimeUnit.SECONDS).build();
		    
	        return externalHttpClient;
		} else {
			return externalHttpClient;
		}
	}
	
	private static void setupMapper() {
        Unirest.setObjectMapper(new ObjectMapper() {  
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
            = new com.fasterxml.jackson.databind.ObjectMapper();

			@SuppressWarnings("unused")
			public  List<?> readValue(String value, CollectionType typeReference) {
	            
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
			}
		});
	}

	@SuppressWarnings("deprecation")
	public static void initialize(AppConfiguration appConfig) throws Exception {
		String apiCert = "conf/api.p12";
		String apiCertPassword = "password";
		
		if(appConfig != null)  {
			if(appConfig.getMgmtServersConfig().getApiCertificate()!=null) {
				apiCert = appConfig.getMgmtServersConfig().getApiCertificate();
				apiCertPassword = appConfig.getMgmtServersConfig().getApiCertificatePassword();
			}
		}

//    	String apiBasicAuthUsername = appConfig.getMgmtServersConfig().getApiBasicAuthUsername();
//    	String apiBasicAuthPassword = appConfig.getMgmtServersConfig().getApiBasicAuthPassword();

		KeyStore apiKeyStore  = KeyStore.getInstance("PKCS12");
        FileInputStream instream = new FileInputStream(new File(apiCert));

        try {
        	apiKeyStore.load(instream, apiCertPassword.toCharArray());
        } catch(Exception e) {
        	e.printStackTrace();
        } finally {
            instream.close();
        }

		SSLContext sslcontext = SSLContexts.custom()
	            .loadKeyMaterial(apiKeyStore, apiCertPassword.toCharArray())
                //.loadTrustMaterial(apiKeyStore, new TrustSelfSignedStrategy())
                .loadTrustMaterial(apiKeyStore, new TrustStrategy() {
					
					@Override
					public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						// TODO Auto-generated method stub
						return true;
					}
				})
	            .build();

        // Allow both TLSv1, TLSv2 protocols
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1", "TLSv1.2" },
                null,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        int timeout = 60;
        RequestConfig requestConfig = RequestConfig.custom()
							       .setConnectTimeout(timeout * 1000)
							       .setConnectionRequestTimeout(timeout * 1000)
							       .setSocketTimeout(timeout * 1000).build();

        CloseableHttpClient httpClient = HttpClients.custom()
							                .setSSLSocketFactory(sslsf)
							                .setDefaultRequestConfig(requestConfig)
							                .build();

        HttpClientSetup.externalHttpClient = getExternalHttpClient();
        
        HttpClientSetup.setupMapper();

        HttpClientSetup.setMgmtHttpClient(httpClient);
        Unirest.setHttpClient(HttpClientSetup.getMgmtHttpClient());
	}

    private static void sampleCall() throws Exception {

        try {
            //HttpResponse<JsonNode> response = Unirest.get("https://httpbin.org/get?show_env=1").asJson();
        	//HttpResponse<String> response = Unirest.get("https://localhost/api/v1/serverinfo/uuid").asString();
        	HttpResponse<String> response = Unirest.get("https://mgmt.dev.droidcloud.mobi/api/v1/serverinfo/uuid").asString();
        	//HttpResponse<String> response = Unirest.get("https://testmgmt.app.com//api/v1/serverinfo/uuid").asString();
            System.out.println(response.getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

	public static void main(String[] args) throws Exception {
		initialize(null);
		sampleCall();
	}
}
