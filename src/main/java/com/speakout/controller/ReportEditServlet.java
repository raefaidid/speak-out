package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;
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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Screen 5 (Update): edit allowed while Draft/Submitted; submitting a draft creates its case. */
public class ReportEditServlet extends HttpServlet {

    private final ReportDAO reportDAO = new ReportDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final CaseDAO caseDAO = new CaseDAO();
    private final UserDAO userDAO = new UserDAO();
    private final EvidenceDAO evidenceDAO = new EvidenceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        try {
            ReportBean r = ownedEditable(req, user);
            if (r == null) {
                Flash.error(req.getSession(), "That report can no longer be edited.");
                resp.sendRedirect(req.getContextPath() + "/reports");
                return;
            }
            req.setAttribute("report", r);
            req.setAttribute("categories", categoryDAO.listAll());
            req.setAttribute("evidenceList", evidenceDAO.listByReport(r.getReportId()));
            req.setAttribute("mode", "edit");
            req.getRequestDispatcher("/WEB-INF/jsp/student/report-form.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        try {
            ReportBean existing = ownedEditable(req, user);
            if (existing == null) {
                Flash.error(req.getSession(), "That report can no longer be edited.");
                resp.sendRedirect(req.getContextPath() + "/reports");
                return;
            }
            Map<String, String> errors = new LinkedHashMap<>();
            ReportBean r = ReportFormServlet.readForm(req, user, errors);
            r.setReportId(existing.getReportId());
            if (!errors.isEmpty()) {
                req.setAttribute("errors", errors);
                req.setAttribute("report", r);
                req.setAttribute("categories", categoryDAO.listAll());
                req.setAttribute("evidenceList", evidenceDAO.listByReport(r.getReportId()));
                req.setAttribute("mode", "edit");
                req.getRequestDispatcher("/WEB-INF/jsp/student/report-form.jsp").forward(req, resp);
                return;
            }
            boolean submittingDraft = "Draft".equals(existing.getStatus())
                    && "submit".equals(req.getParameter("action"));
            r.setStatus(submittingDraft ? "Submitted" : existing.getStatus());
            reportDAO.update(r);
            ReportFormServlet.saveEvidence(req, r.getReportId(), evidenceDAO);
            if (submittingDraft && caseDAO.findByReportId(r.getReportId()) == null) {
                List<UserBean> teachers = userDAO.teachersOfSchool(user.getSchoolId());
                if (!teachers.isEmpty()) {
                    caseDAO.createForReport(r.getReportId(), teachers.get(0).getUserId(), r.getSeverity());
                }
            }
            Flash.success(req.getSession(), submittingDraft
                    ? "Report submitted successfully — your Case ID is #" + r.getReportId() + "."
                    : "Report #" + r.getReportId() + " updated successfully.");
            resp.sendRedirect(req.getContextPath() + "/report?id=" + r.getReportId());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /** The report, if it exists, belongs to this student, and is still editable. */
    private ReportBean ownedEditable(HttpServletRequest req, UserBean user) throws SQLException {
        if (!user.isStudent()) return null;
        ReportBean r = reportDAO.findById(req.getParameter("id"));
        if (r == null || !r.getReporterId().equals(user.getUserId()) || !r.isEditable()) return null;
        return r;
    }
}
