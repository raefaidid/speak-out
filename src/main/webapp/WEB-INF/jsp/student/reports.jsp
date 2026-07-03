<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="active" value="reports"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
  <title>My Reports — SpeakOut</title>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/nav.jspf" %>

<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
    <div>
      <h4 class="fw-bold mb-1">My Reports</h4>
      <div class="text-muted small">Track, update, and manage everything you have reported.</div>
    </div>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/report/new"><i class="bi bi-plus-lg me-1"></i>New Report</a>
  </div>

  <div class="so-card mb-3">
    <form method="get" action="${pageContext.request.contextPath}/reports" class="row g-2 align-items-end">
      <div class="col-md-5">
        <label class="so-form-label" for="q">Search</label>
        <input type="text" class="form-control mt-1" id="q" name="q" value="${param.q}" placeholder="Search by title or case ID…">
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
          <c:forEach var="st" items="${['Draft','Submitted','In review','Resolved']}">
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
      <c:when test="${empty reports}">
        <p class="text-muted mb-0">No reports match. Try clearing the filters, or <a href="${pageContext.request.contextPath}/report/new">create a new report</a>.</p>
      </c:when>
      <c:otherwise>
        <div class="table-responsive">
          <table class="table so-table mb-0">
            <thead><tr><th>Case ID</th><th>Title</th><th>Category</th><th>Status</th><th>Created</th><th>Updated</th><th class="text-end">Actions</th></tr></thead>
            <tbody>
              <c:forEach var="r" items="${reports}">
                <tr>
                  <td class="fw-semibold">#${r.reportId}</td>
                  <td>${r.title}</td>
                  <td><span class="so-pill ${r.categoryPill}">${r.categoryName}</span></td>
                  <td><span class="so-pill ${r.statusPill}">${r.status}</span></td>
                  <td class="text-muted small"><fmt:formatDate value="${r.submittedAt}" pattern="d MMM yyyy"/></td>
                  <td class="text-muted small"><fmt:formatDate value="${r.updatedAt}" pattern="d MMM yyyy"/></td>
                  <td class="text-end text-nowrap">
                    <a class="btn btn-sm btn-outline-secondary" title="View" href="${pageContext.request.contextPath}/report?id=${r.reportId}"><i class="bi bi-eye"></i></a>
                    <c:if test="${r.editable}">
                      <a class="btn btn-sm btn-outline-primary" title="Edit" href="${pageContext.request.contextPath}/report/edit?id=${r.reportId}"><i class="bi bi-pencil"></i></a>
                    </c:if>
                    <c:if test="${r.deletable}">
                      <form class="d-inline" method="post" action="${pageContext.request.contextPath}/report/delete"
                            data-confirm="Delete draft #${r.reportId} (&quot;${r.title}&quot;)? This cannot be undone.">
                        <input type="hidden" name="id" value="${r.reportId}">
                        <button type="submit" class="btn btn-sm btn-outline-danger" title="Delete draft"><i class="bi bi-trash"></i></button>
                      </form>
                    </c:if>
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

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>
</body>
</html>
