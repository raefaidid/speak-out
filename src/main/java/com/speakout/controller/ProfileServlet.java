package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.speakout.dao.UserDAO;
import com.speakout.bean.UserBean;
import com.speakout.util.Flash;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Screen 7: My Profile — name and class editable, email/school read-only. */
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        String fullName = req.getParameter("fullName") == null ? "" : req.getParameter("fullName").trim();
        String classForm = req.getParameter("classForm") == null ? "" : req.getParameter("classForm").trim();

        Map<String, String> errors = new LinkedHashMap<>();
        if (fullName.length() < 3 || fullName.length() > 100) {
            errors.put("fullName", "Full name must be between 3 and 100 characters.");
        }
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, resp);
            return;
        }
        try {
            String cls = user.isStudent() && !classForm.isEmpty() ? classForm : null;
            userDAO.updateProfile(user.getUserId(), fullName, cls);
            // refresh the session copy so the nav/profile show the new values
            req.getSession().setAttribute("user", userDAO.findById(user.getUserId()));
            Flash.success(req.getSession(), "Profile updated successfully.");
            resp.sendRedirect(req.getContextPath() + "/profile");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
