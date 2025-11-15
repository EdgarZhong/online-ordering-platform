<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="header.jsp" />

<main>
    <div class="row">
        <div class="col-md-12">
            <h2>菜单管理</h2>
        </div>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger mt-3">${error}</div>
    </c:if>

    <div class="row mt-3">
        <c:choose>
            <c:when test="${empty editMenu}">
        <div class="col-md-6">
            <h4>菜单列表</h4>
            <table id="menu-list-table" class="table table-striped">
                <thead>
                <tr>
                    <th class="text-nowrap">序号</th>
                    <th class="text-nowrap">名称</th>
                    <th class="text-nowrap">套餐</th>
                    <th class="text-nowrap">描述</th>
                    <th class="text-nowrap">操作</th>
                    <th class="text-nowrap">拖拽以排序</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty menus}">
                        <c:forEach var="m" items="${menus}" varStatus="st">
                            <tr draggable="true" data-id="${m.menuId}">
                                <td>${st.count}</td>
                                <td>${m.name}</td>
                                <td><c:if test="${m['package']}">√</c:if></td>
                                <td>${m.description}</td>
                                <td class="text-nowrap">
                                    <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/admin/menus?action=edit&menuId=${m.menuId}">编辑</a>
                                    <c:choose>
                                        <c:when test="${selectedMenuId == m.menuId}">
                                            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/menus?action=collapse">收起</a>
                                        </c:when>
                                        <c:otherwise>
                                            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/menus?action=expand&menuId=${m.menuId}">展开</a>
                                        </c:otherwise>
                                    </c:choose>
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/menus">
                                        <input type="hidden" name="action" value="delete" />
                                        <input type="hidden" name="menuId" value="${m.menuId}" />
                                        <button type="submit" class="btn btn-sm btn-outline-danger" onclick="return confirm('确认删除该菜单？删除后不可恢复')">删除</button>
                                    </form>
                                </td>
                                <td class="text-muted drag-handle">☰</td>
                            </tr>
                        </c:forEach>
                        <tr>
                            <td colspan="6">
                                <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/menus?action=new">新建菜单</a>
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="6" class="text-muted">暂无菜单</td></tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
            <c:if test="${not empty selectedMenuId && empty editMenu}">
                <h5 class="mt-3">${selectedMenuName}包含的菜品</h5>
                <table class="table table-striped">
                    <thead>
                    <tr>
                        <th class="text-nowrap">菜品名称</th>
                        <th class="text-nowrap">菜单内单价</th>
                        <th class="text-nowrap">数量</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="mi" items="${menuItems}">
                        <tr>
                            <td>
                                <c:forEach var="d" items="${dishes}">
                                    <c:if test="${d.dishId == mi.dishId}">${d.name}</c:if>
                                </c:forEach>
                            </td>
                            <td><fmt:formatNumber value="${mi.price}" type="number" minFractionDigits="2" /></td>
                            <td>${mi.quantity}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </div>
        
            </c:when>
            <c:otherwise>
                <div class="col-md-12">
                    <jsp:include page="menu-edit-panel.jsp" />
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <script>
    function copyDraftFields(form){
      var nameInput = document.getElementById('menuName');
      var descInput = document.getElementById('menuDesc');
      var pkgInput = document.getElementById('isPackageEdit');
      if(form.elements['draftName']) form.elements['draftName'].value = nameInput ? nameInput.value : '';
      if(form.elements['draftDescription']) form.elements['draftDescription'].value = descInput ? descInput.value : '';
      if(form.elements['draftIsPackage']) form.elements['draftIsPackage'].value = (pkgInput && pkgInput.checked) ? 'on' : '';
    }
    (function(){
      var tbody=document.querySelector('#menu-list-table tbody');
      if(!tbody) return;
      var dragSrc;
      var allowRowDrag=false;
      tbody.addEventListener('mousedown',function(e){ allowRowDrag=!!e.target.closest('.drag-handle'); });
      tbody.addEventListener('dragstart',function(e){
        var tr=e.target.closest('tr[draggable="true"]');
        if(!tr) return; if(!allowRowDrag){ e.preventDefault(); return; } dragSrc=tr; e.dataTransfer.effectAllowed='move';
      });
      tbody.addEventListener('dragover',function(e){ e.preventDefault(); });
      tbody.addEventListener('drop',function(e){
        e.preventDefault();
        var tr=e.target.closest('tr[draggable="true"]');
        if(!tr||tr===dragSrc) return;
        var rect=tr.getBoundingClientRect();
        var before=(e.clientY-rect.top)<rect.height/2;
        tbody.insertBefore(dragSrc, before? tr : tr.nextSibling);
        var rows=tbody.querySelectorAll('tr[draggable="true"]');
        var ids=[]; rows.forEach(function(r,idx){ ids.push(r.getAttribute('data-id')); r.children[0].textContent=idx+1; });
        var body='action=reorder&order='+encodeURIComponent(ids.join(','));
        fetch('${pageContext.request.contextPath}/admin/menus',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:body});
        allowRowDrag=false;
      });
    })();
    </script>

    <!-- 只读菜品列表（用于菜单管理页底部），不提供创建/删除；在编辑菜单或选择菜单时可添加到菜单 -->
    <div class="row mt-4">
        <div class="col-md-12">
            <h4>菜品列表</h4>
            <table class="table table-striped">
                    <thead>
                    <tr>
                        <th class="text-nowrap">名称</th>
                        <th class="text-nowrap">默认价</th>
                        <th class="text-nowrap">创建日期</th>
                        <th class="text-nowrap">描述</th>
                        <c:if test="${not empty editMenu}"><th class="text-nowrap">添加到菜单</th></c:if>
                    </tr>
                    </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty dishes}">
                        <c:forEach var="d" items="${dishes}">
                            <tr>
                                <td>${d.name}</td>
                                <td><fmt:formatNumber value="${d.defaultPrice}" type="number" minFractionDigits="2" /></td>
                                <td><fmt:formatDate value="${d.createdAt}" pattern="yyyy-MM-dd HH:mm" /></td>
                                <td>${d.description}</td>
                                <c:if test="${not empty editMenu}">
                                    <td>
                                        <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/menus" onsubmit="copyDraftFields(this)">
                                            <input type="hidden" name="action" value="addDish" />
                                            <input type="hidden" name="menuId" value="${editMenu.menuId}" />
                                            <input type="hidden" name="dishId" value="${d.dishId}" />
                                            <input type="hidden" name="draftName" />
                                            <input type="hidden" name="draftDescription" />
                                            <input type="hidden" name="draftIsPackage" />
                                            <input type="number" step="0.01" min="0" name="price" value="${d.defaultPrice}" class="form-control form-control-sm d-inline" style="width:120px" />
                                            <button type="submit" class="btn btn-sm btn-outline-primary">添加</button>
                                        </form>
                                    </td>
                                </c:if>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="${not empty editMenu ? 5 : 4}">
                                <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/dishes?mode=new">去菜品页添加菜品</a>
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    
    
    
</main>

<jsp:include page="footer.jsp" />