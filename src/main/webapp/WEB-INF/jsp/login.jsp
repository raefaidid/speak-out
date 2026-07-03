<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
  <title>Log in — SpeakOut</title>
</head>
<body>
<div class="so-auth-wrap">
  <div class="so-auth-hero">
    <a class="so-brand text-white fs-4 text-decoration-none" href="#"><span class="so-brand-logo">S</span> SpeakOut</a>
    <div>
      <h1>A safer space to speak up.</h1>
      <p class="lead opacity-75 mb-4">Report bullying anonymously, track your case, and get support from your school's counsellors.</p>
      <div class="quote">
        "I didn't have to be afraid anymore. I reported what happened and someone actually followed up."
        <div class="opacity-75 small mt-2">— Form 4 student</div>
      </div>
    </div>
    <div class="opacity-75 small"><i class="bi bi-shield-check me-1"></i>Identity hidden from peers — visible only to your school's counsellor.</div>
  </div>

  <div class="so-auth-form-wrap">
    <div class="so-auth-form">
      <h3 class="fw-bold mb-1">Welcome back</h3>
      <p class="text-muted mb-4">Log in to continue to SpeakOut.</p>

      <c:if test="${param.out == '1'}">
        <div class="alert alert-success py-2"><i class="bi bi-check-circle me-2"></i>You have been logged out.</div>
      </c:if>
      <c:if test="${not empty sessionScope.flash}">
        <div class="alert alert-${empty sessionScope.flashType ? 'success' : sessionScope.flashType} py-2">
          <i class="bi bi-check-circle me-2"></i>${sessionScope.flash}
        </div>
        <c:remove var="flash" scope="session"/>
        <c:remove var="flashType" scope="session"/>
      </c:if>
      <c:if test="${not empty error}">
        <div class="alert alert-danger py-2"><i class="bi bi-exclamation-triangle me-2"></i>${error}</div>
      </c:if>

      <form method="post" action="${pageContext.request.contextPath}/login" id="loginForm" novalidate>
        <div class="mb-3">
          <label class="so-form-label" for="email">Email</label>
          <div class="input-group mt-1">
            <span class="input-group-text bg-white"><i class="bi bi-envelope text-muted"></i></span>
            <input type="email" class="form-control" id="email" name="email" value="${email}" placeholder="you@school.edu.my" required>
          </div>
        </div>
        <div class="mb-3">
          <div class="d-flex justify-content-between align-items-center">
            <label class="so-form-label mb-0" for="password">Password</label>
            <a href="#" class="small text-decoration-none" onclick="alert('Please contact your school admin to reset your password.'); return false;">Forgot password?</a>
          </div>
          <div class="input-group mt-1">
            <span class="input-group-text bg-white"><i class="bi bi-lock text-muted"></i></span>
            <input type="password" class="form-control" id="password" name="password" required>
          </div>
        </div>
        <div class="form-check mb-3">
          <input class="form-check-input" type="checkbox" id="keepLoggedIn" name="keepLoggedIn">
          <label class="form-check-label small" for="keepLoggedIn">Keep me logged in</label>
        </div>
        <button type="submit" class="btn btn-primary w-100 py-2"><i class="bi bi-box-arrow-in-right me-2"></i>Log in</button>
      </form>

      <div class="text-center my-3 text-muted small">Quick preview (demo accounts)</div>
      <div class="row g-2">
        <div class="col"><button class="btn btn-outline-primary w-100 btn-sm" onclick="demo('student.aiman@speakout.demo','Student@123')">Student</button></div>
        <div class="col"><button class="btn btn-outline-primary w-100 btn-sm" onclick="demo('teacher.aina@speakout.demo','Teacher@123')">Teacher</button></div>
        <div class="col"><button class="btn btn-outline-primary w-100 btn-sm" onclick="demo('admin@speakout.demo','Admin@123')">Admin</button></div>
      </div>

      <p class="text-center mt-4 mb-0 text-muted small">
        New to SpeakOut? <a href="${pageContext.request.contextPath}/register" class="text-decoration-none fw-semibold">Create an account</a>
      </p>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  function demo(email, password) {
    var f = document.getElementById('loginForm');
    f.email.value = email;
    f.password.value = password;
    f.submit();
  }
</script>
</body>
</html>
