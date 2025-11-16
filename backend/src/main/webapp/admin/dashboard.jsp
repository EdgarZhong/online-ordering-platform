<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>

<jsp:include page="header.jsp" />
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!-- 仪表盘主体内容 -->
<main>
    <div class="row">
        <div class="col-md-12">
            <h2>仪表盘</h2>
            <p>欢迎来到商户管理后台。这里将显示您的关键业务数据。</p>
        </div>
    </div>

    <div class="row g-4 mt-2">
        <div class="col-md-3">
            <a href="${pageContext.request.contextPath}/admin/orders?from=${todayStr}&to=${todayStr}" class="text-decoration-none">
                <div class="card text-white bg-primary">
                    <div class="card-body">
                        <h5 class="card-title">今日新订单</h5>
                        <p class="card-text fs-3">${ordersTodayCount}</p>
                    </div>
                </div>
            </a>
        </div>
        <div class="col-md-3">
            <div class="card text-white bg-success">
                <div class="card-body">
                    <h5 class="card-title">今日总收入</h5>
                    <p class="card-text fs-3"><fmt:formatNumber value="${revenueToday}" type="currency"/></p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <a href="${pageContext.request.contextPath}/admin/kitchen" class="text-decoration-none">
                <div class="card text-white bg-warning">
                    <div class="card-body">
                        <h5 class="card-title">待处理(PENDING)</h5>
                        <p class="card-text fs-3">${pendingCount}</p>
                    </div>
                </div>
            </a>
        </div>
        <div class="col-md-3">
            <a href="${pageContext.request.contextPath}/admin/kitchen" class="text-decoration-none">
                <div class="card text-white bg-info">
                    <div class="card-body">
                        <h5 class="card-title">备餐中(PROCESSING)</h5>
                        <p class="card-text fs-3">${processingCount}</p>
                    </div>
                </div>
            </a>
        </div>
    </div>

    <div class="mt-4">
        <a class="btn btn-outline-success me-2" href="${pageContext.request.contextPath}/admin/menus">进入菜单管理</a>
        <a class="btn btn-outline-info me-2" href="${pageContext.request.contextPath}/admin/dishes">进入菜品管理</a>
    </div>

</main>

<jsp:include page="footer.jsp" />
