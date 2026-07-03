<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
  <title>Register — SpeakOut</title>
</head>
<body>
<div class="so-auth-wrap">
  <div class="so-auth-hero">
    <a class="so-brand text-white fs-4 text-decoration-none" href="#"><span class="so-brand-logo">S</span> SpeakOut</a>
    <div>
      <h1>Join a community that listens.</h1>
      <p class="lead opacity-75 mb-4">Create your account with your school code. Your reports stay anonymous to other students.</p>
      <div class="quote">
        "Signing up took a minute. Knowing someone would follow up made all the difference."
        <div class="opacity-75 small mt-2">— Form 2 student</div>
      </div>
    </div>
    <div class="opacity-75 small"><i class="bi bi-shield-check me-1"></i>Identity hidden from peers — visible only to your school's counsellor.</div>
  </div>

  <div class="so-auth-form-wrap">
    <div class="so-auth-form">
      <h3 class="fw-bold mb-1">Create your account</h3>
      <p class="text-muted mb-4">Choose your role to get started.</p>

      <form method="post" action="${pageContext.request.contextPath}/register" id="registerForm" novalidate>
        <c:set var="role" value="${empty param.role ? 'Student' : param.role}"/>
        <div class="btn-group w-100 mb-3" role="group">
          <input type="radio" class="btn-check" name="role" id="roleStudent" value="Student" ${role == 'Student' ? 'checked' : ''}>
          <label class="btn btn-outline-primary" for="roleStudent"><i class="bi bi-backpack me-1"></i>Student</label>
          <input type="radio" class="btn-check" name="role" id="roleTeacher" value="Teacher" ${role == 'Teacher' ? 'checked' : ''}>
          <label class="btn btn-outline-primary" for="roleTeacher"><i class="bi bi-mortarboard me-1"></i>Teacher</label>
        </div>
        <c:if test="${not empty errors.role}"><div class="text-danger small mb-2">${errors.role}</div></c:if>

        <div class="mb-3">
          <label class="so-form-label" for="fullName">Full name</label>
          <input type="text" class="form-control mt-1 ${not empty errors.fullName ? 'is-invalid' : ''}"
                 id="fullName" name="fullName" value="${param.fullName}" maxlength="100" required>
          <div class="invalid-feedback">${empty errors.fullName ? 'Please enter your full name.' : errors.fullName}</div>
        </div>

        <div class="mb-3">
          <label class="so-form-label" for="schoolCode">School code</label>
          <input type="text" class="form-control mt-1 ${not empty errors.schoolCode ? 'is-invalid' : ''}"
                 id="schoolCode" name="schoolCode" value="${param.schoolCode}" placeholder="e.g. BEA0091" required>
          <div class="invalid-feedback">${empty errors.schoolCode ? 'Please enter your school code.' : errors.schoolCode}</div>
          <div class="so-form-help">Ask your school office if you don't know your code.</div>
        </div>

        <div class="mb-3" id="classFormGroup">
          <label class="so-form-label" for="classForm">Class / Form <span class="text-muted">(optional)</span></label>
          <input type="text" class="form-control mt-1" id="classForm" name="classForm" value="${param.classForm}" placeholder="e.g. Form 4 Bestari">
        </div>

        <div class="mb-3">
          <label class="so-form-label" for="email">Email</label>
          <input type="email" class="form-control mt-1 ${not empty errors.email ? 'is-invalid' : ''}"
                 id="email" name="email" value="${param.email}" required>
          <div class="invalid-feedback">${empty errors.email ? 'Please enter a valid email address.' : errors.email}</div>
        </div>

        <div class="row g-2 mb-3">
          <div class="col-md-6">
            <label class="so-form-label" for="password">Password</label>
            <input type="password" class="form-control mt-1 ${not empty errors.password ? 'is-invalid' : ''}"
                   id="password" name="password" minlength="8" required>
            <div class="invalid-feedback">${empty errors.password ? 'At least 8 characters with letters and numbers.' : errors.password}</div>
            <div class="so-form-help">Min. 8 characters, letters and numbers.</div>
          </div>
          <div class="col-md-6">
            <label class="so-form-label" for="confirmPassword">Confirm password</label>
            <input type="password" class="form-control mt-1 ${not empty errors.confirmPassword ? 'is-invalid' : ''}"
                   id="confirmPassword" name="confirmPassword" required>
            <div class="invalid-feedback">${empty errors.confirmPassword ? 'Passwords must match.' : errors.confirmPassword}</div>
          </div>
        </div>

        <div class="form-check mb-3">
          <input class="form-check-input ${not empty errors.pdpa ? 'is-invalid' : ''}" type="checkbox" id="pdpa" name="pdpa" required>
          <label class="form-check-label small" for="pdpa">
            I consent to SpeakOut processing my personal data for bullying case management, in line with the
            Personal Data Protection Act 2010 (PDPA).
          </label>
          <div class="invalid-feedback">${empty errors.pdpa ? 'You must agree before registering.' : errors.pdpa}</div>
        </div>

        <button type="submit" class="btn btn-primary w-100 py-2"><i class="bi bi-person-plus me-2"></i>Create account</button>
      </form>

      <p class="text-center mt-4 mb-0 text-muted small">
        Already have an account? <a href="${pageContext.request.contextPath}/login" class="text-decoration-none fw-semibold">Log in</a>
      </p>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  // Client-side validation on blur + submit; the servlet re-validates everything.
  (function () {
    var form = document.getElementById('registerForm');
    var emailRe = /^[\w.+-]+@[\w-]+(\.[\w-]+)+$/;

    function check(input) {
      var ok = true;
      if (input.id === 'fullName') ok = input.value.trim().length >= 3;
      if (input.id === 'schoolCode') ok = input.value.trim().length > 0;
      if (input.id === 'email') ok = emailRe.test(input.value.trim());
      if (input.id === 'password') ok = input.value.length >= 8 && /[A-Za-z]/.test(input.value) && /\d/.test(input.value);
      if (input.id === 'confirmPassword') ok = input.value === document.getElementById('password').value && input.value !== '';
      if (input.id === 'pdpa') ok = input.checked;
      input.classList.toggle('is-invalid', !ok);
      return ok;
    }

    form.querySelectorAll('input:not([type=radio])').forEach(function (input) {
      input.addEventListener('blur', function () { check(input); });
    });
    form.addEventListener('submit', function (e) {
      var allOk = true;
      form.querySelectorAll('input:not([type=radio])').forEach(function (input) {
        if (!check(input)) allOk = false;
      });
      if (!allOk) e.preventDefault();
    });

    // Class/Form only applies to students
    function toggleClass() {
      document.getElementById('classFormGroup').style.display =
        document.getElementById('roleStudent').checked ? '' : 'none';
    }
    document.getElementById('roleStudent').addEventListener('change', toggleClass);
    document.getElementById('roleTeacher').addEventListener('change', toggleClass);
    toggleClass();
  })();
</script>
</body>
</html>
