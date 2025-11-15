<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>API 测试用例</title>
    <meta charset="UTF-8" />
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        code { background: #f6f8fa; padding: 2px 4px; }
        pre { background: #f6f8fa; padding: 10px; overflow: auto; }
        .endpoint { margin-bottom: 16px; }
    </style>
    <script>
        async function hit(url, targetId){
            const res = await fetch(url);
            const text = await res.text();
            document.getElementById(targetId).textContent = text;
        }
    </script>
</head>
<body>
<h2>API 测试用例（无前端部署验证）</h2>
<div class="endpoint">
    <div>
        <b>GET</b> <code><%=request.getContextPath()%>/api/restaurants</code>
        <button onclick="hit('<%=request.getContextPath()%>/api/restaurants','out1')">请求</button>
    </div>
    <pre id="out1"></pre>
</div>

<div class="endpoint">
    <div>
        <b>GET</b> <code><%=request.getContextPath()%>/api/restaurants/{id}</code>
        <input type="number" id="rid" value="1" />
        <button onclick="hit('<%=request.getContextPath()%>/api/restaurants/'+document.getElementById('rid').value,'out2')">请求</button>
    </div>
    <pre id="out2"></pre>
    <div>
        <b>GET</b> <code><%=request.getContextPath()%>/api/restaurants/{id}/menus</code>
        <input type="number" id="rid2" value="1" />
        <button onclick="hit('<%=request.getContextPath()%>/api/restaurants/'+document.getElementById('rid2').value+'/menus','out3')">请求</button>
    </div>
    <pre id="out3"></pre>
</div>

<div class="endpoint">
    <div>
        <b>GET</b> <code><%=request.getContextPath()%>/api/menus/{menuId}/items</code>
        <input type="number" id="mid" value="1" />
        <button onclick="hit('<%=request.getContextPath()%>/api/menus/'+document.getElementById('mid').value+'/items','out4')">请求</button>
    </div>
    <pre id="out4"></pre>
</div>

 

<div class="endpoint">
    <div>
        <b>GET</b> <code><%=request.getContextPath()%>/api/orders/{orderId}</code>
        <input type="number" id="oid" value="1" />
        <button onclick="hit('<%=request.getContextPath()%>/api/orders/'+document.getElementById('oid').value,'out5')">请求</button>
        <button onclick="hit('<%=request.getContextPath()%>/api/orders/1','out5')">查询订单A(1)</button>
        <button onclick="hit('<%=request.getContextPath()%>/api/orders/2','out5')">查询订单B(2)</button>
        <button onclick="hit('<%=request.getContextPath()%>/api/orders/3','out5')">查询订单C(3)</button>
        <button onclick="hit('<%=request.getContextPath()%>/api/orders/4','out5')">查询订单D(4)</button>
    </div>
    <pre id="out5"></pre>
</div>

<div class="endpoint">
    <div>
        <b>POST</b> <code><%=request.getContextPath()%>/api/orders</code>
        <textarea id="orderJson" rows="8" style="width:100%">{"restaurantId":1,"menus":[{"menuId":2,"quantity":2,"items":[{"dishId":1,"sortOrder":1,"quantity":1},{"dishId":3,"sortOrder":2,"quantity":1}]},{"menuId":1,"quantity":0,"items":[{"dishId":1,"sortOrder":1,"quantity":1}]}]}</textarea>
        <button onclick="fetch('<%=request.getContextPath()%>/api/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:document.getElementById('orderJson').value}).then(r=>r.text()).then(t=>document.getElementById('out6').textContent=t)">下单</button>
    </div>
    <pre id="out6"></pre>
</div>

<div class="endpoint">
    <div>
        <b>POST 错误</b> 跨餐厅
        <textarea id="orderJsonErr1" rows="6" style="width:100%">{"restaurantId":1,"menus":[{"menuId":3,"quantity":1,"items":[{"dishId":6,"sortOrder":1,"quantity":1}]}]}</textarea>
        <button onclick="fetch('<%=request.getContextPath()%>/api/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:document.getElementById('orderJsonErr1').value}).then(r=>r.text()).then(t=>document.getElementById('out7').textContent=t)">触发400</button>
    </div>
    <pre id="out7"></pre>
</div>

<div class="endpoint">
    <div>
        <b>POST 错误</b> 无效 menuId+dishId
        <textarea id="orderJsonErr2" rows="8" style="width:100%">{"restaurantId":1,"menus":[{"menuId":2,"quantity":2,"items":[{"dishId":1,"sortOrder":1,"quantity":1}]}]}</textarea>
        <button onclick="fetch('<%=request.getContextPath()%>/api/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:document.getElementById('orderJsonErr2').value}).then(r=>r.text()).then(t=>document.getElementById('out8').textContent=t)">触发400(套餐不匹配)</button>

<div class="endpoint">
    <div>
        <b>POST 错误</b> 非套餐非法菜品
        <textarea id="orderJsonErr3" rows="6" style="width:100%">{"restaurantId":1,"menus":[{"menuId":1,"quantity":0,"items":[{"dishId":999,"sortOrder":1,"quantity":1}]}]}</textarea>
        <button onclick="fetch('<%=request.getContextPath()%>/api/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:document.getElementById('orderJsonErr3').value}).then(r=>r.text()).then(t=>document.getElementById('out9').textContent=t)">触发400(非法菜品)</button>
    </div>
    <pre id="out9"></pre>
</div>
    </div>
    <pre id="out8"></pre>
</div>

<div class="endpoint">
    <div>
        <b>POST 错误</b> 套餐项数量不匹配
        <textarea id="orderJsonErr4" rows="8" style="width:100%">{"restaurantId":1,"menus":[{"menuId":2,"quantity":1,"items":[{"dishId":1,"sortOrder":1,"quantity":99},{"dishId":3,"sortOrder":2,"quantity":1}]}]}</textarea>
        <button onclick="fetch('<%=request.getContextPath()%>/api/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:document.getElementById('orderJsonErr4').value}).then(r=>r.text()).then(t=>document.getElementById('out10').textContent=t)">触发400(套餐不匹配-数量)</button>
    </div>
    <pre id="out10"></pre>
</div>

<div class="endpoint">
    <div>
        <b>POST 错误</b> 套餐缺少菜品
        <textarea id="orderJsonErr5" rows="8" style="width:100%">{"restaurantId":1,"menus":[{"menuId":2,"quantity":1,"items":[{"dishId":1,"sortOrder":1,"quantity":1}]}]}</textarea>
        <button onclick="fetch('<%=request.getContextPath()%>/api/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:document.getElementById('orderJsonErr5').value}).then(r=>r.text()).then(t=>document.getElementById('out11').textContent=t)">触发400(套餐不匹配-缺项)</button>
    </div>
    <pre id="out11"></pre>
</div>

</body>
</html>