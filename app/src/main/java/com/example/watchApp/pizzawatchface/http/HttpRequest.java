package com.example.watchApp.pizzawatchface.http;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;


import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest implements Serializable{

    private transient HttpUtils.HttpRequestBase base;
    private String url;
    private HttpUtils.Method method = HttpUtils.Method.GET;
    private String encoding = HttpUtils.DEFAULT_ENCODING;
    private final Map<String,String> param = new LinkedHashMap<String,String>();
    private transient HttpUtils.HttpResponseListener listener;
    private Handler handler;
    private HttpUtils.RequestBody requestBody = HttpUtils.RequestBody.HTML;
    private HttpUtils.ResponseBody responseBody = HttpUtils.ResponseBody.TEXT;
    private byte[] content;
    private boolean cancelled;
    private int connectTimeout = HttpUtils.DEFAULT_CONNECT_TIMEOUT;
    private int readTimeout = HttpUtils.DEFAULT_READ_TIMEOUT;
    private boolean useUiThread = true;
    private boolean useRequestBackground = true;
    private final List<HttpMultipartData> multipartDatas = new ArrayList<HttpMultipartData>();
    private boolean multipartRequest;
    private String failedString;
    private int responseCode;
    private long startTimeMillis, endTimeMillis;
    private long minWaitTimeMillis; //use useRequestBackground(true)
    private Map<String, List<String>> reqHeaders;
    private Map<String, List<String>> resHeaders;
    private HttpURLConnection conn;
    private transient Bundle extras = new Bundle();

    public HttpRequest execute(Context context){
        base = new HttpUtils.HttpRequestBase();
        base.request(context, this);
        return this;
    }
    public void cancel(){

        if(base != null){
            cancelled = true;
            if(  Looper.myLooper() == Looper.getMainLooper()){
                new Thread(){
                    @Override
                    public void run(){
                         startCancel();
                    }
                }.start();
            }
            else{
                startCancel();
            }
        }
    }

    private void startCancel(){
        if(base.conn != null) base.conn.disconnect();
        if(base.task != null) base.task.cancel(true);
    }

    public HttpRequest addParameter(String key, String value){
        param.put(key, value);
        return this;
    }

    public HttpRequest addParameterMap(Map<String,String> map){

        for(Map.Entry<String, String> entry : map.entrySet()){
            addParameter(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public Map<String,String> getParameterMap(){
        return param;
    }
    public HttpRequest removeParameter(String key){
        param.remove(key);
        return this;
    }
    public String getUrl(){
        return url;
    }
    public HttpRequest setUrl(String url){
        this.url = url;
        return this;
    }
    public HttpUtils.Method getMethod(){
        return method;
    }
    public HttpRequest setMethod(HttpUtils.Method method){
        this.method = method;
        return this;
    }
    public String getEncoding(){
        return encoding;
    }
    public HttpRequest setEncoding(String encoding){
        this.encoding = encoding;
        return this;
    }
    public HttpUtils.HttpResponseListener getListener(){
        return listener;
    }
    public HttpRequest setListener(HttpUtils.HttpResponseListener listener){
        this.listener = listener;
        return this;
    }
    public Handler getHandler(){
        return handler;
    }
    public HttpRequest setHandler(Handler handler){
        this.handler = handler;
        return this;
    }
    public HttpUtils.ResponseBody getResponseBody(){
        return responseBody;
    }
    public HttpRequest setResponseBody(HttpUtils.ResponseBody responseBody){
        this.responseBody = responseBody;
        return this;
    }

    public HttpUtils.RequestBody getRequestBody(){
        return requestBody;
    }

    public HttpRequest setRequestBody(HttpUtils.RequestBody requestBody){
        this.requestBody = requestBody;
        return this;
    }

    public byte[] getContent(){
        return content;
    }

    public HttpRequest setContent(byte[] content){
        this.content = content;
        return this;
    }

    public boolean isCancelled(){
        return cancelled;
    }
    public int getConnectTimeout(){
        return connectTimeout;
    }

    /**
     *
     * @param connectTimeout : seconds
     * @return
     */
    public HttpRequest setConnectTimeout(int connectTimeout){
        this.connectTimeout = connectTimeout;
        return this;
    }
    public int getReadTimeout(){
        return readTimeout;
    }

    /**
     *
     * @param readTimeout : seconds
     * @return
     */
    public HttpRequest setReadTimeout(int readTimeout){
        this.readTimeout = readTimeout;
        return this;
    }
    public boolean isUseUiThread(){
        return useUiThread;
    }
    public HttpRequest setUseUiThread(boolean useUiThread){
        this.useUiThread = useUiThread;
        return this;
    }
    public String getFailedString(){
        return failedString;
    }
    public void setFailedString(String failedString){
        this.failedString = failedString;
    }
    public int getResponseCode(){
        return responseCode;
    }
    public void setResponseCode(int responseCode){
        this.responseCode = responseCode;
    }
    public Bundle getExtras(){
        return extras;
    }
    public HttpRequest setExtras(Bundle extras){
        this.extras = extras;
        return this;
    }
    public boolean isUseRequestBackground(){
        return useRequestBackground;
    }
    public HttpRequest setUseRequestBackground(boolean useRequestBackground){
        this.useRequestBackground = useRequestBackground;
        return this;
    }
    public HttpRequest addMultipartData(HttpMultipartData data){
        this.multipartDatas.add(data);
        return this;
    }
    public List<HttpMultipartData> getMultipartDatas(){
        return this.multipartDatas;
    }
    public HttpRequest setMultipartRequest(boolean multipartRequest){
        this.multipartRequest = multipartRequest;
        return this;
    }
    public boolean isMultipartRequest(){
        return multipartRequest;
    }

    public long getStartTimeMillis(){
        return startTimeMillis;
    }

    public HttpRequest setStartTimeMillis(long startTimeMillis){
        this.startTimeMillis = startTimeMillis;
        return this;
    }

    public long getEndTimeMillis(){
        return endTimeMillis;
    }

    public HttpRequest setEndTimeMillis(long endTimeMillis){
        this.endTimeMillis = endTimeMillis;
        return this;
    }

    public long getMinWaitTimeMillis(){
        return minWaitTimeMillis;
    }

    public HttpRequest setMinWaitTimeMillis(long minWaitTimeMillis){
        this.minWaitTimeMillis = minWaitTimeMillis;
        return this;
    }

    public Map<String,List<String>> getReqHeaders(){
        return reqHeaders;
    }

    public void setReqHeaders(Map<String,List<String>> reqHeaders){
        this.reqHeaders = reqHeaders;
    }

    public Map<String,List<String>> getResHeaders(){
        return resHeaders;
    }

    public void setResHeaders(Map<String,List<String>> resHeaders){
        this.resHeaders = resHeaders;
    }

    public HttpURLConnection getConn(){
        return conn;
    }

    public void setConn(HttpURLConnection conn){
        this.conn = conn;
    }
}
