package com.platform.ordering.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CorsFilter implements Filter {

    private String allowedOrigin;

    @Override
    public void init(FilterConfig filterConfig) {
        allowedOrigin = filterConfig.getServletContext().getInitParameter("corsAllowedOrigin");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String origin = req.getHeader("Origin");
        if (allowedOrigin != null && !allowedOrigin.isEmpty() && allowedOrigin.equals(origin)) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            resp.setHeader("Vary", "Origin");
            resp.setHeader("Access-Control-Expose-Headers", "X-Page, X-Size, X-Menu-Version, X-Menu-Signature, ETag, x-menu-version, x-menu-signature");
        }

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            resp.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Requested-With, Authorization");
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}