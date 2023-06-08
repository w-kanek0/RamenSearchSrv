<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
	if(session.getAttribute("userId") != null){
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1 ">
<meta http-equiv="Content-Script-Type" content="text/javascript">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/ramencommon.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/ramenreviewlist.css">
<script src="${pageContext.request.contextPath}/js/ramenscript.js"></script>
<title>${ramen.shopName} の口コミ</title>
</head>
<body>
<div id="wrap">
    <div class="header">
        こんにちは、${userName} さん<br />
        <form action="Logout" method="POST"><input type="submit" class="btn btn-clear" value="ログアウト"></form>
    </div>
    <h3>${ramen.shopName} の口コミ一覧</h3>
    <hr> 
    <c:forEach items="${reviewList}" var="review">
    <table class="review-list">
    	<tr><td>${review.userid}さんの口コミ
    	<c:if test="${userId == review.userid}">
    		<div class="right"><form style="display:inline" action="DspRamenReviewUpdate" method="POST">
				<input type="hidden" name="reviewid" value="${review.reviewid}"/>
				<input type="hidden" name="shopid" value="${ramen.shopid}"/>
				<input type="hidden" name="name" value="${ramen.shopName}"/>
				<input type="submit" id="register" class="btn btn-update" value="編集">
			</form>
			<form style="display:inline" action="RamenReviewDelete" method="POST">
				<input type="hidden" name="reviewid" value="${review.reviewid}"/>
				<input type="hidden" name="shopid" value="${ramen.shopid}"/>
				<input type="submit" class="btn btn-delete" id="3" value="削除" onclick="return confirm_review(this.id)"/>
			</form></div>
    	</c:if>
    	</td></tr>
    	<tr><td>評価：${review.valueLabel}</td></tr>
    	<tr><td>来訪日：${review.visitday.toString().substring(0, 10)}</td></tr>
    	<tr><td>投稿日時：${review.registerdate.toString().substring(0, 16)}</td></tr>
    	<tr><td>更新日時：${review.lastupdate.toString().substring(0, 16)}</td></tr>
    	<tr><td class="review-title">${review.reviewTitle}</td></tr>
    	<c:if test="${!empty review.filenames}">
    	<tr><td>
    		<c:forEach items="${review.filenames}" var="filename">
				<img src="${pageContext.request.contextPath}/upload/review/${filename}" width="100" height="80">　
			</c:forEach>
    	</td></tr>
    	</c:if>
    	<tr><td>${review.reviewBr}</td></tr>
    </table>
    </c:forEach>
	<div class="center" >
	<c:if test="${ramen.reviewCount == 0}">
    	<form style="display:inline" action="DspRamenReviewInsert" id="register" method="POST">
			<input type="hidden" name="shopid" value="${ramen.shopid}"/>
			<input type="hidden" name="name" value="${ramen.shopName}"/>
			<input type="hidden" name="userid" value="${userId}"/>
			<input type="submit" id="register" class="btn btn-search" value="口コミを投稿">
		</form>
	</c:if>
		<form style="display:inline" action="DspRamenSearch" method="post">
			<input type="submit" class="btn btn-clear" value="一覧に戻る" />
		</form>
	</div>
</div>
</body>
</html>
<%
}else{
	RequestDispatcher dispatcher = request.getRequestDispatcher("/RamenSearchSystem"); // ログイン画面遷移
	dispatcher.forward(request, response);
}
%>