<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="/admin/header.jsp" />

<main class="container mt-3">
  <h3>租户隔离验证页</h3>
  <%
    Integer rid = (Integer) request.getAttribute("restaurantId");
    if (rid == null) {
      com.platform.ordering.model.User u = (com.platform.ordering.model.User) session.getAttribute("user");
      if (u != null) rid = u.getRestaurantId();
    }
    java.util.List<com.platform.ordering.model.Menu> menus = null;
    java.util.List<com.platform.ordering.model.Dish> dishes = null;
    if (rid != null) {
      menus = new com.platform.ordering.dao.MenuDAOImpl().listByRestaurant(rid);
      dishes = new com.platform.ordering.dao.DishDAOImpl().listByRestaurant(rid);
      request.setAttribute("menus", menus);
      request.setAttribute("dishes", dishes);
    }
    request.setAttribute("restaurantId", rid);
  %>
  <c:set var="restaurantId" value="${requestScope.restaurantId}" />
  <c:if test="${empty restaurantId}">
    <div class="alert alert-warning">当前无餐厅上下文，请在商户登录后访问本页面。</div>
  </c:if>
  <c:if test="${not empty restaurantId}">
    <div class="alert alert-info">当前餐厅ID：${restaurantId}</div>
    <div class="row">
      <div class="col-md-6">
        <h5>本餐厅的菜单列表</h5>
        <table class="table table-striped">
          <thead>
            <tr>
              <th class="text-nowrap">菜单名称</th>
              <th class="text-nowrap">描述</th>
            </tr>
          </thead>
          <tbody>
            <c:set var="menus" value="${requestScope.menus}" />
            <c:choose>
              <c:when test="${empty menus}">
                <tr><td colspan="2" class="text-muted">无数据</td></tr>
              </c:when>
              <c:otherwise>
                <c:forEach var="m" items="${menus}">
                  <tr>
                    <td>${m.name}</td>
                    <td>${m.description}</td>
                  </tr>
                </c:forEach>
              </c:otherwise>
            </c:choose>
          </tbody>
        </table>
      </div>
      <div class="col-md-6">
        <h5>本餐厅的菜品列表</h5>
        <table class="table table-striped">
          <thead>
            <tr>
              <th class="text-nowrap">名称</th>
              <th class="text-nowrap">创建日期</th>
            </tr>
          </thead>
          <tbody>
            <c:set var="dishes" value="${requestScope.dishes}" />
            <c:choose>
              <c:when test="${empty dishes}">
                <tr><td colspan="2" class="text-muted">无数据</td></tr>
              </c:when>
              <c:otherwise>
                <c:forEach var="d" items="${dishes}">
                  <tr>
                    <td>${d.name}</td>
                    <td><fmt:formatDate value="${d.createdAt}" pattern="yyyy-MM-dd HH:mm" /></td>
                  </tr>
                </c:forEach>
              </c:otherwise>
            </c:choose>
          </tbody>
        </table>
      </div>
    </div>
  </c:if>
</main>

<jsp:include page="/admin/footer.jsp" />
