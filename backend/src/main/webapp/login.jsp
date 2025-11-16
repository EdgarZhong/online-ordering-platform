<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>统一登录</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f8f9fa; }
    </style>
    </head>
<body>
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-5">
            <div class="card">
                <div class="card-header">
                    <h3>统一登录</h3>
                </div>
                <div class="card-body">
                    <!-- 显示提示信息 -->
                    <c:if test="${param.registration == 'success'}">
                        <div class="alert alert-success">注册成功，请登录！</div>
                    </c:if>
                    <c:if test="${param.error == 'registrationFailed'}">
                        <div class="alert alert-danger">注册失败，可能用户名已存在。</div>
                    </c:if>
                    <c:if test="${param.error == 'invalidCredentials'}">
                        <div class="alert alert-danger">用户名或密码错误。</div>
                    </c:if>
                    <c:if test="${param.logout == 'success'}">
                        <div class="alert alert-info">您已成功退出。</div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/login" method="post">
                        <div class="mb-3">
                            <label for="username" class="form-label">用户名</label>
                            <input type="text" class="form-control" id="username" name="username" required>
                        </div>
                        <div class="mb-3">
                            <label for="password" class="form-label">密码</label>
                            <input type="password" class="form-control" id="password" name="password" required>
                        </div>
                        <input type="hidden" name="redirect" value="${param.redirect}" />
                        <button type="submit" class="btn btn-primary w-100">登录</button>
                    </form>
                </div>
                <div class="card-footer">
                    <div class="d-flex justify-content-between">
                        <a href="register-customer.jsp">注册消费者账号</a>
                        <a href="register-merchant.jsp">注册商户账号</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>