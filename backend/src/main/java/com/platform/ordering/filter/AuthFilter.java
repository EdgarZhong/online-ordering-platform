package com.platform.ordering.filter;

import com.platform.ordering.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 后台访问权限过滤器 (AuthFilter)
 * <p>
 * 保护所有 /admin/ 路径下的资源。
 * 检查用户是否已登录以及是否拥有访问后台的权限（商户或超级管理员）。
 * 如果用户未登录或权限不足，则重定向到登录页面。
 * </p>
 */
@WebFilter("/admin/*") // 仅保护/admin/目录下的所有资源
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false); // false表示不创建新session

        boolean isLoggedIn = false;
        boolean hasPermission = false;

        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                isLoggedIn = true;
                // 检查角色是否为商户或超级管理员
                if ("merchant".equals(user.getRole()) || "superadmin".equals(user.getRole())) {
                    hasPermission = true;
                }
            }
        }

        if (isLoggedIn && hasPermission) {
            // 用户已登录且有权限，继续访问
            chain.doFilter(request, response);
        } else {
            // 用户未登录或权限不足，重定向到登录页面
            // 使用 httpRequest.getContextPath() 来构建正确的URL
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
        }
    }

    @Override
    public void destroy() {
    }
}

