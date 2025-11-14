<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<jsp:include page="/admin/header.jsp" />

<div class="container mt-5 mb-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card">
                <div class="card-header">
                    <h3>商户注册</h3>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/register" method="post">
                        <!-- 隐藏字段，用于告知Servlet角色 -->
                        <input type="hidden" name="role" value="merchant">

                        <h5 class="mt-3">账号信息</h5>
                        <hr>
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label for="username" class="form-label">管理员用户名</label>
                                <input type="text" class="form-control" id="username" name="username" required>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label for="password" class="form-label">密码</label>
                                <input type="password" class="form-control" id="password" name="password" required>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label for="phone" class="form-label">管理员手机号</label>
                                <input type="tel" class="form-control" id="phone" name="phone" required>
                            </div>
                        </div>

                        <h5 class="mt-4">餐厅信息</h5>
                        <hr>
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label for="restaurantName" class="form-label">餐厅名称</label>
                                <input type="text" class="form-control" id="restaurantName" name="restaurantName" required>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label for="restaurantPhone" class="form-label">餐厅联系电话</label>
                                <input type="tel" class="form-control" id="restaurantPhone" name="restaurantPhone" required>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label for="address" class="form-label">餐厅地址</label>
                            <input type="text" class="form-control" id="address" name="address" required>
                        </div>
                        <div class="mb-3">
                            <label for="description" class="form-label">餐厅简介</label>
                            <textarea class="form-control" id="description" name="description" rows="3"></textarea>
                        </div>

                        <button type="submit" class="btn btn-primary w-100 mt-3">注册成为商户</button>
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
