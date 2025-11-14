<!--
 * @Author: EdgarZhong 18518713412@163.com
 * @Date: 2025-11-15 00:05:55
 * @LastEditors: EdgarZhong 18518713412@163.com
 * @LastEditTime: 2025-11-15 00:05:59
 * @FilePath: \final\online-ordering-platform\backend\src\main\webapp\admin\menu-edit-panel.jsp
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${not empty editMenu}">
    <div class="row mt-4">
        <div class="col-md-12">
            <h4>编辑菜单</h4>
            <form id="menu-edit-form" method="post" action="${pageContext.request.contextPath}/admin/menus">
                <input type="hidden" name="action" value="update" />
                <input type="hidden" name="menuId" value="${editMenu.menuId}" />
                <div class="mb-2">
                    <label class="form-label">名称</label>
                    <input type="text" id="menuName" name="name" class="form-control" value="${editMenu.name}" required />
                </div>
                <div class="mb-2">
                    <label class="form-label">描述</label>
                    <input type="text" id="menuDesc" name="description" class="form-control" value="${editMenu.description}" />
                </div>
                <div class="form-check mb-2">
                    <input class="form-check-input" type="checkbox" name="isPackage" id="isPackageEdit" <c:if test='${editMenu["package"]}'>checked</c:if> />
                    <label class="form-check-label" for="isPackageEdit">设置为套餐</label>
                </div>
                <button type="submit" class="btn btn-primary">保存</button>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/menus">取消</a>
        </div>
    </div>

    <c:if test="${true}">
            <table id="menu-items-table" class="table table-striped mt-3">
                <thead>
                <tr><th>序号</th><th>菜品名称</th><th>价格</th><th>数量</th><th>拖拽</th></tr>
                </thead>
                <tbody>
                <c:set var="draft" value="${sessionScope.menuDraft}" />
                <c:choose>
                    <c:when test="${not empty draft and not empty draft.items}">
                        <c:forEach var="di" items="${draft.items}" varStatus="st">
                            <tr draggable="true" data-id="" >
                                <td>${st.count}<input type="hidden" name="menuItemId" value="" /></td>
                                <td>
                                    <c:forEach var="d" items="${dishes}">
                                        <c:if test='${d.dishId == di.dishId}'>${d.name}</c:if>
                                    </c:forEach>
                                    <input type="hidden" name="dishId" value="${di.dishId}" />
                                </td>
                                <td><input type="number" step="0.01" min="0" name="price" value="${di.price}" class="form-control form-control-sm" /></td>
                                <td>
                                    <div class="input-group input-group-sm" style="width:140px;">
                                        <button class="btn btn-outline-secondary" type="button" onclick="var i=this.nextElementSibling; i.value=Math.max(0,parseInt(i.value||'0')-1)">-</button>
                                        <input type="number" min="0" name="quantity" value="${di.quantity}" class="form-control" />
                                        <button class="btn btn-outline-secondary" type="button" onclick="var i=this.previousElementSibling; i.value=parseInt(i.value||'0')+1">+</button>
                                    </div>
                                </td>
                                <td class="text-muted">☰</td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${editMenu.menuId > 0}">
                            <c:choose>
                            <c:when test="${not empty menuItems}">
                                <c:forEach var="mi" items="${menuItems}" varStatus="st">
                                    <tr draggable="true" data-id="${mi.menuItemId}">
                                        <td>${st.count}<input type="hidden" name="menuItemId" value="${mi.menuItemId}" /></td>
                                        <td>
                                            <c:forEach var="d" items="${dishes}">
                                                <c:if test='${d.dishId == mi.dishId}'>${d.name}</c:if>
                                            </c:forEach>
                                            <input type="hidden" name="dishId" value="${mi.dishId}" />
                                        </td>
                                        <td><input type="number" step="0.01" min="0" name="price" value="${mi.price}" class="form-control form-control-sm" /></td>
                                        <td>
                                            <div class="input-group input-group-sm" style="width:140px;">
                                                <button class="btn btn-outline-secondary" type="button" onclick="var i=this.nextElementSibling; i.value=Math.max(0,parseInt(i.value||'0')-1)">-</button>
                                                <input type="number" min="0" name="quantity" value="${mi.quantity}" class="form-control" />
                                                <button class="btn btn-outline-secondary" type="button" onclick="var i=this.previousElementSibling; i.value=parseInt(i.value||'0')+1">+</button>
                                            </div>
                                        </td>
                                        <td class="text-muted">☰</td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="5" class="text-muted">当前菜单暂无菜品，请在下方列表点击“添加”</td></tr>
                            </c:otherwise>
                            </c:choose>
                        </c:if>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
            <small class="text-muted">数量为0的菜品将自动删除</small>
            </form>
    </c:if>
    
    <script>
    (function(){
      var table=document.getElementById('menu-items-table');
      var form=document.getElementById('menu-edit-form');
      if(!table||!form) return;
      var tbody=table.querySelector('tbody');
      var dragSrc;
      tbody.addEventListener('dragstart',function(e){ var tr=e.target.closest('tr[draggable="true"]'); if(!tr) return; dragSrc=tr; e.dataTransfer.effectAllowed='move'; });
      tbody.addEventListener('dragover',function(e){ e.preventDefault(); });
      tbody.addEventListener('drop',function(e){ e.preventDefault(); var tr=e.target.closest('tr[draggable="true"]'); if(!tr||tr===dragSrc) return; var rect=tr.getBoundingClientRect(); var before=(e.clientY-rect.top)<rect.height/2; tbody.insertBefore(dragSrc, before? tr : tr.nextSibling); updateOrderInputs(); });
      function updateOrderInputs(){ var rows=tbody.querySelectorAll('tr[draggable="true"]'); rows.forEach(function(r,idx){ var seqCell=r.children[0]; if(seqCell) seqCell.firstChild.nodeValue=idx+1; var id=r.getAttribute('data-id'); var h=r.querySelector('input[name="sortOrder"]'); if(!h){ h=document.createElement('input'); h.type='hidden'; h.name='sortOrder'; r.appendChild(h); }
        h.value=idx; }); }
      form.addEventListener('submit',function(){ updateOrderInputs(); });
    })();
    </script>
</c:if>