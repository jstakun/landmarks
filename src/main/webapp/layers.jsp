<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Layers finder</title>
</head>
<body>
<h3>Select layer to find landmarks:</h3>
	<s:form action="/actions/layerLandmarks">
      	<s:select name="layerForm.layer" 
      	          label="Layer" 
      	          list="layers"
      	          value="defaultLayer"/>
      	<s:textfield name="layerForm.latitude" label="Latitude" value="52.251234"/>
      	<s:textfield name="layerForm.longitude" label ="Longitude" value="20.951234"/> 
      	<s:textfield name="layerForm.radius" label="Radius in KM" value="10"/>
      	<s:textfield name="layerForm.limit" label="Limit" value="30"/>
      	<s:select label="Format"
       			  name="layerForm.format"
       			  list="#{'json':'JSON', 'deflate':'Binary'}"
       			  value="defaultFormat"/>
      	<s:select label="Display"
       			  name="layerForm.display"
       			  list="#{'s':'Small', 'n':'Normal', 'l':'Large', 'xl':'Extra Large'}"
       			  value="defaultDisplay"/>
    	<s:submit/>   
	</s:form> 
</body>
</html>