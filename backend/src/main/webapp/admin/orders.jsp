<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="/admin/header.jsp" />
<div class="container">
    <h2 class="mb-3">订单管理</h2>
    <form class="row g-2 align-items-end mb-3" method="get" action="${pageContext.request.contextPath}/admin/orders">
        <div class="col-md-2">
            <label class="form-label">状态</label>
            <select name="status" class="form-select">
                <option value="">全部</option>
                <option value="PENDING" ${status == 'PENDING' ? 'selected' : ''}>PENDING</option>
                <option value="PROCESSING" ${status == 'PROCESSING' ? 'selected' : ''}>PROCESSING</option>
                <option value="COMPLETED" ${status == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                <option value="CANCELLED" ${status == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
            </select>
        </div>
        <div class="col-md-2">
            <label class="form-label">起始日期</label>
            <input type="date" class="form-control" name="from" value="${from}" />
        </div>
        <div class="col-md-2">
            <label class="form-label">截止日期</label>
            <input type="date" class="form-control" name="to" value="${to}" />
        </div>
        <div class="col-md-3">
            <label class="form-label">关键词</label>
            <input type="text" class="form-control" name="keyword" value="${keyword}" placeholder="订单号或顾客用户名" />
        </div>
        <div class="col-md-2">
            <label class="form-label">每页条数</label>
            <input type="number" class="form-control" name="size" value="${size}" min="1" />
        </div>
        <div class="col-md-1">
            <button type="submit" class="btn btn-primary w-100">筛选</button>
        </div>
    </form>

    <style>
        .table-orders { table-layout: fixed; }
        .table-orders th, .table-orders td { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    </style>
    <table class="table table-striped table-hover table-bordered table-orders">
        <colgroup>
            <col style="width:10%" />
            <col style="width:12%" />
            <col style="width:10%" />
            <col style="width:24%" />
            <col style="width:12%" />
            <col style="width:12%" />
            <col style="width:20%" />
        </colgroup>
        <thead class="table-light">
        <tr class="text-nowrap">
            <th>订单ID</th>
            <th>顾客ID</th>
            <th>流水号</th>
            <th>下单时间</th>
            <th class="text-end">总价</th>
            <th>当前状态</th>
            <th>查看详情</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="o" items="${orders}">
            <tr>
                <td>${o.orderId}</td>
                <td>${o.userId}</td>
                <td>${o.serialNumber}</td>
                <td><fmt:formatDate value="${o.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td class="text-end"><fmt:formatNumber value="${o.totalPrice}" type="currency"/></td>
                <td>
                    <c:choose>
                        <c:when test="${o.status == 'PENDING'}"><span class="badge text-white bg-warning">待处理</span></c:when>
                        <c:when test="${o.status == 'PROCESSING'}"><span class="badge text-white bg-info">制作中</span></c:when>
                        <c:when test="${o.status == 'COMPLETED'}"><span class="badge text-white bg-success">已完成</span></c:when>
                        <c:when test="${o.status == 'CANCELLED'}"><span class="badge text-white bg-danger">已取消</span></c:when>
                        <c:otherwise><span class="badge text-white bg-secondary">${o.status}</span></c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/orders/${o.orderId}">查看详情</a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <div class="d-flex justify-content-between align-items-center mt-2">
        <div>共 ${total} 条；第 ${page}/${pages} 页</div>
        <div>
            <c:forEach var="p" begin="1" end="${pages}">
                <c:choose>
                    <c:when test="${p == page}">
                        <span class="badge bg-primary">${p}</span>
                    </c:when>
                    <c:otherwise>
                        <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/orders?status=${status}&from=${from}&to=${to}&keyword=${keyword}&page=${p}&size=${size}">${p}</a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>
    </div>
</div>
<jsp:include page="/admin/footer.jsp" />