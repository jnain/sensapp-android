package net.modelbased.sensapp.android.sensappdroid.restservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class RestRequest {
	
	private static final String TAG = RestRequest.class.getName(); 
	private static final String PROTOCOLE = "http";
	private static final String SENSOR_PATH = "/registry/sensors";
	private static final String DISPATCHER_PATH = "/dispatch";
	
	public static String postSensor(String server, int port, String content) {
		Log.i(TAG, "POST Sensor");
		Log.v(TAG, "Content: " + content);
		URI target = getURITarget(server, port, SENSOR_PATH);
		Log.v(TAG, "Target: " + target.toString());
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(target);
		request.setHeader("Content-type", "application/json");
		String response = null;
		try {
			StringEntity seContent = new StringEntity(content);
			seContent.setContentType("text/json");  
			request.setEntity(seContent);  
			response = resolveResponse(client.execute(request));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.e(TAG, "Post sensor result: " + response);
		return response; 
	}
	
	public static String putData(String server, int port, String data) {
		Log.i(TAG, "PUT Data");
		Log.v(TAG, "Content: " + data);
		URI target = getURITarget(server, port, DISPATCHER_PATH);
		Log.v(TAG, "Target: " + target.toString());
		HttpClient client = new DefaultHttpClient();
		HttpPut request = new HttpPut(target);
		request.setHeader("Content-type", "application/json");
		String response = null;
		try {
			StringEntity seContent = new StringEntity(data);
			seContent.setContentType("text/json");  
			request.setEntity(seContent);  
			response = resolveResponse(client.execute(request));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.e(TAG, "Put data result: " + response);
		return response; 
	}
	
	private static URI getURITarget(String server, int port, String path) {
		URI target = null;
		try {
			target = new URI(PROTOCOLE, null, server, port, path, null, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return target;
	}
	
	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	private static String resolveResponse(HttpResponse response) {
		StatusLine status = response.getStatusLine();
		if (status.getStatusCode() != 200) {
			try {
				throw new IOException("Invalid response from server: " + status.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		HttpEntity entity = response.getEntity();
		InputStream inputStream = null;
		String result = null;
		try {
			inputStream = entity.getContent();
			result = convertStreamToString(inputStream);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
