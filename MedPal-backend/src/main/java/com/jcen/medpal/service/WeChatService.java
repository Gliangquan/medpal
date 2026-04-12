package com.jcen.medpal.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.config.WeChatConfig;
import com.jcen.medpal.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WeChatService {

    private final WeChatConfig weChatConfig;
    private final RestTemplate restTemplate;

    public WeChatService(WeChatConfig weChatConfig) {
        this.weChatConfig = weChatConfig;
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> getSessionKeyOrOpenid(String code) {
        Map<String, String> params = new HashMap<>();
        params.put("appid", weChatConfig.getAppId());
        params.put("secret", weChatConfig.getSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");

        String url = weChatConfig.getLoginUrl() + "?appid={appid}&secret={secret}&js_code={js_code}&grant_type={grant_type}";

        try {
            String response = restTemplate.getForObject(url, String.class, params);
            log.info("微信登录响应: {}", response);

            JSONObject jsonObject = JSON.parseObject(response);

            if (jsonObject.containsKey("errcode") && jsonObject.getInteger("errcode") != 0) {
                Integer errCode = jsonObject.getInteger("errcode");
                String errMsg = jsonObject.getString("errmsg");
                log.error("微信登录失败, errcode={}, errmsg={}", errCode, errMsg);
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "微信登录失败(" + errCode + "): " + errMsg);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("openid", jsonObject.getString("openid"));
            result.put("session_key", jsonObject.getString("session_key"));
            result.put("unionid", jsonObject.getString("unionid"));

            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信接口异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "微信登录接口调用失败");
        }
    }
}
