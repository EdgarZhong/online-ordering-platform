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
    </div>
    <pre id="out5"></pre>
</div>

</body>
</html>