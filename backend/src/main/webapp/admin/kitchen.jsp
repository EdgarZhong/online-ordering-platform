<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="/admin/header.jsp" />
<div class="container">
    <h2 class="mb-3">厨房/出餐面板</h2>
    <div class="row g-3">
        <div class="col-md-6">
            <h4 class="mb-2">待处理 (PENDING)</h4>
            <c:if test="${empty pendingGroups}"><div class="alert alert-secondary">暂无订单</div></c:if>
            <c:forEach var="g" items="${pendingGroups}">
                <h5 class="mt-2">${g.key}</h5>
                <c:forEach var="o" items="${g.value}">
                    <div class="card mb-2">
                        <div class="card-body">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <div>流水号：${o.serialNumber}
                                                <c:choose>
                                                    <c:when test="${o.status == 'PENDING'}"><span class="badge text-white bg-warning ms-1">待处理</span></c:when>
                                                    <c:when test="${o.status == 'PROCESSING'}"><span class="badge text-white bg-info ms-1">制作中</span></c:when>
                                                    <c:when test="${o.status == 'COMPLETED'}"><span class="badge text-white bg-success ms-1">已完成</span></c:when>
                                                    <c:when test="${o.status == 'CANCELLED'}"><span class="badge text-white bg-danger ms-1">已取消</span></c:when>
                                                </c:choose>
                                            </div>
                                            <div class="text-muted">时间：<fmt:formatDate value="${o.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></div>
                                            <div>总价：<span class="fw-bold"><fmt:formatNumber value="${o.totalPrice}" type="currency"/></span></div>
                                        </div>
                                        <div>
                                            <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/kitchen">
                                                <input type="hidden" name="action" value="updateStatus"/>
                                                <input type="hidden" name="orderId" value="${o.orderId}"/>
                                                <input type="hidden" name="newStatus" value="PROCESSING"/>
                                                <button type="submit" class="btn btn-sm btn-info text-white">开始备餐</button>
                                            </form>
                                            <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/kitchen">
                                                <input type="hidden" name="action" value="updateStatus"/>
                                                <input type="hidden" name="orderId" value="${o.orderId}"/>
                                                <input type="hidden" name="newStatus" value="CANCELLED"/>
                                                <input type="text" name="reason" class="form-control form-control-sm d-inline-block" style="width:160px;" placeholder="取消原因（可选）"/>
                                                <button type="submit" class="btn btn-sm btn-danger ms-1">取消</button>
                                            </form>
                                        </div>
                                    </div>
                                    <div class="mt-2">
                                        <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/orders/${o.orderId}">查看详情</a>
                                    </div>
                        </div>
                    </div>
                </c:forEach>
                <hr class="mt-3 mb-2" />
            </c:forEach>
        </div>
        <div class="col-md-6">
            <h4 class="mb-2">备餐中 (PROCESSING)</h4>
            <c:if test="${empty processingGroups}"><div class="alert alert-secondary">暂无订单</div></c:if>
            <c:forEach var="g" items="${processingGroups}">
                <h5 class="mt-2">${g.key}</h5>
                <c:forEach var="o" items="${g.value}">
                    <div class="card mb-2">
                        <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div>流水号：${o.serialNumber}
                                    <c:choose>
                                        <c:when test="${o.status == 'PENDING'}"><span class="badge bg-warning ms-1">待处理</span></c:when>
                                        <c:when test="${o.status == 'PROCESSING'}"><span class="badge bg-info ms-1">制作中</span></c:when>
                                        <c:when test="${o.status == 'COMPLETED'}"><span class="badge bg-success ms-1">已完成</span></c:when>
                                        <c:when test="${o.status == 'CANCELLED'}"><span class="badge bg-danger ms-1">已取消</span></c:when>
                                    </c:choose>
                                </div>
                                <div class="text-muted">时间：<fmt:formatDate value="${o.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></div>
                                <div>总价：<span class="fw-bold"><fmt:formatNumber value="${o.totalPrice}" type="currency"/></span></div>
                            </div>
                            <div>
                                <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/kitchen">
                                    <input type="hidden" name="action" value="updateStatus"/>
                                    <input type="hidden" name="orderId" value="${o.orderId}"/>
                                    <input type="hidden" name="newStatus" value="COMPLETED"/>
                                    <button type="submit" class="btn btn-sm btn-success">标记完成</button>
                                </form>
                                <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/kitchen">
                                    <input type="hidden" name="action" value="updateStatus"/>
                                    <input type="hidden" name="orderId" value="${o.orderId}"/>
                                    <input type="hidden" name="newStatus" value="CANCELLED"/>
                                    <input type="text" name="reason" class="form-control form-control-sm d-inline-block" style="width:160px;" placeholder="取消原因（可选）"/>
                                    <button type="submit" class="btn btn-sm btn-danger ms-1">取消</button>
                                </form>
                            </div>
                        </div>
                        <div class="mt-2">
                            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/orders/${o.orderId}">查看详情</a>
                        </div>
                    </div>
                </div>
                </c:forEach>
                <hr class="mt-3 mb-2" />
            </c:forEach>
        </div>
    </div>
    <div class="mt-3">
        <button onclick="location.reload()" class="btn btn-outline-secondary">刷新</button>
    </div>
</div>
<jsp:include page="/admin/footer.jsp" />
<script>
(function(){
  function connect(){
    try {
      var es = new EventSource("${pageContext.request.contextPath}/admin/kitchen/events");
      es.addEventListener("new_order", function(){ location.reload(); });
      es.addEventListener("order_updated", function(){ location.reload(); });
      es.addEventListener("hello", function(){});
      es.addEventListener("ping", function(){});
      es.onerror = function(){ try { es.close(); } catch(e){} setTimeout(connect, 3000); };
    } catch(e) { setTimeout(connect, 3000); }
  }
  connect();
})();
</script>