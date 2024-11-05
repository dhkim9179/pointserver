package com.example.pointserver.common.logging;

import com.example.pointserver.common.logging.wrapper.RequestWrapper;
import com.example.pointserver.common.logging.wrapper.ResponseWrapper;
import com.example.pointserver.common.utils.JsonUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.RequestInfo;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LoggingFilter extends OncePerRequestFilter {
    private String[] responseContentTypePrefixesExcludeLogging = new String[] {};

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/point")) {
            RequestWrapper requestWrapper = new RequestWrapper(0L, request);
            ResponseWrapper responseWrapper = new ResponseWrapper(1L, response);
            try {
                RequestInfo requestInfo = RequestInfo.builder()
                        .ip(request.getRemoteAddr())
                        .body(getRequestBody(requestWrapper))
                        .params(getParameters(request))
                        .build();

                log.info("{} {} {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        JsonUtils.toPrettyPrintJson(JsonUtils.toJson(requestInfo))
                );
                filterChain.doFilter(requestWrapper, responseWrapper);
            } catch (Exception e) {

            } finally {
                log.info("{} {} {} {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        JsonUtils.toPrettyPrintJson(getResponseBody(responseWrapper))
                );
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private Map<String, Object> getParameters(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        for (Enumeration<String> em = request.getParameterNames(); em.hasMoreElements();) {
            String parameterName = em.nextElement();
            params.put(parameterName, request.getParameter(parameterName));
        }
        return params.isEmpty() ? null : params;
    }

    private Object getRequestBody(RequestWrapper request) {
        String jsonBodyString = new String(request.toByteArray(), StandardCharsets.UTF_8);
        if (!jsonBodyString.equals("")) {
            if (!isMultipartRequest(request) && !isBinaryContentRequest(request)) {
                return JsonUtils.fromJson(jsonBodyString, Map.class);
            }
        }
        return jsonBodyString;
    }

    private String getResponseBody(ResponseWrapper response) {
        return new String(response.toByteArray(), StandardCharsets.UTF_8);
    }

    public void setResponseContentTypePrefixesExcludeLogging(String... responseContentTypePrefixesExcludeLogging) {
        this.responseContentTypePrefixesExcludeLogging = responseContentTypePrefixesExcludeLogging;
    }

    private boolean isBinaryContentRequest(HttpServletRequest request) {
        return isBinaryContent(request.getContentType());
    }

    private boolean isBinaryContent(String contentType) {
        return contentType != null && (contentType.startsWith("image") || contentType.startsWith("video") || contentType.startsWith("audio"));
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && (contentType.startsWith("multipart/form-data"));
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class RequestInfo {
        private String ip;
        private Object body;
        private Map<String, Object> params;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class ResponseInfo {
        private int status;
        private Object body;
    }
}
