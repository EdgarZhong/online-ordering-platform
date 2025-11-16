<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="/admin/header.jsp" />
<div class="container">
    <h2 class="mb-3">订单详情</h2>
    <c:if test="${order == null}">
        <div class="alert alert-danger">订单不存在或不属于当前餐厅。</div>
    </c:if>
    <c:if test="${order != null}">
        <div class="card mb-3">
            <div class="card-body">
                <div class="row g-3">
                    <div class="col-md-3">订单ID：<span class="fw-bold">${order.orderId}</span></div>
                    <div class="col-md-3">顾客ID：<span class="fw-bold">${order.userId}</span></div>
                    <div class="col-md-3">下单时间：<span class="fw-bold"><fmt:formatDate value="${order.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></span></div>
                    <div class="col-md-3">总价：<span class="fw-bold"><fmt:formatNumber value="${order.totalPrice}" type="currency"/></span></div>
                </div>
                <div class="mt-2">当前状态：
                    <c:choose>
                        <c:when test="${order.status == 'PENDING'}"><span class="badge text-white bg-warning">待处理</span></c:when>
                        <c:when test="${order.status == 'PROCESSING'}"><span class="badge text-white bg-info">制作中</span></c:when>
                        <c:when test="${order.status == 'COMPLETED'}"><span class="badge text-white bg-success">已完成</span></c:when>
                        <c:when test="${order.status == 'CANCELLED'}"><span class="badge text-white bg-danger">已取消</span></c:when>
                        <c:otherwise><span class="badge text-white bg-secondary">${order.status}</span></c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <c:forEach var="entry" items="${itemGroups}">
            <div class="card mb-3">
                <div class="card-header">菜单：
                    <span class="fw-bold">${entry.key}</span>
                    <c:if test="${groupIsPackage[entry.key]}"><span class="ms-2 text-danger">【套餐】</span></c:if>
                    （合计：<fmt:formatNumber value="${groupTotals[entry.key]}" type="currency"/>）
                </div>
                <div class="card-body p-0">
                    <table class="table table-striped table-hover table-bordered mb-0">
                        <thead class="table-light">
                        <tr class="text-nowrap">
                            <th>菜名</th>
                            <th class="text-end">数量</th>
                            <th class="text-end">购买单价</th>
                            <th class="text-end">小计</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="it" items="${entry.value}">
                            <tr>
                                <td>${it.dishName}</td>
                                <td class="text-end">${it.quantity}</td>
                                <td class="text-end"><fmt:formatNumber value="${it.unitPrice}" type="currency"/></td>
                                <td class="text-end"><fmt:formatNumber value="${it.unitPrice * it.quantity}" type="currency"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:forEach>

        <div class="card mb-3">
            <div class="card-header">状态流转</div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${order.status == 'PENDING'}">
                        <form method="post" action="${pageContext.request.contextPath}/admin/orders" class="d-inline">
                            <input type="hidden" name="action" value="updateStatus"/>
                            <input type="hidden" name="orderId" value="${order.orderId}"/>
                            <input type="hidden" name="newStatus" value="PROCESSING"/>
                            <button type="submit" class="btn btn-info text-white">转为PROCESSING</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/admin/orders" class="d-inline ms-2">
                            <input type="hidden" name="action" value="updateStatus"/>
                            <input type="hidden" name="orderId" value="${order.orderId}"/>
                            <input type="hidden" name="newStatus" value="CANCELLED"/>
                            <input type="text" name="reason" class="form-control d-inline-block" style="width:220px;" placeholder="取消原因（可选）"/>
                            <button type="submit" class="btn btn-danger ms-1">取消订单</button>
                        </form>
                    </c:when>
                    <c:when test="${order.status == 'PROCESSING'}">
                        <form method="post" action="${pageContext.request.contextPath}/admin/orders" class="d-inline">
                            <input type="hidden" name="action" value="updateStatus"/>
                            <input type="hidden" name="orderId" value="${order.orderId}"/>
                            <input type="hidden" name="newStatus" value="COMPLETED"/>
                            <button type="submit" class="btn btn-success">标记完成</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/admin/orders" class="d-inline ms-2">
                            <input type="hidden" name="action" value="updateStatus"/>
                            <input type="hidden" name="orderId" value="${order.orderId}"/>
                            <input type="hidden" name="newStatus" value="CANCELLED"/>
                            <input type="text" name="reason" class="form-control d-inline-block" style="width:220px;" placeholder="取消原因（可选）"/>
                            <button type="submit" class="btn btn-danger ms-1">取消订单</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <span class="text-muted">订单已完成或已取消，不能再变更。</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </c:if>

    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/orders">返回列表</a>
</div>
<jsp:include page="/admin/footer.jsp" />