<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="active" value="dashboard"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
  <title>Dashboard — SpeakOut</title>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/nav.jspf" %>

<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
    <div>
      <h4 class="fw-bold mb-1">Welcome back, ${sessionScope.user.fullName}</h4>
      <div class="text-muted small">${sessionScope.user.schoolName}</div>
    </div>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/report/new"><i class="bi bi-plus-lg me-1"></i>New Report</a>
  </div>

  <div class="row g-3 mb-4">
    <div class="col-6 col-lg-3"><div class="so-kpi"><div class="label">Total reports</div><div class="value">${stats.total}</div></div></div>
    <div class="col-6 col-lg-3"><div class="so-kpi"><div class="label">Awaiting counsellor</div><div class="value">${stats.awaiting}</div></div></div>
    <div class="col-6 col-lg-3"><div class="so-kpi"><div class="label">Submitted last 7 days</div><div class="value">${stats.lastWeek}</div></div></div>
    <div class="col-6 col-lg-3"><div class="so-kpi"><div class="label">Drafts (not submitted)</div><div class="value">${stats.drafts}</div></div></div>
  </div>

  <div class="row g-3">
    <div class="col-lg-8">
      <div class="so-card">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <div class="so-section-title mb-0">Recent reports</div>
          <a href="${pageContext.request.contextPath}/reports" class="small text-decoration-none">View all</a>
        </div>
        <c:choose>
          <c:when test="${empty recentReports}">
            <p class="text-muted mb-0">No reports yet. When you submit a report, it will show up here.</p>
          </c:when>
          <c:otherwise>
            <div class="table-responsive">
              <table class="table so-table mb-0">
                <thead><tr><th>Case ID</th><th>Title</th><th>Category</th><th>Status</th><th>Updated</th><th></th></tr></thead>
                <tbody>
                  <c:forEach var="r" items="${recentReports}">
                    <tr>
                      <td class="fw-semibold">#${r.reportId}</td>
                      <td>${r.title}</td>
                      <td><span class="so-pill ${r.categoryPill}">${r.categoryName}</span></td>
                      <td><span class="so-pill ${r.statusPill}">${r.status}</span></td>
                      <td class="text-muted small"><fmt:formatDate value="${r.updatedAt}" pattern="d MMM yyyy"/></td>
                      <td class="text-end">
                        <c:choose>
                          <c:when test="${r.status == 'Draft'}">
                            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/report/edit?id=${r.reportId}">Continue</a>
                          </c:when>
                          <c:otherwise>
                            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/report?id=${r.reportId}">Open</a>
                          </c:otherwise>
                        </c:choose>
                      </td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <div class="col-lg-4">
      <div class="so-card mb-3">
        <div class="so-section-title">Quick actions</div>
        <div class="d-grid gap-2">
          <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/report/new"><i class="bi bi-megaphone me-2"></i>Report an incident</a>
          <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/reports"><i class="bi bi-folder2-open me-2"></i>Track my reports</a>
          <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/profile"><i class="bi bi-person me-2"></i>Update my profile</a>
        </div>
      </div>

      <div class="so-card bg-primary-soft border-primary">
        <div class="so-section-title"><i class="bi bi-heart-pulse text-primary me-1"></i>Need help now?</div>
        <p class="small mb-2">If you are in immediate danger, don't wait for the app:</p>
        <ul class="list-unstyled small mb-2">
          <li class="mb-1"><strong>999</strong> — Emergency</li>
          <li class="mb-1"><strong>15999</strong> — Talian Kasih (24h)</li>
          <li><strong>03-7627 2929</strong> — Befrienders KL</li>
        </ul>
        <c:if test="${not empty counsellor}">
          <hr class="my-2">
          <div class="small">Your school counsellor:<br><strong>${counsellor.fullName}</strong></div>
        </c:if>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>
</body>
</html>
