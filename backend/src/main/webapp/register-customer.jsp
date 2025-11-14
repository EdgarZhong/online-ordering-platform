<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<jsp:include page="/admin/header.jsp" />

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <h3>消费者注册</h3>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/register" method="post">
                        <!-- 隐藏字段，用于告知Servlet角色 -->
                        <input type="hidden" name="role" value="customer">

                        <div class="mb-3">
                            <label for="username" class="form-label">用户名</label>
                            <input type="text" class="form-control" id="username" name="username" required>
                        </div>
                        <div class="mb-3">
                            <label for="password" class="form-label">密码</label>
                            <input type="password" class="form-control" id="password" name="password" required>
                        </div>
                        <div class="mb-3">
                            <label for="phone" class="form-label">手机号</label>
                            <input type="tel" class="form-control" id="phone" name="phone" required>
                        </div>
                        <button type="submit" class="btn btn-primary w-100">注册</button>
                    </form>
                </div>
                <div class="card-footer text-center">
                    <a href="login.jsp">已有账号？直接登录</a>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/admin/footer.jsp" />
