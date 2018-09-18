package org.wso2.transport.http.netty.contractimpl.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.List;

/**
 * Custom Http Content Compressor to handle the content-length and transfer encoding.
 */
public class CustomHttpContentCompressor extends HttpContentCompressor {

    private HttpMethod method;

    public CustomHttpContentCompressor() {
        super();
    }

    @Override
    protected Result beginEncode(HttpResponse headers, String acceptEncoding) throws Exception {
        String allowHeader = headers.headers().get(HttpHeaderNames.ALLOW);
        String contentLength = headers.headers().get(HttpHeaderNames.CONTENT_LENGTH);
        if (method == HttpMethod.OPTIONS && allowHeader != null && contentLength.equals("0")) {
            return null;
        }
        String contentEncoding = headers.headers().get(HttpHeaderNames.CONTENT_ENCODING);
        if (contentEncoding != null) {
            //When the response contains content-encoding header, override acceptEncoding value with it, which will
            //ultimately be used for compression and then remove the content-encoding header from response.
            acceptEncoding = contentEncoding;
            headers.headers().remove(HttpHeaderNames.CONTENT_ENCODING);
        }
        return super.beginEncode(headers, acceptEncoding);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpRequest msg, List<Object> out)
            throws Exception {
        this.method = msg.method();
        super.decode(ctx, msg, out);
    }
}
