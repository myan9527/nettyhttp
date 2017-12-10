/*
  MIT License
  <p>
  Copyright (c) 2017 Michael Yan
  <p>
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
  <p>
  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.
  <p>
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
 */
package org.nettymvc.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.nettymvc.Constants;
import org.nettymvc.annotation.RequestMethod;
import org.nettymvc.data.FormParam;
import org.nettymvc.data.HttpHeaderConstants;
import org.nettymvc.data.QueryParam;
import org.nettymvc.data.RequestParam;
import org.nettymvc.data.response.NettyResponse;
import org.nettymvc.exception.InvalidResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by myan on 12/5/2017.
 * Intellij IDEA
 */
@ChannelHandler.Sharable
public class NettyRequestDispatcher extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyRequestDispatcher.class);
    
    private final RoutingContext routingContext = RoutingContext.getRoutingContext();
    
    private final ThreadLocal<HttpHeaders> headers = new ThreadLocal<>();
    private final ThreadLocal<HttpRequest> request = new ThreadLocal<>();
    
    // decode our post requests
    private HttpPostRequestDecoder decoder;
    private static final HttpDataFactory FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MAXSIZE);
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // check routing init status
        if (!routingContext.isInitialized())
            routingContext.init();
        // parse our request and send the mapped resource
        if (msg instanceof HttpRequest) {
            request.set((HttpRequest) msg);
            headers.set(request.get().headers());
            String uri = request.get().uri();
            
            if (uri.equalsIgnoreCase(Constants.FAVICON_ICO))
                return; // discard the invalid request
            
            try {
                doDispatch(request.get(), uri, ctx);
            } catch (Throwable e) {
                LOGGER.error("Error occur:", e);
                ReferenceCountUtil.release(msg);
                throw e;
            }
        } else {
            ReferenceCountUtil.release(msg);// discard this request directly.
        }
    }
    
    /*
   * process request:
   * 1.parse uri
   * 2.build the params
   * 3.load the action method from actionMap -- move to doXXX method
   * 4.invoke the action method and build our response
   */
    private void doDispatch(HttpRequest request, String uri, ChannelHandlerContext ctx) throws Exception {
        HttpMethod requestMethod = request.method();
        RequestParam params = new RequestParam();
        FullHttpResponse response;
        uri = processQueryParams(uri, params);
        if (requestMethod.equals(HttpMethod.GET)) {
            // search for mapped resource,send response to client
            response = doGet(uri, params);
        } else if (requestMethod.equals(HttpMethod.POST)) {
            // we need to cast this object for latter processing.
            response = doPost((FullHttpRequest) request, uri, params);
        } else {
            throw new UnsupportedOperationException("We can not process such request at present.");
        }
        // write response
        if (response != null) {
            ChannelFuture future = ctx.channel().write(response);
            if (!isShortConnection()) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
    
    private FullHttpResponse doGet(String uri, RequestParam params) {
        ActionHandler handler = routingContext.getActionHandler(uri, RequestMethod.GET);
        return getResponse(params, handler);
    }
    
    private FullHttpResponse getResponse(RequestParam params, ActionHandler handler) {
        if (handler != null) {
            Object returnResult = ClassTracker.invokeMethod(routingContext.getSingletons().get(handler.getRouter()),
                    handler.getMethod(), params);
            if (returnResult instanceof NettyResponse) {
                return ((NettyResponse) returnResult).response();
            } else {
                throw new InvalidResponseException("All response must be presented by NettyResponse!");
            }
        } else {
            return Constants.NOT_FOUND_RESPONSE;
        }
    }
    
    private String processQueryParams(String uri, RequestParam params) {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(uri, CharsetUtil.UTF_8);
        Map<String, List<String>> uriAttributes = queryDecoder.parameters();
        if (uri.contains("?")) {
            uri = uri.substring(0, uri.indexOf("?"));
        }
        LOGGER.info(String.format("Processing get request, path: %s", uri));
        // just process url query params
        for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
            List<String> attrValue = attr.getValue();
            if (attrValue != null) {
                if (attrValue.size() == 1)
                    params.add(new QueryParam(attr.getKey(), attrValue.get(0)));
                else
                    params.add(new QueryParam(attr.getKey(), attrValue));
            }
        }
        return uri;
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        FullHttpResponse exceptionResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.copiedBuffer(cause.getMessage(), CharsetUtil.UTF_8));
        ctx.channel().writeAndFlush(exceptionResponse);
        ctx.close();
    }
    
    private FullHttpResponse doPost(FullHttpRequest fullHttpRequest, String uri, RequestParam params) throws IOException {
        ActionHandler handler = this.routingContext.getActionHandler(uri, RequestMethod.POST);
        switch (getRequestContentType()) {
            // process different type of params.
            case Constants.JSON:
                String content = fullHttpRequest.content().toString(CharsetUtil.UTF_8);
                JSONObject object = JSON.parseObject(content);
                if (object != null) {
                    for (Map.Entry<String, Object> entry : object.entrySet()) {
                        params.add(new FormParam(entry.getKey(), entry.getValue()));
                    }
                }
                break;
            case Constants.FORM:
                if (decoder != null) {
                    decoder.cleanFiles();
                    decoder = null;
                }
                decoder = new HttpPostRequestDecoder(FACTORY, fullHttpRequest, CharsetUtil.UTF_8);
                for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                        Attribute attribute = (Attribute) data;
                        params.add(new FormParam(attribute.getName(), attribute.getValue()));
                    }
                }
                break;
            case Constants.MULTI_PART:
                // process binary parameters.
                LOGGER.info("Processing uploaded files....");
                break;
            default:
                throw new UnsupportedOperationException("We can not process such request at present.");
        }
        return getResponse(params, handler);
    }
    
    private String getRequestContentType() {
        return headers.get().get(Constants.CONTENT_TYPE).split(":")[0];
    }
    
    private boolean isShortConnection() {
        return headers.get().contains(HttpHeaderConstants.CONNECTION, Constants.CONNECTION_CLOSE, true) ||
                (request.get().protocolVersion().equals(HttpVersion.HTTP_1_0) &&
                        !headers.get().contains(HttpHeaderConstants.CONNECTION, Constants.CONNECTION_KEEP_ALIVE, true));
    }
}
