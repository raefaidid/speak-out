<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="editing" value="${mode == 'edit'}"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
  <title>${editing ? 'Edit Report' : 'Submit Report'} — SpeakOut</title>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/nav.jspf" %>

<div class="container py-4" style="max-width: 760px;">
  <h4 class="fw-bold mb-1">${editing ? 'Edit report' : 'Report an incident'}</h4>
  <p class="text-muted mb-3">${editing ? 'Update the details below.' : 'Take your time. You can save a draft and finish later.'}</p>

  <div class="so-anon-banner mb-4"><i class="bi bi-shield-check me-1"></i>Your identity is hidden from peers. Only your school's counsellor and admin can see who reported.</div>

  <c:if test="${not empty errors}">
    <div class="alert alert-danger py-2"><i class="bi bi-exclamation-triangle me-2"></i>Please fix the highlighted fields below.</div>
  </c:if>

  <form method="post" enctype="multipart/form-data"
        action="${pageContext.request.contextPath}${editing ? '/report/edit?id=' : '/report/new'}${editing ? report.reportId : ''}"
        id="reportForm" ${editing && report.status == 'Submitted' ? 'data-confirm="Save changes to this submitted report? Your counsellor will see the updated version."' : ''}>
    <div class="so-card mb-3">
      <div class="mb-3">
        <label class="so-form-label" for="title">Title <span class="text-muted small">(max 100 characters)</span></label>
        <input type="text" class="form-control mt-1 ${not empty errors.title ? 'is-invalid' : ''}" id="title" name="title"
               value="${report.title}" maxlength="100" required>
        <div class="invalid-feedback">${errors.title}</div>
      </div>

      <div class="row g-3 mb-3">
        <div class="col-md-6">
          <label class="so-form-label" for="categoryId">Category</label>
          <select class="form-select mt-1 ${not empty errors.categoryId ? 'is-invalid' : ''}" id="categoryId" name="categoryId" required>
            <option value="">Choose a category…</option>
            <c:forEach var="cat" items="${categories}">
              <option value="${cat.categoryId}" ${report.categoryId == cat.categoryId ? 'selected' : ''}>${cat.name} — ${cat.description}</option>
            </c:forEach>
          </select>
          <div class="invalid-feedback">${errors.categoryId}</div>
        </div>
        <div class="col-md-6">
          <label class="so-form-label" for="incidentDate">When did it happen? <span class="text-muted small">(optional)</span></label>
          <input type="date" class="form-control mt-1 ${not empty errors.incidentDate ? 'is-invalid' : ''}" id="incidentDate"
                 name="incidentDate" value="${report.incidentDate}">
          <div class="invalid-feedback">${errors.incidentDate}</div>
        </div>
      </div>

      <div class="row g-3 mb-3">
        <div class="col-md-6">
          <label class="so-form-label" for="location">Where did it happen? <span class="text-muted small">(optional)</span></label>
          <select class="form-select mt-1" id="location" name="location">
            <option value="">Choose a location…</option>
            <c:forEach var="loc" items="${['Classroom','Canteen','Corridor / stairwell','Toilet','Sports field','School bus','Online','Other']}">
              <option value="${loc}" ${report.location == loc ? 'selected' : ''}>${loc}</option>
            </c:forEach>
          </select>
        </div>
        <div class="col-md-6">
          <label class="so-form-label" for="severity">How serious was it?</label>
          <select class="form-select mt-1 ${not empty errors.severity ? 'is-invalid' : ''}" id="severity" name="severity" required>
            <option value="">Choose…</option>
            <c:forEach var="sev" items="${['Low','Medium','High','Critical']}">
              <option value="${sev}" ${report.severity == sev ? 'selected' : ''}>${sev}</option>
            </c:forEach>
          </select>
          <div class="invalid-feedback">${errors.severity}</div>
        </div>
      </div>

      <div class="mb-1">
        <label class="so-form-label" for="description">What happened?</label>
        <textarea class="form-control mt-1 ${not empty errors.description ? 'is-invalid' : ''}" id="description" name="description"
                  rows="6" placeholder="Describe what happened, who was involved, and whether it has happened before." required>${report.description}</textarea>
        <div class="invalid-feedback">${errors.description}</div>
      </div>
    </div>

    <div class="so-card mb-3">
      <c:if test="${editing and not empty evidenceList}">
        <div class="so-form-label mb-2">Already uploaded</div>
        <ul class="list-unstyled mb-3">
          <c:forEach var="ev" items="${evidenceList}">
            <li class="mb-1">
              <i class="bi ${ev.fileType == 'image' ? 'bi-image' : ev.fileType == 'video' ? 'bi-camera-video' : 'bi-file-earmark-text'} text-primary me-2"></i>
              <a href="${pageContext.request.contextPath}/evidence?id=${ev.evidenceId}" target="_blank" class="text-decoration-none">${ev.fileName}</a>
              <span class="text-muted small">(${ev.sizeDisplay})</span>
            </li>
          </c:forEach>
        </ul>
      </c:if>
      <label class="so-form-label" for="evidence">${editing ? 'Add more evidence' : 'Evidence'} <span class="text-muted small">(optional — images, videos or documents, up to 50MB each)</span></label>
      <input type="file" class="form-control mt-2" id="evidence" name="evidence" multiple
             accept="image/*,video/*,.pdf,.doc,.docx">
      <div class="so-form-help">Screenshots, photos, or recordings help your counsellor understand what happened.</div>
    </div>

    <div class="d-flex gap-2 justify-content-end">
      <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}${editing ? '/reports' : '/dashboard'}">Cancel</a>
      <c:choose>
        <c:when test="${not editing}">
          <button type="submit" name="action" value="draft" class="btn btn-outline-primary" formnovalidate><i class="bi bi-save me-1"></i>Save as draft</button>
          <button type="submit" name="action" value="submit" class="btn btn-primary"><i class="bi bi-send me-1"></i>Submit report</button>
        </c:when>
        <c:otherwise>
          <button type="submit" name="action" value="save" class="btn btn-primary"><i class="bi bi-save me-1"></i>Save changes</button>
          <c:if test="${report.status == 'Draft'}">
            <button type="submit" name="action" value="submit" class="btn btn-primary"><i class="bi bi-send me-1"></i>Submit report</button>
          </c:if>
        </c:otherwise>
      </c:choose>
    </div>
  </form>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>
<script>
  // Client-side checks on blur/submit; drafts skip them (formnovalidate handles the browser side).
  (function () {
    var form = document.getElementById('reportForm');
    var fields = ['title', 'categoryId', 'severity', 'description'];

    function check(id) {
      var el = document.getElementById(id);
      var ok = el.value.trim() !== '';
      el.classList.toggle('is-invalid', !ok);
      return ok;
    }
    fields.forEach(function (id) {
      document.getElementById(id).addEventListener('blur', function () { check(id); });
    });
    form.addEventListener('submit', function (e) {
      if (e.submitter && e.submitter.value === 'draft') return;   // drafts may be incomplete
      var ok = true;
      fields.forEach(function (id) { if (!check(id)) ok = false; });
      if (!ok) e.preventDefault();
    });
  })();
</script>
</body>
</html>
