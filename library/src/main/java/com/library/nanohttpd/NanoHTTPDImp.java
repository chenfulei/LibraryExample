package com.library.nanohttpd;

/**
 * 创建本地服务
 * Created by chen_fulei on 2015/7/27.
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NanoHTTPDImp extends NanoHTTPD {

    public Response response = newFixedLengthResponse("");
    public String uri;
    public Method method;
    public Map<String, String> header;
    public Map<String, String> parms;
    public Map<String, String> files;
    public Map<String, List<String>> decodedParamters;
    public Map<String, List<String>> decodedParamtersFromParameter;
    public String queryParameterString;

    public NanoHTTPDImp() {
        super(8080);
    }

    public NanoHTTPDImp(int port) {
        super(port);
    }

    public NanoHTTPDImp(String hostname, int port) {
        super(hostname, port);
    }

    public NanoHTTPDImp(String hostname) {
        super(hostname, 8080);
    }

    public HTTPSession createSession(TempFileManager tempFileManager,
                                     InputStream inputStream, OutputStream outputStream) {
        return new HTTPSession(tempFileManager, inputStream, outputStream);
    }

    public HTTPSession createSession(TempFileManager tempFileManager,
                                     InputStream inputStream, OutputStream outputStream,
                                     InetAddress inetAddress) {
        return new HTTPSession(tempFileManager, inputStream, outputStream,
                inetAddress);
    }

    @Override
    public String decodePercent(String str) {
        return super.decodePercent(str);
    }

    @Override
    public Response serve(IHTTPSession session) {
        this.uri = session.getUri();
        this.method = session.getMethod();
        this.header = session.getHeaders();
        this.parms = session.getParms();
        this.files = new HashMap<String, String>();
        try {
            session.parseBody(this.files);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.queryParameterString = session.getQueryParameterString();
        this.decodedParamtersFromParameter = decodeParameters(this.queryParameterString);
        this.decodedParamters = decodeParameters(session.getQueryParameterString());
        return this.response;
    }

}
