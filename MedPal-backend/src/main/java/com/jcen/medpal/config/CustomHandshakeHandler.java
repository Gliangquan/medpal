package com.jcen.medpal.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeHandler;

import java.util.Map;

/**
 * 自定义 WebSocket 握手处理器
 * 用于从请求中提取用户信息并建立连接
 */
public class CustomHandshakeHandler implements HandshakeHandler {

    @Override
    public boolean doHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 从请求参数中获取用户ID（微信小程序通过 URL 参数传递）
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String userId = servletRequest.getServletRequest().getParameter("userId");
            String userType = servletRequest.getServletRequest().getParameter("userType");
            
            if (userId != null) {
                attributes.put("userId", userId);
            }
            if (userType != null) {
                attributes.put("userType", userType);
            }
        }
        return true;
    }
}
