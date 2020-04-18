<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2020/4/11
  Time: 15:50
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Formatter</title>
</head>
<body>
    <h3> Excel 清除空格与换行 </h3>
    <br>
    <form action="/generator/sysUser/uploadExcel" enctype="multipart/form-data" method="post">
        <input type="file" name="fileName"><br><br>
        <input type="submit" value="上传">
    </form>

</body>
</html>
