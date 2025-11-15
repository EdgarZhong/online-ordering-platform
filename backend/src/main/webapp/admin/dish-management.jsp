<!--
 * @Author: EdgarZhong 18518713412@163.com
 * @Date: 2025-11-15 00:03:24
 * @LastEditors: EdgarZhong 18518713412@163.com
 * @LastEditTime: 2025-11-15 00:03:28
 * @FilePath: \final\online-ordering-platform\backend\src\main\webapp\admin\dish-management.jsp
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="header.jsp" />

<main>
    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>
    <div class="row">
        <div class="col-md-6">
            <h4>菜品列表</h4>
            <table class="table table-striped">
                <thead>
                <tr>
                    <th class="text-nowrap">名称</th>
                    <th class="text-nowrap">默认价格</th>
                    <th class="text-nowrap">图片</th>
                    <th class="text-nowrap">描述</th>
                    <th class="text-nowrap">创建日期</th>
                    <th class="text-nowrap">操作</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty dishes}">
                        <c:forEach var="d" items="${dishes}">
                            <tr>
                                <td>${d.name}</td>
                                <td><fmt:formatNumber value="${d.defaultPrice}" type="number" minFractionDigits="2" /></td>
                                <td><c:if test="${not empty d.imageUrl}"><img src="${d.imageUrl}" style="height:40px"/></c:if></td>
                                <td><c:out value="${d.description}"/></td>
                                <td><fmt:formatDate value="${d.createdAt}" pattern="yyyy-MM-dd HH:mm" /></td>
                                <td class="text-nowrap">
                                    <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/admin/dishes?mode=edit&dishId=${d.dishId}">编辑</a>
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/dishes">
                                        <input type="hidden" name="action" value="delete" />
                                        <input type="hidden" name="dishId" value="${d.dishId}" />
                                        <button type="submit" class="btn btn-sm btn-outline-danger">删除</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="6" class="text-muted">暂无菜品</td></tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/dishes?mode=new">新建菜品</a>
        </div>

        <div class="col-md-6">
            <c:if test="${newDish}">
                <h4>新建菜品</h4>
                <form method="post" action="${pageContext.request.contextPath}/admin/dishes">
                    <input type="hidden" name="action" value="create" />
                    <div class="mb-2">
                        <label class="form-label">名称</label>
                        <input type="text" name="name" class="form-control" required />
                    </div>
                    <div class="mb-2">
                        <label class="form-label">图片URL</label>
                        <input type="text" name="imageUrl" class="form-control" />
                    </div>
                    <div class="mb-2">
                        <label class="form-label">默认价格</label>
                        <input type="text" name="defaultPrice" class="form-control" placeholder="例如：32.00" required />
                    </div>
                    <div class="mb-2">
                        <label class="form-label">描述</label>
                        <textarea name="description" class="form-control" rows="3"></textarea>
                    </div>
                    <button type="submit" class="btn btn-primary">确认保存</button>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/dishes">取消新建</a>
                </form>
            </c:if>

            <c:if test="${not empty editDish}">
                <h4>编辑菜品</h4>
                <form id="editDishForm" method="post" action="${pageContext.request.contextPath}/admin/dishes">
                    <input type="hidden" name="action" value="update" />
                    <input type="hidden" name="dishId" value="${editDish.dishId}" />
                    <div class="mb-2">
                        <label class="form-label">名称</label>
                        <input type="text" name="name" class="form-control" value="${editDish.name}" required />
                    </div>
                    <div class="mb-2">
                        <label class="form-label">图片URL</label>
                        <input type="text" name="imageUrl" class="form-control" value="${editDish.imageUrl}" />
                    </div>
                    <div class="mb-2">
                        <label class="form-label">默认价格</label>
                        <input type="text" name="defaultPrice" class="form-control" value="${editDish.defaultPrice}" placeholder="例如：32.00" required />
                    </div>
                    <div class="mb-2">
                        <label class="form-label">描述</label>
                        <textarea name="description" class="form-control" rows="3">${editDish.description}</textarea>
                    </div>
                    <button type="submit" class="btn btn-primary">确认修改</button>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/dishes">取消修改</a>
                </form>
                <script>
                  document.addEventListener('DOMContentLoaded', function(){
                    var form = document.getElementById('editDishForm')
                    if (!form) return
                    var priceInput = form.querySelector('input[name="defaultPrice"]')
                    if (!priceInput) return
                    var original = parseFloat(priceInput.value)
                    form.addEventListener('submit', function(e){
                      var current = parseFloat(priceInput.value)
                      if (!isNaN(original) && !isNaN(current) && current !== original) {
                        var ok = window.confirm('您正在修改菜品默认价格，菜单中的菜品价格将不会更改。\n如果您希望修改菜单中的菜品实际价格，请前往菜单管理修改。')
                        if (!ok) { e.preventDefault() }
                      }
                    })
                  })
                </script>
            </c:if>
        </div>
    </div>
</main>

<jsp:include page="footer.jsp" />