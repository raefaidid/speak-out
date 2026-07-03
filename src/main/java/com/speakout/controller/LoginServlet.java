package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import com.speakout.dao.UserDAO;
import com.speakout.bean.UserBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + homeFor((UserBean) session.getAttribute("user")));
            return;
        }
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String keep = req.getParameter("keepLoggedIn");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            fail(req, resp, email, "Please enter both your email and password.");
            return;
        }
        try {
            UserBean user = userDAO.findByEmail(email);
            if (user == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
                fail(req, resp, email, "Email or password is incorrect.");
                return;
            }
            if (!user.isActive()) {
                fail(req, resp, email, "This account has been deactivated. Please contact your school admin.");
                return;
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            if (keep != null) {
                session.setMaxInactiveInterval(7 * 24 * 60 * 60);   // "keep me logged in": 7 days
            }
            resp.sendRedirect(req.getContextPath() + homeFor(user));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String email, String message)
            throws ServletException, IOException {
        req.setAttribute("error", message);
        req.setAttribute("email", email);
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

    static String homeFor(UserBean user) {
        if (user.isAdmin()) return "/analytics";
        if (user.isTeacher()) return "/cases";
        return "/dashboard";
    }
}
