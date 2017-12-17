<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Device Command Form</title>
</head>
<body>
	<em>Send command to registered device:
   </em>
   <form action="/actions/commandDevice" method="post">
   		<label for="imei">IMEI*</label><br/>
   		<input type="text" name="imei" required/><br/>
   		<label for="pin">PIN*</label><br/>
   		<input type="password" name="pin" required/><br/>
   		<label for="command">Command*</label><br/>
   		 <select>
  			<option value="pingdlt">Ping</option>
 			<option value="locatedlt">Locate</option>
		</select> <br/>
   		<label for="params">Command parameters</label><br/>
   		<input type="text" name="params"/><br/>
   		<input type="submit" value="Send Command"/>
   </form>
</body>
</html>