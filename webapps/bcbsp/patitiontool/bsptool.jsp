<%@ page contentType="text/html; charset=gb2312" language="java" import="java.io.*" errorPage="" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type"
			content="text/html; charset=gb2312">
		<title>BSP tool Instruction</title>
	</head>
	<body>
		<div>
		<jsp:include page="bsptop.jsp"/>                            
		</div><br/>
		<div id="main">
			<h1>Ranger Numbering 
		</h1>
			<h2>
				Introduce
			</h2>
			<p>����ֱ����Ϊ���ֹ���ʹ�ã�����ͬ�ķ������ݽ��������Ķ�����,Ҳ������Ϊ�������ֹ��ߵĺ�����Ŵ���  
	    </p>
	    <h2>
				DP(Distributed Partition)��
			</h2>
			<p>�ֲ�ʽ�Ļ��ֹ��ߣ���������ʽ�Ļ����㷨���л��֣�����һ���̶ȵ������ܼ��͡�  
	    </p>
	    <h2>
			DBP(Distributed with Bucket Partition)��
			</h2>
			<p>�ֲ�ʽ�Ļ��ֹ��ߣ���������ʽ�����㷨���л��֣�����������Ͱ���飬����һ���������ܼ��͡�  
	    </p>
			<br>
			<a href="loadrange.jsp">StartTo get range numbering code.</a>
			<br>
			<a href="loaddb.jsp">StartTo get db&&dbp  code.</a>
		<br><br></body>
	  </div>
</html>
