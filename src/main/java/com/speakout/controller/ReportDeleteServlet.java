package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.speakout.dao.ReportDAO;
import com.speakout.bean.ReportBean;
import com.speakout.bean.UserBean;
import com.speakout.util.Flash;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Screen 5 (Delete): drafts only, behind a confirmation modal. */
public class ReportDeleteServlet extends HttpServlet {

    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        try {
            ReportBean r = reportDAO.findById(req.getParameter("id"));
            if (r == null || !user.isStudent() || !r.getReporterId().equals(user.getUserId())
                    || !r.isDeletable()) {
                Flash.error(req.getSession(), "Only your own draft reports can be deleted.");
            } else {
                reportDAO.delete(r.getReportId());
                Flash.success(req.getSession(), "Draft #" + r.getReportId() + " deleted successfully.");
            }
            resp.sendRedirect(req.getContextPath() + "/reports");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
