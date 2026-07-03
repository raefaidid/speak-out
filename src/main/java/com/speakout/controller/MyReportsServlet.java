package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.speakout.dao.CategoryDAO;
import com.speakout.dao.ReportDAO;
import com.speakout.bean.UserBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Screen 5: My Reports list with search + category/status filters. */
public class MyReportsServlet extends HttpServlet {

    private final ReportDAO reportDAO = new ReportDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        if (!user.isStudent()) {
            resp.sendRedirect(req.getContextPath() + LoginServlet.homeFor(user));
            return;
        }
        try {
            String q = req.getParameter("q");
            String categoryId = req.getParameter("category");
            String status = req.getParameter("status");
            req.setAttribute("reports", reportDAO.listByReporter(user.getUserId(), q, categoryId, status));
            req.setAttribute("categories", categoryDAO.listAll());
            req.getRequestDispatcher("/WEB-INF/jsp/student/reports.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
