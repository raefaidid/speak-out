package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.speakout.dao.ReportDAO;
import com.speakout.dao.UserDAO;
import com.speakout.bean.UserBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DashboardServlet extends HttpServlet {

    private final ReportDAO reportDAO = new ReportDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        if (!user.isStudent()) {
            resp.sendRedirect(req.getContextPath() + LoginServlet.homeFor(user));
            return;
        }
        try {
            req.setAttribute("stats", reportDAO.studentStats(user.getUserId()));
            req.setAttribute("recentReports", reportDAO.listRecentByReporter(user.getUserId(), 5));
            List<UserBean> teachers = userDAO.teachersOfSchool(user.getSchoolId());
            req.setAttribute("counsellor", teachers.isEmpty() ? null : teachers.get(0));
            req.getRequestDispatcher("/WEB-INF/jsp/student/dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
