package org.expressme.wireless.reader.fetcher;

import java.io.UnsupportedEncodingException;

/**
 * Simple JavaBean that holds the HTTP response.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class Response {

    public final String contentType;
    public final String charset;
    public final byte[] content;

    public Response(String contentType, String charset, byte[] content) {
        this.contentType = contentType;
        this.charset = charset;
        this.content = content;
    }

    public String getContentAsString() throws UnsupportedEncodingException {
        String enc = charset==null ? "ISO-8859-1" : charset;
        return new String(content, enc);
    }

    public boolean isPlain() {
        return contentType!=null && contentType.startsWith("text/plain");
    }

    public boolean isXml() {
        return contentType!=null && (
                contentType.startsWith("application/xml")
                || contentType.startsWith("text/xml")
        );
    }

    public boolean isHtml() {
        return contentType!=null && (
                contentType.startsWith("text/html")
                || contentType.startsWith("text/xml")
        );
    }
}
