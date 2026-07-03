<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="active" value="cases"/>
<c:set var="r" value="${caseRecord.report}"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
  <title>Case ${caseRecord.caseId} — SpeakOut</title>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/nav.jspf" %>

<div class="container py-4">
  <div class="d-flex justify-content-between align-items-start mb-4 flex-wrap gap-2">
    <div>
      <div class="d-flex align-items-center gap-2 flex-wrap">
        <h4 class="fw-bold mb-0">${r.title}</h4>
        <span class="so-pill ${caseRecord.statusPill}">${caseRecord.status}</span>
        <span class="so-pill ${r.categoryPill}">${r.categoryName}</span>
      </div>
      <div class="text-muted small mt-1">
        Case <strong>${caseRecord.caseId}</strong> · Report <strong>#${r.reportId}</strong>
        · Submitted <fmt:formatDate value="${r.submittedAt}" pattern="d MMM yyyy, h:mm a"/>
        · ${r.schoolName}
      </div>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/case/delete"
          data-confirm="Delete case ${caseRecord.caseId}? Its investigation notes will be removed and report #${r.reportId} will return to the submitted queue.">
      <input type="hidden" name="id" value="${caseRecord.caseId}">
      <button type="submit" class="btn btn-outline-danger btn-sm"><i class="bi bi-trash me-1"></i>Delete case</button>
    </form>
  </div>

  <div class="row g-3">
    <div class="col-lg-8">
      <div class="so-card mb-3">
        <div class="so-section-title">Reported incident</div>
        <p class="mb-0" style="white-space: pre-line;">${r.description}</p>
        <c:if test="${not empty evidenceList}">
          <hr>
          <div class="so-form-label mb-2">Evidence</div>
          <ul class="list-unstyled mb-0">
            <c:forEach var="ev" items="${evidenceList}">
              <li class="mb-1">
                <i class="bi ${ev.fileType == 'image' ? 'bi-image' : ev.fileType == 'video' ? 'bi-camera-video' : 'bi-file-earmark-text'} text-primary me-2"></i>
                <a href="${pageContext.request.contextPath}/evidence?id=${ev.evidenceId}" target="_blank" class="text-decoration-none">${ev.fileName}</a>
                <span class="text-muted small">(${ev.sizeDisplay})</span>
              </li>
            </c:forEach>
          </ul>
        </c:if>
      </div>

      <div class="so-card">
        <div class="so-section-title">Investigation notes</div>
        <c:if test="${empty notes}">
          <p class="text-muted">No notes yet. Add the first investigation note below.</p>
        </c:if>
        <div class="so-timeline mb-4">
          <c:forEach var="n" items="${notes}">
            <div class="so-timeline-item">
              <div class="meta">
                <fmt:formatDate value="${n.createdAt}" pattern="d MMM yyyy, h:mm a"/>
                · ${n.authorName}
                · <span class="so-pill ${n.visibility == 'internal' ? 'draft' : 'resolved'}">${n.visibility == 'internal' ? 'Internal' : 'Visible to reporter'}</span>
              </div>
              <div class="small" style="white-space: pre-line;">${n.body}</div>
            </div>
          </c:forEach>
        </div>

        <form method="post" action="${pageContext.request.contextPath}/case/note">
          <input type="hidden" name="id" value="${caseRecord.caseId}">
          <label class="so-form-label" for="body">Add a note</label>
          <textarea class="form-control mt-1 mb-2" id="body" name="body" rows="3" required
                    placeholder="Record what was done, discussed, or decided…"></textarea>
          <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
            <select class="form-select form-select-sm w-auto" name="visibility">
              <option value="internal">Internal (staff only)</option>
              <option value="reporter-visible">Visible to reporter</option>
            </select>
            <button type="submit" class="btn btn-primary btn-sm"><i class="bi bi-plus-lg me-1"></i>Add note</button>
          </div>
        </form>
      </div>
    </div>

    <div class="col-lg-4">
      <div class="so-card mb-3">
        <div class="so-section-title">Case status</div>
        <form method="post" action="${pageContext.request.contextPath}/case/status"
              data-confirm="Change the status of case ${caseRecord.caseId}? The reporter will see the updated status.">
          <input type="hidden" name="id" value="${caseRecord.caseId}">
          <select class="form-select mb-2" name="status">
            <c:forEach var="st" items="${['New','Under Investigation','Resolved','Closed']}">
              <option value="${st}" ${caseRecord.status == st ? 'selected' : ''}>${st}</option>
            </c:forEach>
          </select>
          <button type="submit" class="btn btn-primary w-100"><i class="bi bi-arrow-repeat me-1"></i>Update status</button>
        </form>
      </div>

      <div class="so-card mb-3">
        <div class="so-section-title">Handlers</div>
        <div class="d-flex align-items-center gap-2 mb-2">
          <span class="so-brand-logo">${empty caseRecord.assignedName ? '?' : caseRecord.assignedName.substring(0, 1)}</span>
          <div>
            <div class="fw-semibold small">${empty caseRecord.assignedName ? 'Unassigned' : caseRecord.assignedName}</div>
            <div class="text-muted small">Lead handler</div>
          </div>
        </div>
        <c:forEach var="h" items="${handlers}">
          <div class="d-flex align-items-center gap-2 mb-2">
            <span class="so-brand-logo">${h.fullName.substring(0, 1)}</span>
            <div>
              <div class="fw-semibold small">${h.fullName}</div>
              <div class="text-muted small">Co-handler</div>
            </div>
          </div>
        </c:forEach>
        <hr class="my-2">
        <form method="post" action="${pageContext.request.contextPath}/case/handler" class="d-flex gap-2">
          <input type="hidden" name="id" value="${caseRecord.caseId}">
          <select class="form-select form-select-sm" name="userId" required>
            <option value="">Add co-handler…</option>
            <c:forEach var="t" items="${schoolTeachers}">
              <option value="${t.userId}">${t.fullName}</option>
            </c:forEach>
          </select>
          <button type="submit" class="btn btn-outline-primary btn-sm text-nowrap">Add</button>
        </form>
      </div>

      <div class="so-card">
        <div class="so-section-title">Case details</div>
        <table class="table table-sm table-borderless small mb-0">
          <tr><td class="text-muted">Reporter</td><td class="text-end">${r.reporterName}<c:if test="${not empty r.reporterClass}"><br><span class="text-muted">${r.reporterClass}</span></c:if></td></tr>
          <tr><td class="text-muted">School</td><td class="text-end">${r.schoolName}</td></tr>
          <tr><td class="text-muted">Priority</td><td class="text-end">${caseRecord.priority}</td></tr>
          <tr><td class="text-muted">Severity</td><td class="text-end">${r.severity}</td></tr>
          <tr><td class="text-muted">Location</td><td class="text-end">${empty r.location ? '—' : r.location}</td></tr>
          <tr><td class="text-muted">Incident date</td><td class="text-end"><c:choose><c:when test="${empty r.incidentDate}">—</c:when><c:otherwise><fmt:formatDate value="${r.incidentDate}" pattern="d MMM yyyy"/></c:otherwise></c:choose></td></tr>
          <tr><td class="text-muted">Case opened</td><td class="text-end"><fmt:formatDate value="${caseRecord.createdAt}" pattern="d MMM yyyy"/></td></tr>
          <tr><td class="text-muted">Anonymity</td><td class="text-end"><i class="bi bi-shield-check text-primary"></i> Hidden from peers</td></tr>
        </table>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>
</body>
</html>
