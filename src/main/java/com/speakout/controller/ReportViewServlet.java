package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.speakout.dao.CaseDAO;
import com.speakout.dao.CaseNoteDAO;
import com.speakout.dao.EvidenceDAO;
import com.speakout.dao.ReportDAO;
import com.speakout.bean.CaseNoteBean;
import com.speakout.bean.CaseBean;
import com.speakout.bean.ReportBean;
import com.speakout.bean.UserBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Screen 6: Report Status / Case Detail — student read-only view with timeline. */
public class ReportViewServlet extends HttpServlet {

    private final ReportDAO reportDAO = new ReportDAO();
    private final CaseDAO caseDAO = new CaseDAO();
    private final CaseNoteDAO noteDAO = new CaseNoteDAO();
    private final EvidenceDAO evidenceDAO = new EvidenceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        try {
            ReportBean r = reportDAO.findById(req.getParameter("id"));
            boolean owner = r != null && r.getReporterId().equals(user.getUserId());
            if (r == null || (!owner && !user.isStaff())) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            CaseBean cs = caseDAO.findByReportId(r.getReportId());
            req.setAttribute("report", r);
            req.setAttribute("caseRecord", cs);
            req.setAttribute("evidenceList", evidenceDAO.listByReport(r.getReportId()));
            req.setAttribute("timeline", buildTimeline(r, cs));
            req.getRequestDispatcher("/WEB-INF/jsp/student/report-detail.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /** Chronological events: submitted -> assigned -> reporter-visible notes -> resolution. */
    private List<Map<String, Object>> buildTimeline(ReportBean r, CaseBean cs) throws SQLException {
        List<Map<String, Object>> items = new ArrayList<>();
        if (!"Draft".equals(r.getStatus())) {
            items.add(event(r.getSubmittedAt(), "Report submitted",
                    "Your report was received and logged as #" + r.getReportId() + "."));
        }
        if (cs != null) {
            items.add(event(cs.getCreatedAt(), "Case assigned",
                    cs.getAssignedName() == null
                            ? "A case was opened for your report."
                            : "Assigned to " + cs.getAssignedName() + " for review."));
            for (CaseNoteBean n : noteDAO.listByCase(cs.getCaseId(), false)) {
                items.add(event(n.getCreatedAt(), "Update from " + n.getAuthorName(), n.getBody()));
            }
            if ("Resolved".equals(cs.getStatus()) || "Closed".equals(cs.getStatus())) {
                items.add(event(cs.getUpdatedAt(), "Case " + cs.getStatus().toLowerCase(),
                        "This case has been marked as " + cs.getStatus().toLowerCase() + "."));
            }
        }
        // notes can arrive after a resolution event, so order strictly by time
        items.sort(Comparator.comparing(m -> (Timestamp) m.get("when")));
        return items;
    }

    private Map<String, Object> event(Timestamp when, String title, String body) {
        Map<String, Object> m = new HashMap<>();
        m.put("when", when);
        m.put("title", title);
        m.put("body", body);
        return m;
    }
}
