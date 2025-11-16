package com.platform.ordering.filter;

import javax.servlet.*;
import java.io.IOException;

/**
 * 全站字符编码过滤器 (CharacterEncodingFilter)
 * <p>
 * 强制将所有请求和响应的编码设置为UTF-8，以防止出现乱码问题。
 * 这是Java Web开发的标准实践之一。
 * </p>
 */
public class CharacterEncodingFilter implements Filter {

    private static final String ENCODING = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter初始化，这里不需要特殊操作
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 1. 设置请求编码
        request.setCharacterEncoding(ENCODING);

        // 2. 设置响应编码
        response.setCharacterEncoding(ENCODING);

        // 3. 将请求传递给过滤器链中的下一个过滤器或目标Servlet
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Filter销毁，这里不需要特殊操作
    }
}
