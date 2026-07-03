package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.speakout.dao.AnalyticsDAO;
import com.speakout.bean.UserBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Screen 10: Admin cross-school analytics dashboard (Chart.js + live GROUP BYs). */
public class AnalyticsServlet extends HttpServlet {

    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        if (!user.isAdmin()) {
            resp.sendRedirect(req.getContextPath() + LoginServlet.homeFor(user));
            return;
        }
        try {
            req.setAttribute("kpis", analyticsDAO.kpis());
            req.setAttribute("reportsPerMonth", analyticsDAO.reportsPerMonth());
            req.setAttribute("casesPerSchool", analyticsDAO.casesPerSchool());
            req.getRequestDispatcher("/WEB-INF/jsp/staff/analytics.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
