<%--
  Created by IntelliJ IDEA.
  Date: 2020/4/11
  Time: 15:50
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Formatter And TE</title>
    <script type="text/javascript" src="/js/jquery.min.js"></script>
    <script type="text/javascript" src="/js/jquery.jsonview.js"></script>
    <link rel="stylesheet" href="/js/jquery.jsonview.css">
</head>
<body>
<div align="center">
    <h3>Query by ID in private</h3>
    <form action="/generator/productInfo/query_id" method="post">
        <input type="text" placeholder="Query ID" name="query">
        <input type="submit" name="query" value="post">
    </form>

    <hr/>
    <div id="json"></div>


    <script>
        var json = ${message}
        json  = JSON.stringify(json);
        $(function() {
            $("#json").JSONView(json);
        });
    </script>

    <div style="display: none"><h3> Excel 清除空格与换行 </h3>
        <br>
        <form action="/generator/sysUser/uploadExcel" enctype="multipart/form-data" method="post">
            <input type="file" name="fileName"><br><br>
            <input type="submit" value="上传">
        </form>
    </div>
</div>


</body>
</html>
