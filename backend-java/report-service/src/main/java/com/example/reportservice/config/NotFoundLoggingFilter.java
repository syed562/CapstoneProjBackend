package com.example.reportservice.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class NotFoundLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(NotFoundLoggingFilter.class);

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        filterChain.doFilter(request, response);

        if (request.getDispatcherType() == DispatcherType.ERROR
                && response.getStatus() == HttpServletResponse.SC_NOT_FOUND) {
            String uri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            String query = request.getQueryString();
            Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

            log.warn(
                    "404 {} {}{} status={} reason={}",
                    request.getMethod(),
                    uri != null ? uri : request.getRequestURI(),
                    query != null ? "?" + query : "",
                    statusCode != null ? statusCode : response.getStatus(),
                    message != null ? message : "Not Found");
        }
    }
}
