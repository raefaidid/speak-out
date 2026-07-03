package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.speakout.dao.CaseDAO;
import com.speakout.dao.CategoryDAO;
import com.speakout.bean.UserBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Screen 8: Teacher main page — cases assigned to you (admins see every case). */
public class CasesServlet extends HttpServlet {

    private final CaseDAO caseDAO = new CaseDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        if (!user.isStaff()) {
            resp.sendRedirect(req.getContextPath() + LoginServlet.homeFor(user));
            return;
        }
        try {
            String q = req.getParameter("q");
            String categoryId = req.getParameter("category");
            String status = req.getParameter("status");
            req.setAttribute("stats", caseDAO.staffStats(user));
            req.setAttribute("cases", caseDAO.listForStaff(user, q, categoryId, status));
            req.setAttribute("categories", categoryDAO.listAll());
            req.getRequestDispatcher("/WEB-INF/jsp/staff/cases.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
