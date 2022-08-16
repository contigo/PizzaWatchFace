package com.example.watchApp.pizzawatchface.http;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.example.watchApp.pizzawatchface.util.FileUtils;
import com.example.watchApp.pizzawatchface.util.FormatUtils;
import com.example.watchApp.pizzawatchface.util.TaskUtils;
import com.example.watchApp.pizzawatchface.util.UniqueUtils;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class HttpUtils {

    private static final String TAG = "[http-util] ";

    private static final String CRLF = "\r\n";
    private static final String TWO_HYPHENS = "--";
    private static final String BOUNDARY = "*****";

	public static final String DEFAULT_ENCODING = "utf-8";
	public static final int DEFAULT_CONNECT_TIMEOUT = 30;
	public static final int DEFAULT_READ_TIMEOUT = 60;

    private static final boolean DEBUG = false;

    public enum RequestBody{
        HTML,
		JSON
	}

	public enum ResponseBody{
		JSON,
        GSON,
        TEXT,
        JSON_READER,
        BINARY,
        STREAM
	}
	public enum Method{
		GET, POST
	}
	
	private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{
		
		new X509TrustManager() {
			
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) 
					throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
			}
	}};
	
	public static void trustAllCerts() {

		 try{
			 SSLContext ctx = SSLContext.getInstance("TLS");
			 ctx.init(null, TRUST_ALL_CERTS, new java.security.SecureRandom());
			 HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
			 HostnameVerifier hv = (urlHostName, session)->true;
			 HttpsURLConnection.setDefaultHostnameVerifier(hv);
		 }
		 catch(Exception e){
             Log.e(TAG, e.getMessage());
		 }
	 }
	
	public interface HttpResponseListener{
		
		void onSuccess(HttpRequest req, Object result);
		void onFailure(HttpRequest req);
	}

	public static String buildQueryString(Map<String,String> parameter) {
		
		StringBuilder sb = new StringBuilder();
		
		try{
			Iterator<Map.Entry<String, String>> it = parameter.entrySet().iterator();			
			while(it.hasNext()){
				Map.Entry<String, String> entry = it.next();
				String key = entry.getKey();
				if(key != null){
					sb.append(URLEncoder.encode(key, DEFAULT_ENCODING));
					sb.append("=");
					String value = entry.getValue()!=null?URLEncoder.encode(entry.getValue(), DEFAULT_ENCODING):"";
					sb.append(value);
					if(it.hasNext()) sb.append("&");
				}
			}			
	    }
		catch(UnsupportedEncodingException e){
			 Log.e(TAG, e.getMessage());
		}
		
		return sb.toString();
	}
	
	private static void storeCookie(Context context, HttpURLConnection conn){
		
		CookieManager cookieManager = CookieManager.getInstance();		
		List<String> cookieList = conn.getHeaderFields().get("Set-Cookie");
	    if (cookieList != null) {
	        for (String cookieTemp : cookieList) {
	            cookieManager.setCookie(conn.getURL().toString(), cookieTemp);
	        }
	    }
	}
	
	private static void setCookie(Context context, HttpURLConnection conn){
		 
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
			 CookieSyncManager.createInstance(context);
			 CookieSyncManager.getInstance().sync();
		}
		
		CookieManager cookieManager = CookieManager.getInstance();
		String cookie = cookieManager.getCookie(conn.getURL().toString());
	    if(cookie != null){

	        if(false)
	    	     Log.d(TAG,TAG+"set cookie >> "+cookie);

	        conn.setRequestProperty("Cookie", cookie);
	    }
	}

	public static boolean deleteMultipartDataFile(Context context, int fileType, String filePath){

	    File file = null;
        if(fileType == HttpMultipartData.FILE_TYPE_PRIVATE){
            //file = context.getFileStreamPath(multipartData.getFilePath());
            //fis = context.openFileInput(multipartData.getFilePath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                file = new File(FileUtils.commonDocumentDirPath(""), filePath);
            else
                file = new File(context.getFilesDir(), filePath);
        }
        else if(fileType == HttpMultipartData.FILE_TYPE_PUBLIC){
            file = new File(filePath);
        }

        if(file != null){
             Log.d(TAG,TAG+"[delete file info] path:"+file.getAbsolutePath()+"(exists:"+file.exists()+" size:"+ FormatUtils.formatFileSize(context, file.length())+")");
            if(file.exists())
                return file.delete();
        }

        return false;
    }
	
	public static class HttpRequestBase{

		HttpURLConnection conn = null;
		AsyncTask<?,?,?> task = null;


		private Object readStream(ResponseBody body, InputStream in, String contentEncoding, String encoding) throws Exception{

			if(body == ResponseBody.STREAM){
				return in;
			}
			
			byte[] buffer = new byte[8192];
			int read = -1;			
			StringBuffer sb = new StringBuffer();
			
			if(contentEncoding != null && contentEncoding.equals("gzip")){
				
				if(body == ResponseBody.JSON_READER){					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();					
					GZIPInputStream gis = new GZIPInputStream(in);
					while((read = gis.read(buffer)) != -1){
						baos.write(buffer, 0, read);
					}					
					ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());					
					JsonReader reader = new JsonReader(new InputStreamReader(bais, encoding));
					bais.close();
					baos.close();
					 Log.d(TAG,TAG+"result :: JsonReader >> "+reader);
					return reader;	
				}
				else if(body == ResponseBody.BINARY){
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					GZIPInputStream gis = new GZIPInputStream(in);
					while((read = gis.read(buffer)) != -1){
						baos.write(buffer, 0 , read);
					}
					gis.close();
					byte[] data = baos.toByteArray();
					baos.close();
					 Log.d(TAG,TAG+"result :: binary["+data.length+"]");
					return data;
				}
				else{
				    ByteArrayOutputStream baos = new ByteArrayOutputStream();
					GZIPInputStream gis = new GZIPInputStream(in);
					while((read = gis.read(buffer)) != -1){
                        baos.write(buffer, 0, read);
					}
					gis.close();

                    BufferedReader r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()), encoding));
                    String line = null;
                    while((line = r.readLine()) != null){
                        sb.append(line);
                        sb.append("\n");
                    }
                    baos.close();
                    r.close();
				}
			}
			else{
				if(body == ResponseBody.JSON_READER){
					InputStreamReader is = new InputStreamReader(in, encoding);
					JsonReader reader = new JsonReader(is);
					 Log.d(TAG,TAG+"result :: JsonReader >> "+reader);
					return reader;	
				}
				else if(body == ResponseBody.BINARY){
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					while((read = in.read(buffer)) != -1){
						baos.write(buffer, 0, read);
					}
					in.close();
					byte[] data = baos.toByteArray();
					baos.close();
					 Log.d(TAG,TAG+"result :: binary["+data.length+"]");
					return data;
				}
                else{
                    BufferedReader r = new BufferedReader(new InputStreamReader(in, encoding));
                    String line = null;
                    while((line = r.readLine()) != null){
                        sb.append(line);
                        sb.append("\n");
                    }
                    r.close();
                }
			}
			
			String r = sb.toString();

            if(body == ResponseBody.JSON || body == ResponseBody.GSON){
                r = r.trim();
            }

			if(DEBUG)
			     Log.d(TAG,TAG+"result :: "+r);
			
			if(body == ResponseBody.JSON){
				if(r.startsWith("{"))
					return new JSONObject(r);
				else if(r.startsWith("["))
					return new JSONArray(r);
			}
			else if(body == ResponseBody.GSON){
				JsonParser parser = new JsonParser();
				if(r.startsWith("{"))
					return parser.parse(r).getAsJsonObject();
				else if(r.startsWith("["))
					return parser.parse(r).getAsJsonArray();
			}
			return r;
		}

        public Object[] requestMain(final Context context, final HttpRequest req){

            final boolean directCall = (!req.isUseRequestBackground() || !req.isUseUiThread());
            boolean success = false;
            Object result = null;

            final String sid = (false? UniqueUtils.getRandUUID():null);

            try{
                String query = buildQueryString(req.getParameterMap());

                String urlString = req.getUrl();
                if(req.getMethod() == Method.GET || req.getRequestBody() == RequestBody.JSON){
                    urlString += TextUtils.isEmpty(query)?"":"?"+query;
                    if(DEBUG)
                         Log.d(TAG,TAG+"request[get:sid("+sid+")] url >> \n"+URLDecoder.decode(urlString, req.getEncoding()));
                }
                else{
                    if(DEBUG)
                         Log.d(TAG,TAG+"request[post:("+sid+")] url >> \n"+urlString+"?"+URLDecoder.decode(query, req.getEncoding()));
                }

                URL url = new URL(urlString);

                if(urlString.startsWith("https")){

                    trustAllCerts();

                    ((HttpsURLConnection)(conn = (HttpsURLConnection) url.openConnection())).setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    });
                }
                else{
                    conn = (HttpURLConnection) url.openConnection();
                }

                conn.setConnectTimeout((int)TimeUnit.MILLISECONDS.convert(req.getConnectTimeout(), TimeUnit.SECONDS));
                conn.setReadTimeout((int)TimeUnit.MILLISECONDS.convert(req.getReadTimeout(), TimeUnit.SECONDS));
                conn.setUseCaches(false);
                conn.setRequestProperty("Accept-Encoding", "identity");
                conn.setRequestProperty("Accept-Language", Locale.getDefault().getLanguage());

                setCookie(context, conn);

                if(req.isMultipartRequest()){
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Cache-Control", "no-cache");
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+BOUNDARY);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

                    Iterator<Map.Entry<String,String>> params = req.getParameterMap().entrySet().iterator();
                    while(params.hasNext()){
                        Map.Entry<String,String> entry = params.next();
                        wr.writeBytes(TWO_HYPHENS+BOUNDARY+CRLF);
                        wr.writeBytes("Content-Disposition: form-data; name=\""+entry.getKey()+"\""+CRLF);
                        //wr.writeBytes("Content-Type: text/plain; charset=\""+req.getEncoding()+"\""+CRLF);
                        wr.writeBytes(CRLF);
                        wr.write((entry.getValue()==null?"null".getBytes():entry.getValue().getBytes(req.getEncoding())));
                        wr.writeBytes(CRLF);
                        wr.flush();
                    }

                    byte[] buffer = new byte[8192];
                    for(HttpMultipartData multipartData : req.getMultipartDatas()){

                        if(multipartData.getFilePath() == null && multipartData.getData() == null){
                            Log.e(TAG,"multipart data is null.");
                            continue;
                        }

                        if(multipartData.getType() == HttpMultipartData.TYPE_FILE){
                            if(multipartData.getFilePath() == null){
                                File f = null;
                                if(multipartData.getFileType() == HttpMultipartData.FILE_TYPE_PRIVATE){
                                    f = new File(context.getFilesDir(), multipartData.getFilePath());
                                }
                                else if(multipartData.getFileType() == HttpMultipartData.FILE_TYPE_PUBLIC){
                                    f = new File(multipartData.getFilePath());
                                }

                                if(f == null || !f.exists()){
                                    Log.e(TAG,"file not found.["+(f==null?null:f.getAbsolutePath())+"]");
                                    continue;
                                }
                            }
                        }

                        wr.writeBytes(TWO_HYPHENS+BOUNDARY+CRLF);

                         Log.d(TAG,TAG+"[multipart] add file >> "+multipartData.getName());
                        wr.writeBytes("Content-Disposition: form-data; name=\""+multipartData.getName()+"\";filename=\""+multipartData.getName()+"\""+CRLF);
                        wr.writeBytes("Content-Transfer-Encoding: binary"+CRLF);
                        wr.writeBytes(CRLF);

                        if(multipartData.getType() == HttpMultipartData.TYPE_FILE){

                            FileInputStream fis = null;
                            try{
                                File file = null;
                                if(multipartData.getFileType() == HttpMultipartData.FILE_TYPE_PRIVATE){
                                    //file = context.getFileStreamPath(multipartData.getFilePath());
                                    //fis = context.openFileInput(multipartData.getFilePath());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                        file = new File(FileUtils.commonDocumentDirPath(""), multipartData.getFilePath());
                                    else
                                        file = new File(context.getFilesDir(), multipartData.getFilePath());

                                    Log.e(TAG,">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> http ,fileutil data postion "+ file);
                                    fis = new FileInputStream(file);
                                }
                                else if(multipartData.getFileType() == HttpMultipartData.FILE_TYPE_PUBLIC){
                                    file = new File(multipartData.getFilePath());
                                    fis = new FileInputStream(file);
                                }

                                if(file != null)
                                     Log.d(TAG,TAG+"[file info] path:"+file.getAbsolutePath()+"(exists:"+file.exists()+" size:"+ FormatUtils.formatFileSize(context, file.length())+")");

                                if(fis != null){
                                    int read = -1;
                                    while((read = fis.read(buffer)) != -1){
                                        wr.write(buffer, 0, read);
                                    }
                                }
                            }
                            catch(Exception e){
                                 Log.e(TAG, e.getMessage());
                            }
                            finally{
                                if(fis != null)
                                    fis.close();
                            }
                        }
                        else if(multipartData.getType() == HttpMultipartData.TYPE_DATA){
                            wr.write(multipartData.getData());
                        }

                        wr.writeBytes(CRLF);
                        wr.flush();
                    }
                    wr.writeBytes(TWO_HYPHENS+BOUNDARY+TWO_HYPHENS+CRLF);
                    wr.flush();
                    wr.close();
                }
                else if(req.getMethod() == Method.POST){

                    byte[] data = null;

                    if(req.getRequestBody() == RequestBody.JSON){
                        conn.setRequestProperty("Content-Type", "application/json; "+req.getEncoding());
                        conn.setRequestProperty("Accept", "application/json");
                        data = req.getContent();
                    }
                    else{
                        data = query.getBytes();
                    }

                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    wr.close();
                }
                else if(req.getMethod() == Method.GET){
                    conn.setRequestMethod("GET");
                }

                conn.connect();
                req.setConn(conn);

                req.setResHeaders(conn.getHeaderFields());

                int responseCode = conn.getResponseCode();
                req.setResponseCode(responseCode);

                req.setEndTimeMillis(System.currentTimeMillis());

                if(DEBUG)
                     Log.d(TAG,TAG+"reqeust >> response >> code >> "+responseCode);


                if(req.getMinWaitTimeMillis() > 0 && req.isUseRequestBackground()){
                    long remainMillis = req.getMinWaitTimeMillis()-(req.getEndTimeMillis()-req.getStartTimeMillis());
                     Log.d(TAG,"wait time >> "+remainMillis+" ms.");
                    if(remainMillis > 0 ){
                        Thread.sleep(remainMillis);
                    }
                }

                if(responseCode == HttpURLConnection.HTTP_OK){

                    storeCookie(context, conn);

                    InputStream in = conn.getInputStream();
                    result = readStream(req.getResponseBody(), in, conn.getContentEncoding(), req.getEncoding());
                    success = true;
                    if(directCall) if(req.getListener() != null) req.getListener().onSuccess(req, result);
                }
                else{

                    req.setFailedString("SERVER_ERROR");

                    if(directCall) if(req.getListener() != null) req.getListener().onFailure(req);
                }
            }
            catch(Exception e){
                req.setFailedString(e.getMessage());
                 Log.e(TAG, e.getMessage());
                if(directCall) if(req.getListener() != null) req.getListener().onFailure(req);
            }
            finally{

                if(false){
                    if(success)
                         Log.d(TAG,TAG+"(sid:"+sid+") result success. >> "+req.getUrl());
                    else
                        Log.e(TAG ,"(sid:"+sid+")result failure. >> "+req.getUrl() );
                }

                if(req.getResponseBody() != ResponseBody.STREAM)
                    if(conn != null) conn.disconnect();
            }
            return new Object[]{ success, result};
        }

        private static class RequestTask extends AsyncTask<Void,Object[],Object[]>{

            WeakReference<HttpRequestBase> weakReference;
            Context context;
            HttpRequest req;

            public RequestTask(HttpRequestBase base, Context context, HttpRequest req){
                weakReference = new WeakReference<HttpRequestBase>(base);
                this.context = context;
                this.req = req;
            }

            @Override
            protected Object[] doInBackground(Void... v) {

                HttpRequestBase base = weakReference.get();
                if(base == null) return null;

                return base.requestMain(context, req);
            }

            @Override
            protected void onPostExecute(Object[] v) {

                HttpRequestBase base = weakReference.get();
                if(base == null) return;

                if(!req.isUseUiThread())
                    return;

                if(isCancelled()){
                     Log.d(TAG,TAG+"this action was cancelled. callback is not called.");
                    return;
                }

                boolean success = (Boolean)v[0];
                Object result = v[1];

                if(success){
                    if(req.getListener() != null) req.getListener().onSuccess(req, result);
                }
                else{
                    if(req.getListener() != null) req.getListener().onFailure(req);
                }

            }
        }
		public void request(final Context context, final HttpRequest req){

		    req.setStartTimeMillis(System.currentTimeMillis());

            if(!req.isUseRequestBackground()){
                requestMain(context, req);
                return;
            }

			TaskUtils.execute((task = new RequestTask(this, context, req)));
		}
	}
}
