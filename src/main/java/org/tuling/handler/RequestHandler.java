/**
 * MIT License
 * <p>
 * Copyright (c) 2017 Michael Yan
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.tuling.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.tuling.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by myan on 12/4/2017.
 * Intellij IDEA
 */
@ChannelHandler.Sharable
public class RequestHandler extends ChannelInboundHandlerAdapter {
    private HttpHeaders headers;
    // decode our post requests
    private HttpPostRequestDecoder decoder;
    private static final HttpDataFactory FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MAXSIZE);
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            headers = request.headers();
            
            if (request.uri().equalsIgnoreCase(Constants.FAVICON_ICO))
                return; // discard the invalid request
            
            HttpMethod requestMethod = request.method();
            if (requestMethod.equals(HttpMethod.GET)) {
                QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri(), CharsetUtil.UTF_8);
                Map<String, List<String>> uriAttributes = queryDecoder.parameters();
                // just process our query params
                for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
                    for (String attrVal : attr.getValue()) {
                        System.out.println(attr.getKey() + "=" + attrVal);
                    }
                }
            } else if (requestMethod.equals(HttpMethod.POST)) {
                // we need to cast this object for latter processing.
                processPostRequest((FullHttpRequest) msg);
            } else {
                throw new UnsupportedOperationException("We can not process such request at present.");
            }
        } else {
            ReferenceCountUtil.release(msg);// discard this request directly.
        }
        
    }
    
    private void processPostRequest(FullHttpRequest fullHttpRequest) throws IOException {
        switch (getContentType()) {
            case Constants.JSON:
                String content = fullHttpRequest.content().toString(CharsetUtil.UTF_8);
                JSONObject object = JSON.parseObject(content);
                for (Map.Entry<String, Object> entry : object.entrySet()) {
                    System.out.println(entry.getKey() + ":" + entry.getValue().toString());
                }
                break;
            case Constants.FORM:
                if(decoder != null) {
                    decoder.cleanFiles();
                    decoder = null;
                }
                decoder = new HttpPostRequestDecoder(FACTORY, fullHttpRequest, CharsetUtil.UTF_8);
                for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                    if(data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                        Attribute attribute = (Attribute) data;
                        System.out.println(attribute.getName() + "=" + attribute.getValue());
                    }
                }
                break;
            case Constants.MULTI_PART:
                // process file upload here.
                break;
            default:
                throw new UnsupportedOperationException("We can not process such request at present.");
        }
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
    
    private String getContentType() {
        return headers.get("Content-Type").split(";")[0];
    }
}
