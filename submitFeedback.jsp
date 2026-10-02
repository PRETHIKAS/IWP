<%@ page import="java.util.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    // Get form data
    String username = request.getParameter("username");
    String feedback = request.getParameter("feedback");

    // Store in session
    session.setAttribute("username", username);
    session.setAttribute("feedback", feedback);

    // Set cookie for last visited user
    Cookie userCookie = new Cookie("lastUser", username);
    userCookie.setMaxAge(60*60*24); // 1 day
    response.addCookie(userCookie);

    // Prepare list of feedback (in session)
    List<String> feedbackList = (List<String>) application.getAttribute("feedbackList");
    if (feedbackList == null) {
        feedbackList = new ArrayList<>();
    }
    feedbackList.add(username + ": " + feedback);
    application.setAttribute("feedbackList", feedbackList);
%>

<!DOCTYPE html>
<html>
<head>
    <title>Feedback Submitted</title>
</head>
<body>
    <h2>Thank You, <%= username %>!</h2>
    <p>Your feedback has been submitted successfully.</p>

    <h3>Previous Feedbacks:</h3>
    <ul>
        <c:forEach var="item" items="${applicationScope.feedbackList}">
            <li>${item}</li>
        </c:forEach>
    </ul>

    <%
        // Display last user from cookies
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie c : cookies){
                if(c.getName().equals("lastUser")){
    %>
                    <p>Last visitor: <%= c.getValue() %></p>
    <%
                }
            }
        }
    %>
</body>
</html>