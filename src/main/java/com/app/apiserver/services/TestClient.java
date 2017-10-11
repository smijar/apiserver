package com.app.apiserver.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

/**
 * This example demonstrates how to create secure connections with a custom SSL
 * context.
 */
public class TestClient {
	//private static String url = "https://localhost/api/v1/serverinfo/uuid";
	private static String url = "https://hypmgmt.srvdev.droidcloud.mobi/api/v1/serverinfo/uuid";

    public final static void main(String[] args) throws Exception {
        //KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
    	KeyStore trustStore  = KeyStore.getInstance("PKCS12");

        //FileInputStream instream = new FileInputStream(new File("client.p12"));
    	FileInputStream instream = new FileInputStream(new File("conf/api.p12"));
        try {
            //trustStore.load(instream, "password123".toCharArray());
        	trustStore.load(instream, "password".toCharArray());
        } finally {
            instream.close();
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
        		.useTLS()
        		.loadKeyMaterial(trustStore, "password".toCharArray())
                .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                .build();

        // Allow both TLSv1, TLSv2 protocols
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1", "TLSv1.2" },
                null,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();

        // with plain java
        //plainJavaCall(httpclient);

        // with unirest
        unirestCall(httpclient);
    }

    static void unirestCall(CloseableHttpClient httpclient) throws Exception {
        Unirest.setHttpClient(httpclient);
        //HttpResponse<String> output = Unirest.get("https://hypmgmt.srvdev.droidcloud.mobi/api/v1/serverinfo/uuid").asString();
        HttpResponse<String> output = Unirest.get(url).asString();
        System.out.println(output.getBody().toString());
    }

    static void plainJavaCall(CloseableHttpClient httpclient) throws Exception {
        try {

            HttpGet httpget = new HttpGet(url);

            System.out.println("executing request" + httpget.getRequestLine());

            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                HttpEntity entity = response.getEntity();

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (entity != null) {
                    System.out.println("Response content length: " + entity.getContentLength());
                    byte[] buffer = new byte[(int) entity.getContentLength()];
                    StringWriter sw = new StringWriter();
                    System.out.println("Response content:" + IOUtils.toString(entity.getContent()));
                }
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

}