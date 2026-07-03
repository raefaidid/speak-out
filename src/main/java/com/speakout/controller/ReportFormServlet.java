package com.speakout.controller;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.speakout.dao.CaseDAO;
import com.speakout.dao.CategoryDAO;
import com.speakout.dao.EvidenceDAO;
import com.speakout.dao.ReportDAO;
import com.speakout.dao.UserDAO;
import com.speakout.bean.ReportBean;
import com.speakout.bean.UserBean;
import com.speakout.util.Flash;
import com.speakout.util.Uploads;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/** Screen 4: Submit Report (Create). GET shows the form, POST creates draft/submitted report. */
public class ReportFormServlet extends HttpServlet {

    private final ReportDAO reportDAO = new ReportDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    private final CaseDAO caseDAO = new CaseDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        if (!user.isStudent()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            req.setAttribute("categories", categoryDAO.listAll());
            req.setAttribute("mode", "new");
            req.getRequestDispatcher("/WEB-INF/jsp/student/report-form.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        if (!user.isStudent()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        boolean asDraft = "draft".equals(req.getParameter("action"));
        Map<String, String> errors = new LinkedHashMap<>();
        ReportBean r = readForm(req, user, errors);

        try {
            if (!errors.isEmpty()) {
                req.setAttribute("errors", errors);
                req.setAttribute("report", r);
                req.setAttribute("categories", categoryDAO.listAll());
                req.setAttribute("mode", "new");
                req.getRequestDispatcher("/WEB-INF/jsp/student/report-form.jsp").forward(req, resp);
                return;
            }
            r.setStatus(asDraft ? "Draft" : "Submitted");
            String reportId = reportDAO.insert(r);
            saveEvidence(req, reportId, evidenceDAO);
            if (!asDraft) {
                assignCase(reportId, r);
                Flash.success(req.getSession(), "Report submitted successfully — your Case ID is #" + reportId + ".");
            } else {
                Flash.success(req.getSession(), "Draft saved. You can finish and submit it from My Reports.");
            }
            resp.sendRedirect(req.getContextPath() + "/report?id=" + reportId);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /** Auto-assign to the least-loaded teacher of the school, if any. */
    private void assignCase(String reportId, ReportBean r) throws SQLException {
        List<UserBean> teachers = userDAO.teachersOfSchool(r.getSchoolId());
        if (!teachers.isEmpty()) {
            caseDAO.createForReport(reportId, teachers.get(0).getUserId(), r.getSeverity());
        }
    }

    /** Stores any uploaded "evidence" parts. Shared with ReportEditServlet; no-op for non-multipart posts. */
    static void saveEvidence(HttpServletRequest req, String reportId, EvidenceDAO evidenceDAO)
            throws IOException, ServletException, SQLException {
        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/")) return;
        for (Part part : req.getParts()) {
            if (!"evidence".equals(part.getName()) || part.getSize() == 0) continue;
            String relative = Uploads.save(part, reportId);
            evidenceDAO.insert(reportId, relative, Uploads.detectType(part.getContentType()), part.getSize());
        }
    }

    /** Shared server-side validation for create and edit. */
    static ReportBean readForm(HttpServletRequest req, UserBean user, Map<String, String> errors) {
        ReportBean r = new ReportBean();
        r.setSchoolId(user.getSchoolId());
        r.setReporterId(user.getUserId());
        r.setTitle(trim(req.getParameter("title")));
        r.setCategoryId(trim(req.getParameter("categoryId")));
        r.setLocation(emptyToNull(trim(req.getParameter("location"))));
        r.setDescription(trim(req.getParameter("description")));
        r.setSeverity(trim(req.getParameter("severity")));
        r.setAnonymityFlag(true);

        if (r.getTitle().isEmpty()) {
            errors.put("title", "Please give your report a short title.");
        } else if (r.getTitle().length() > 100) {
            errors.put("title", "Title must be 100 characters or fewer.");
        }
        if (r.getCategoryId().isEmpty()) {
            errors.put("categoryId", "Please choose a category.");
        }
        if (r.getDescription().isEmpty()) {
            errors.put("description", "Please describe what happened.");
        }
        if (!List.of("Low", "Medium", "High", "Critical").contains(r.getSeverity())) {
            errors.put("severity", "Please choose how serious the incident was.");
        }
        String date = trim(req.getParameter("incidentDate"));
        if (!date.isEmpty()) {
            try {
                LocalDate d = LocalDate.parse(date);
                if (d.isAfter(LocalDate.now())) {
                    errors.put("incidentDate", "The incident date cannot be in the future.");
                } else {
                    r.setIncidentDate(Date.valueOf(d));
                }
            } catch (Exception e) {
                errors.put("incidentDate", "Please enter a valid date.");
            }
        }
        return r;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String emptyToNull(String s) {
        return s.isEmpty() ? null : s;
    }
}
