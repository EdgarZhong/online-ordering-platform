/*
 * @Author: EdgarZhong 18518713412@163.com
 * @Date: 2025-10-27 17:15:41
 * @LastEditors: EdgarZhong 18518713412@163.com
 * @LastEditTime: 2025-11-14 19:25:23
 * @FilePath: \final\online-ordering-platform\backend\src\main\java\com\platform\ordering\filter\AuthFilter.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.platform.ordering.filter;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.platform.ordering.model.User;

/**
 * 后台访问权限过滤器 (AuthFilter)
 * <p>
 * 保护所有 /admin/ 路径下的资源。
 * 检查用户是否已登录以及是否拥有访问后台的权限（商户或超级管理员）。
 * 如果用户未登录或权限不足，则重定向到登录页面。
 * </p>
 */
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
            User user = (User) session.getAttribute("user");
            if ("merchant".equals(user.getRole())) {
                Integer restaurantId = user.getRestaurantId();
                if (restaurantId == null) {
                    String target = httpRequest.getRequestURI();
                    String qs = httpRequest.getQueryString();
                    if (qs != null) {
                        target += "?" + qs;
                    }
                    String ret = URLEncoder.encode(target, "UTF-8");
                    httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?redirect=" + ret);
                    return;
                }
                request.setAttribute("restaurantId", restaurantId);
            }
            chain.doFilter(request, response);
        } else {
            // 用户未登录或权限不足，重定向到登录页面
            // 使用 httpRequest.getContextPath() 来构建正确的URL
            String target = httpRequest.getRequestURI();
            String qs = httpRequest.getQueryString();
            if (qs != null) {
                target += "?" + qs;
            }
            String ret = URLEncoder.encode(target, "UTF-8");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?redirect=" + ret);
        }
    }

    @Override
    public void destroy() {
    }
}

