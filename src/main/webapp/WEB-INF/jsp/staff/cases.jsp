<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="active" value="cases"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
  <title>Cases — SpeakOut</title>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/nav.jspf" %>

<div class="container py-4">
  <div class="mb-4">
    <h4 class="fw-bold mb-1">${sessionScope.user.admin ? 'All cases' : 'Cases assigned to you'}</h4>
    <div class="text-muted small">${sessionScope.user.admin ? 'Every case across all schools.' : sessionScope.user.schoolName}</div>
  </div>

  <div class="row g-3 mb-4">
    <div class="col-6 col-lg-3"><div class="so-kpi"><div class="label">Open cases</div><div class="value">${stats.open}</div></div></div>
    <div class="col-6 col-lg-3"><div class="so-kpi"><div class="label">Needs first review</div><div class="value">${stats.needsReview}</div></div></div>
    <div class="col-6 col-lg-3"><div class="so-kpi"><div class="label">Resolved this month</div><div class="value">${stats.resolvedMonth}</div></div></div>
    <div class="col-6 col-lg-3"><div class="so-kpi"><div class="label">Avg response time</div><div class="value">${stats.avgHours}<span class="fs-6 text-muted"> h</span></div></div></div>
  </div>

  <div class="so-card mb-3">
    <form method="get" action="${pageContext.request.contextPath}/cases" class="row g-2 align-items-end">
      <div class="col-md-5">
        <label class="so-form-label" for="q">Search</label>
        <input type="text" class="form-control mt-1" id="q" name="q" value="${param.q}" placeholder="Search by title, case ID, or reporter…">
      </div>
      <div class="col-md-3">
        <label class="so-form-label" for="category">Category</label>
        <select class="form-select mt-1" id="category" name="category">
          <option value="">All categories</option>
          <c:forEach var="cat" items="${categories}">
            <option value="${cat.categoryId}" ${param.category == cat.categoryId ? 'selected' : ''}>${cat.name}</option>
          </c:forEach>
        </select>
      </div>
      <div class="col-md-2">
        <label class="so-form-label" for="status">Status</label>
        <select class="form-select mt-1" id="status" name="status">
          <option value="">All statuses</option>
          <c:forEach var="st" items="${['New','Under Investigation','Resolved','Closed']}">
            <option value="${st}" ${param.status == st ? 'selected' : ''}>${st}</option>
          </c:forEach>
        </select>
      </div>
      <div class="col-md-2 d-grid">
        <button type="submit" class="btn btn-outline-primary"><i class="bi bi-funnel me-1"></i>Filter</button>
      </div>
    </form>
  </div>

  <div class="so-card">
    <c:choose>
      <c:when test="${empty cases}">
        <p class="text-muted mb-0">No cases match the current filters.</p>
      </c:when>
      <c:otherwise>
        <div class="table-responsive">
          <table class="table so-table mb-0">
            <thead><tr><th>Case ID</th><th>Title</th><th>Reporter</th><th>Category</th><th>Status</th><th>Priority</th><th>Updated</th><th></th></tr></thead>
            <tbody>
              <c:forEach var="cs" items="${cases}">
                <tr>
                  <td class="fw-semibold">${cs.caseId}</td>
                  <td>${cs.report.title}</td>
                  <td>
                    <div>${cs.report.reporterName}</div>
                    <div class="text-muted small">${empty cs.report.reporterClass ? cs.report.schoolName : cs.report.reporterClass}</div>
                  </td>
                  <td><span class="so-pill ${cs.report.categoryPill}">${cs.report.categoryName}</span></td>
                  <td><span class="so-pill ${cs.statusPill}">${cs.status}</span></td>
                  <td class="small">${cs.priority}</td>
                  <td class="text-muted small"><fmt:formatDate value="${cs.updatedAt}" pattern="d MMM yyyy"/></td>
                  <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/case?id=${cs.caseId}">Open</a></td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>
</body>
</html>
