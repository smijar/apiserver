package com.app.apiserver.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class HttpClientSetup2 {
	public static void initialize() throws Exception {
    	KeyStore adminKeyStore  = KeyStore.getInstance("PKCS12");
        FileInputStream instream = new FileInputStream(new File("conf/api.p12"));

        try {
        	adminKeyStore.load(instream, "password".toCharArray());
        } finally {
            instream.close();
        }
        
        // no need to load the truststore at this point because we turn off host cert verification
//        KeyStore trustStore = KeyStore.getInstance("PKCS12");
//        FileInputStream instream2 = new FileInputStream(new File("ca.p12"));
//
//        try {
//        	trustStore.load(instream2, "password".toCharArray());
//        } finally {
//            instream2.close();
//        }

        SSLContext sslcontext = SSLContexts.custom()
	            .loadKeyMaterial(adminKeyStore, "password".toCharArray())
	            //.loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
	            .loadTrustMaterial(null, new TrustStrategy() {

					@Override
					public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType)
							throws java.security.cert.CertificateException {
						return true;
					}
	            })
	            .build();

		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
												sslcontext,
												SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                .setSSLSocketFactory(sslsf)
                .build();

        Unirest.setHttpClient(httpclient);
	}

    private static void sampleCall() throws Exception {

        try {
            //HttpResponse<JsonNode> response = Unirest.get("https://httpbin.org/get?show_env=1").asJson();
            HttpResponse<String> response = Unirest.get("https://localhost/api/v1/cloudinfo/57a53bbbe4b0afc5ac4c38ac").asString();
            System.out.println(response.getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

	public static void main(String[] args) throws Exception {
		initialize();
		sampleCall();
	}
}
