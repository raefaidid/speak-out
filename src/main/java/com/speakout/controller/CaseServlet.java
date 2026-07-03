package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.speakout.dao.CaseDAO;
import com.speakout.dao.CaseNoteDAO;
import com.speakout.dao.EvidenceDAO;
import com.speakout.dao.UserDAO;
import com.speakout.bean.CaseBean;
import com.speakout.bean.UserBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Screen 9: Case Management Dashboard (Teacher/Admin). */
public class CaseServlet extends HttpServlet {

    private final CaseDAO caseDAO = new CaseDAO();
    private final CaseNoteDAO noteDAO = new CaseNoteDAO();
    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        try {
            CaseBean cs = accessibleCase(req.getParameter("id"), user, caseDAO);
            if (cs == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("caseRecord", cs);
            req.setAttribute("notes", noteDAO.listByCase(cs.getCaseId(), true));
            req.setAttribute("handlers", caseDAO.listHandlers(cs.getCaseId()));
            req.setAttribute("evidenceList", evidenceDAO.listByReport(cs.getReportId()));
            req.setAttribute("schoolTeachers", userDAO.teachersOfSchool(cs.getReport().getSchoolId()));
            req.getRequestDispatcher("/WEB-INF/jsp/staff/case-detail.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /** Admins reach any case; teachers only cases they lead or co-handle. */
    static CaseBean accessibleCase(String caseId, UserBean user, CaseDAO caseDAO) throws SQLException {
        if (!user.isStaff()) return null;
        CaseBean cs = caseDAO.findById(caseId);
        if (cs == null) return null;
        if (user.isAdmin()) return cs;
        if (user.getUserId().equals(cs.getAssignedTo())) return cs;
        boolean coHandler = caseDAO.listHandlers(cs.getCaseId()).stream()
                .anyMatch(h -> h.getUserId().equals(user.getUserId()));
        return coHandler ? cs : null;
    }
}
