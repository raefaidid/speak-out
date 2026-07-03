<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="active" value="analytics"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
  <title>Analytics — SpeakOut</title>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/nav.jspf" %>

<div class="container py-4">
  <div class="mb-4">
    <h4 class="fw-bold mb-1">System overview</h4>
    <div class="text-muted small">Cross-school analytics, computed live from the database.</div>
  </div>

  <c:set var="delta" value="${kpis.casesThisMonth - kpis.casesLastMonth}"/>
  <div class="row g-3 mb-4">
    <div class="col-6 col-lg-3">
      <div class="so-kpi">
        <div class="label">Total cases</div>
        <div class="value">${kpis.totalCases}</div>
        <div class="delta ${delta > 0 ? 'down' : 'up'}">
          <i class="bi ${delta > 0 ? 'bi-arrow-up' : 'bi-arrow-down'}"></i>
          ${delta >= 0 ? '+' : ''}${delta} vs last month
        </div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="so-kpi">
        <div class="label">Active users</div>
        <div class="value">${kpis.activeUsers}</div>
        <div class="delta text-muted">${kpis.activeStudents} students · ${kpis.activeStaff} staff</div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="so-kpi">
        <div class="label">Resolution rate</div>
        <div class="value">${kpis.resolutionRate}%</div>
        <div class="delta ${kpis.resolutionRate >= 60 ? 'up' : 'down'}">goal: 60%</div>
      </div>
    </div>
    <div class="col-6 col-lg-3">
      <div class="so-kpi">
        <div class="label">Avg response time</div>
        <div class="value">${kpis.avgResponseHours}<span class="fs-6 text-muted"> h</span></div>
        <div class="delta ${kpis.avgResponseHours <= 48 ? 'up' : 'down'}">SLA: 48 h</div>
      </div>
    </div>
  </div>

  <div class="row g-3">
    <div class="col-lg-7">
      <div class="so-card">
        <div class="so-section-title">Reports per month</div>
        <canvas id="monthChart" height="220" role="img" aria-label="Line chart of reports submitted per month"></canvas>
      </div>
    </div>
    <div class="col-lg-5">
      <div class="so-card">
        <div class="so-section-title">Cases per school</div>
        <canvas id="schoolChart" height="220" role="img" aria-label="Bar chart of cases per school"></canvas>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script>
  const brand = '#5B4FE0';
  const ink = '#6B7280';
  const grid = '#E5E7EB';
  Chart.defaults.font.family = "'Inter', -apple-system, sans-serif";
  Chart.defaults.color = ink;

  new Chart(document.getElementById('monthChart'), {
    type: 'line',
    data: {
      labels: [<c:forEach var="row" items="${reportsPerMonth}" varStatus="s">'${row[0]}'<c:if test="${!s.last}">,</c:if></c:forEach>],
      datasets: [{
        label: 'Reports',
        data: [<c:forEach var="row" items="${reportsPerMonth}" varStatus="s">${row[1]}<c:if test="${!s.last}">,</c:if></c:forEach>],
        borderColor: brand,
        backgroundColor: 'rgba(91, 79, 224, 0.08)',
        borderWidth: 2,
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBackgroundColor: brand,
        fill: true,
        tension: 0.3
      }]
    },
    options: {
      plugins: { legend: { display: false } },
      interaction: { mode: 'index', intersect: false },
      scales: {
        y: { beginAtZero: true, ticks: { precision: 0 }, grid: { color: grid } },
        x: { grid: { display: false } }
      }
    }
  });

  new Chart(document.getElementById('schoolChart'), {
    type: 'bar',
    data: {
      labels: [<c:forEach var="row" items="${casesPerSchool}" varStatus="s">'${fn:substring(row[0], 0, 34)}'<c:if test="${!s.last}">,</c:if></c:forEach>],
      datasets: [{
        label: 'Cases',
        data: [<c:forEach var="row" items="${casesPerSchool}" varStatus="s">${row[1]}<c:if test="${!s.last}">,</c:if></c:forEach>],
        backgroundColor: brand,
        borderRadius: 4,
        maxBarThickness: 22
      }]
    },
    options: {
      indexAxis: 'y',
      plugins: { legend: { display: false } },
      scales: {
        x: { beginAtZero: true, ticks: { precision: 0 }, grid: { color: grid } },
        y: { grid: { display: false }, ticks: { autoSkip: false, font: { size: 11 } } }
      }
    }
  });
</script>
</body>
</html>
