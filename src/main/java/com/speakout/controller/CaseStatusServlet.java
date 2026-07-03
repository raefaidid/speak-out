package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.speakout.dao.CaseDAO;
import com.speakout.dao.ReportDAO;
import com.speakout.bean.CaseBean;
import com.speakout.bean.UserBean;
import com.speakout.util.Flash;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Screen 9 (Update): status change behind a confirmation modal; syncs report status. */
public class CaseStatusServlet extends HttpServlet {

    private final CaseDAO caseDAO = new CaseDAO();
    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        String status = req.getParameter("status");
        try {
            CaseBean cs = CaseServlet.accessibleCase(req.getParameter("id"), user, caseDAO);
            if (cs == null || !List.of("New", "Under Investigation", "Resolved", "Closed").contains(status)) {
                Flash.error(req.getSession(), "Could not update that case.");
                resp.sendRedirect(req.getContextPath() + "/cases");
                return;
            }
            caseDAO.updateStatus(cs.getCaseId(), status);
            switch (status) {
                case "Under Investigation" -> reportDAO.updateStatus(cs.getReportId(), "In review");
                case "Resolved", "Closed" -> reportDAO.updateStatus(cs.getReportId(), "Resolved");
                case "New" -> reportDAO.updateStatus(cs.getReportId(), "Submitted");
            }
            Flash.success(req.getSession(),
                    "Case " + cs.getCaseId() + " status updated to \"" + status + "\" successfully.");
            resp.sendRedirect(req.getContextPath() + "/case?id=" + cs.getCaseId());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
