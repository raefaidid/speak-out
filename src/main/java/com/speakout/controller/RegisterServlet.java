package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

import com.speakout.dao.SchoolDAO;
import com.speakout.dao.UserDAO;
import com.speakout.bean.SchoolBean;
import com.speakout.bean.UserBean;
import com.speakout.util.Flash;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RegisterServlet extends HttpServlet {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final UserDAO userDAO = new UserDAO();
    private final SchoolDAO schoolDAO = new SchoolDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String role = trim(req.getParameter("role"));
        String fullName = trim(req.getParameter("fullName"));
        String schoolCode = trim(req.getParameter("schoolCode"));
        String classForm = trim(req.getParameter("classForm"));
        String email = trim(req.getParameter("email"));
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirmPassword");
        boolean pdpa = req.getParameter("pdpa") != null;

        Map<String, String> errors = new LinkedHashMap<>();
        SchoolBean school = null;
        try {
            if (!"Student".equals(role) && !"Teacher".equals(role)) {
                errors.put("role", "Please choose whether you are registering as a Student or a Teacher.");
            }
            if (fullName.isEmpty() || fullName.length() < 3) {
                errors.put("fullName", "Please enter your full name (at least 3 characters).");
            } else if (fullName.length() > 100) {
                errors.put("fullName", "Full name must be 100 characters or fewer.");
            }
            if (schoolCode.isEmpty()) {
                errors.put("schoolCode", "Please enter your school code, e.g. BEA0091.");
            } else {
                school = schoolDAO.findByCode(schoolCode);
                if (school == null) {
                    errors.put("schoolCode", "School code \"" + schoolCode + "\" was not found. Check with your school office.");
                }
            }
            if (email.isEmpty() || !EMAIL.matcher(email).matches()) {
                errors.put("email", "Please enter a valid email address.");
            } else if (userDAO.emailExists(email)) {
                errors.put("email", "An account with this email already exists. Try logging in instead.");
            }
            if (password == null || password.length() < 8
                    || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
                errors.put("password", "Password must be at least 8 characters and contain both letters and numbers.");
            } else if (!password.equals(confirm)) {
                errors.put("confirmPassword", "Passwords do not match.");
            }
            if (!pdpa) {
                errors.put("pdpa", "You must agree to the PDPA notice before registering.");
            }

            if (!errors.isEmpty()) {
                req.setAttribute("errors", errors);
                req.setAttribute("form", req.getParameterMap());
                req.getRequestDispatcher("/WEB-INF/jsp/register.jsp").forward(req, resp);
                return;
            }

            UserBean u = new UserBean();
            u.setSchoolId(school.getSchoolId());
            u.setFullName(fullName);
            u.setEmail(email);
            u.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
            u.setRole(role);
            u.setClassForm("Student".equals(role) && !classForm.isEmpty() ? classForm : null);
            userDAO.insert(u);

            Flash.success(req.getSession(true), "Account created successfully. You can now log in.");
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
