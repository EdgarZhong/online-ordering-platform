<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="header.jsp" />

<main>
    <div class="row">
        <div class="col-md-12">
            <h2>店铺信息</h2>
        </div>
    </div>

    <div class="row mt-3">
        <div class="col-md-8">
            <form method="post" action="${pageContext.request.contextPath}/admin/restaurant">
                <input type="hidden" name="action" value="update" />
                <div class="mb-3">
                    <label class="form-label">店铺名称</label>
                    <input type="text" name="name" class="form-control" value="${restaurant.name}" required />
                </div>
                <div class="mb-3">
                    <label class="form-label">地址</label>
                    <input type="text" name="address" class="form-control" value="${restaurant.address}" />
                </div>
                <div class="mb-3">
                    <label class="form-label">电话</label>
                    <input type="text" name="phone" class="form-control" value="${restaurant.phone}" />
                </div>
                <div class="mb-3">
                    <label class="form-label">Logo URL</label>
                    <input type="text" name="logoUrl" class="form-control" value="${restaurant.logoUrl}" />
                </div>
                <div class="mb-3">
                    <label class="form-label">店铺介绍</label>
                    <textarea name="description" class="form-control" rows="4">${restaurant.description}</textarea>
                </div>
                <button type="submit" class="btn btn-primary">保存</button>
            </form>
        </div>
    </div>
</main>

<jsp:include page="footer.jsp" />