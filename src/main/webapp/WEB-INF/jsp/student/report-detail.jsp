<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="active" value="reports"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
  <title>Report #${report.reportId} — SpeakOut</title>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/nav.jspf" %>

<div class="container py-4">
  <div class="d-flex justify-content-between align-items-start mb-4 flex-wrap gap-2">
    <div>
      <div class="d-flex align-items-center gap-2 flex-wrap">
        <h4 class="fw-bold mb-0">${report.title}</h4>
        <span class="so-pill ${report.statusPill}">${report.status}</span>
        <span class="so-pill ${report.categoryPill}">${report.categoryName}</span>
      </div>
      <div class="text-muted small mt-1">
        Case ID <strong>#${report.reportId}</strong>
        · Submitted <fmt:formatDate value="${report.submittedAt}" pattern="d MMM yyyy, h:mm a"/>
        <c:if test="${not empty report.location}"> · ${report.location}</c:if>
      </div>
    </div>
    <div class="d-flex gap-2">
      <c:if test="${sessionScope.user.student and report.editable}">
        <a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/report/edit?id=${report.reportId}"><i class="bi bi-pencil me-1"></i>Edit</a>
      </c:if>
      <c:if test="${sessionScope.user.staff and not empty caseRecord}">
        <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/case?id=${caseRecord.caseId}"><i class="bi bi-briefcase me-1"></i>Open case</a>
      </c:if>
    </div>
  </div>

  <div class="row g-3">
    <div class="col-lg-8">
      <div class="so-card mb-3">
        <div class="so-section-title">What I reported</div>
        <p class="mb-0" style="white-space: pre-line;">${report.description}</p>
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
        <div class="so-section-title">Case timeline</div>
        <c:choose>
          <c:when test="${empty timeline}">
            <p class="text-muted mb-0">This report has not been submitted yet. Submit it to start the case timeline.</p>
          </c:when>
          <c:otherwise>
            <div class="so-timeline">
              <c:forEach var="ev" items="${timeline}">
                <div class="so-timeline-item">
                  <div class="meta"><fmt:formatDate value="${ev.when}" pattern="d MMM yyyy, h:mm a"/></div>
                  <div class="fw-semibold">${ev.title}</div>
                  <div class="small text-muted">${ev.body}</div>
                </div>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <div class="col-lg-4">
      <c:if test="${not empty caseRecord and not empty caseRecord.assignedName}">
        <div class="so-card mb-3">
          <div class="so-section-title">Assigned counsellor</div>
          <div class="d-flex align-items-center gap-3">
            <span class="so-brand-logo">${caseRecord.assignedName.substring(0, 1)}</span>
            <div>
              <div class="fw-semibold">${caseRecord.assignedName}</div>
              <div class="text-muted small">Handling case ${caseRecord.caseId}</div>
            </div>
          </div>
        </div>
      </c:if>

      <div class="so-card">
        <div class="so-section-title">Case details</div>
        <table class="table table-sm table-borderless small mb-0">
          <tr><td class="text-muted">Status</td><td class="text-end">${report.status}</td></tr>
          <tr><td class="text-muted">Category</td><td class="text-end">${report.categoryName}</td></tr>
          <tr><td class="text-muted">Severity</td><td class="text-end">${report.severity}</td></tr>
          <tr><td class="text-muted">Location</td><td class="text-end">${empty report.location ? '—' : report.location}</td></tr>
          <tr><td class="text-muted">Incident date</td><td class="text-end"><c:choose><c:when test="${empty report.incidentDate}">—</c:when><c:otherwise><fmt:formatDate value="${report.incidentDate}" pattern="d MMM yyyy"/></c:otherwise></c:choose></td></tr>
          <tr><td class="text-muted">Submitted</td><td class="text-end"><fmt:formatDate value="${report.submittedAt}" pattern="d MMM yyyy"/></td></tr>
          <tr><td class="text-muted">Anonymity</td><td class="text-end"><i class="bi bi-shield-check text-primary"></i> Hidden from peers</td></tr>
        </table>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>
</body>
</html>
