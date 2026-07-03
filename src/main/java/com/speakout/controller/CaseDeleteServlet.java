package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.speakout.dao.CaseDAO;
import com.speakout.dao.ReportDAO;
import com.speakout.bean.CaseBean;
import com.speakout.bean.UserBean;
import com.speakout.util.Flash;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Screen 9 (Delete): invalidate a case behind a confirmation modal. The case and
 * its notes are removed; the underlying report returns to "Submitted".
 */
public class CaseDeleteServlet extends HttpServlet {

    private final CaseDAO caseDAO = new CaseDAO();
    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        try {
            CaseBean cs = CaseServlet.accessibleCase(req.getParameter("id"), user, caseDAO);
            if (cs == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            caseDAO.delete(cs.getCaseId());
            reportDAO.updateStatus(cs.getReportId(), "Submitted");
            Flash.success(req.getSession(),
                    "Case " + cs.getCaseId() + " deleted successfully. Report #" + cs.getReportId()
                            + " returned to the submitted queue.");
            resp.sendRedirect(req.getContextPath() + "/cases");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
