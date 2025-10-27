<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:include page="header.jsp" />

<!-- 仪表盘主体内容 -->
<main>
    <div class="row">
        <div class="col-md-12">
            <h2>仪表盘</h2>
            <p>欢迎来到商户管理后台。这里将显示您的关键业务数据。</p>
        </div>
    </div>

    <div class="row g-4 mt-2">
        <div class="col-md-4">
            <div class="card text-white bg-primary">
                <div class="card-body">
                    <h5 class="card-title">今日新订单</h5>
                    <p class="card-text fs-3">12</p>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card text-white bg-success">
                <div class="card-body">
                    <h5 class="card-title">今日总收入</h5>
                    <p class="card-text fs-3">¥ 865.50</p>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card text-white bg-warning">
                <div class="card-body">
                    <h5 class="card-title">待处理事项</h5>
                    <p class="card-text fs-3">3</p>
                </div>
            </div>
        </div>
    </div>

</main>

<jsp:include page="footer.jsp" />
