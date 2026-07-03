<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="active" value="profile"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
  <title>My Profile — SpeakOut</title>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/nav.jspf" %>

<div class="container py-4" style="max-width: 720px;">
  <h4 class="fw-bold mb-4">My Profile</h4>

  <div class="so-card mb-3">
    <div class="d-flex align-items-center gap-3">
      <span class="so-brand-logo" style="width: 56px; height: 56px; font-size: 22px;">${sessionScope.user.initials}</span>
      <div>
        <div class="fw-bold fs-5">${sessionScope.user.fullName}</div>
        <div class="text-muted small">${sessionScope.user.role} · ${sessionScope.user.schoolName}</div>
        <div class="text-muted small">Member since <fmt:formatDate value="${sessionScope.user.createdAt}" pattern="MMMM yyyy"/></div>
      </div>
    </div>
  </div>

  <div class="so-card">
    <div class="so-section-title">Account information</div>
    <form method="post" action="${pageContext.request.contextPath}/profile"
          data-confirm="Save these changes to your profile?">
      <div class="mb-3">
        <label class="so-form-label" for="fullName">Full name</label>
        <input type="text" class="form-control mt-1 ${not empty errors.fullName ? 'is-invalid' : ''}"
               id="fullName" name="fullName" value="${sessionScope.user.fullName}" maxlength="100" required>
        <div class="invalid-feedback">${errors.fullName}</div>
      </div>
      <c:if test="${sessionScope.user.student}">
        <div class="mb-3">
          <label class="so-form-label" for="classForm">Class / Form</label>
          <input type="text" class="form-control mt-1" id="classForm" name="classForm"
                 value="${sessionScope.user.classForm}" placeholder="e.g. Form 4 Bestari">
        </div>
      </c:if>
      <div class="mb-3">
        <label class="so-form-label" for="email">Email</label>
        <input type="email" class="form-control mt-1" id="email" value="${sessionScope.user.email}" disabled>
        <div class="so-form-help">Contact your school admin to change your email.</div>
      </div>
      <div class="mb-4">
        <label class="so-form-label" for="school">School</label>
        <input type="text" class="form-control mt-1" id="school" value="${sessionScope.user.schoolName} (${sessionScope.user.schoolCode})" disabled>
        <div class="so-form-help">Contact your school admin if you have moved school.</div>
      </div>
      <button type="submit" class="btn btn-primary"><i class="bi bi-save me-1"></i>Save changes</button>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>
</body>
</html>
