<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Error Page</title>
</head>
<body>
      <h4>The application has malfunctioned.</h4>

	  <h4>Exception Name: <s:property value="exception"/></h4>
	  <h4>Exception Details: <s:property value="exceptionStack"/></h4>
      
</body>
</html>