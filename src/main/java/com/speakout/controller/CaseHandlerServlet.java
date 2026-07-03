package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.speakout.dao.CaseDAO;
import com.speakout.dao.UserDAO;
import com.speakout.bean.CaseBean;
import com.speakout.bean.UserBean;
import com.speakout.util.Flash;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Screen 9: add a co-handler (teacher of the same school) to a case. */
public class CaseHandlerServlet extends HttpServlet {

    private final CaseDAO caseDAO = new CaseDAO();
    private final UserDAO userDAO = new UserDAO();

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
            UserBean teacher = userDAO.findById(req.getParameter("userId"));
            boolean sameSchool = teacher != null && teacher.isTeacher()
                    && teacher.getSchoolId().equals(cs.getReport().getSchoolId());
            if (!sameSchool) {
                Flash.error(req.getSession(), "Co-handlers must be teachers from the same school.");
            } else if (teacher.getUserId().equals(cs.getAssignedTo())) {
                Flash.error(req.getSession(), teacher.getFullName() + " is already the lead handler.");
            } else {
                caseDAO.addHandler(cs.getCaseId(), teacher.getUserId());
                Flash.success(req.getSession(), teacher.getFullName() + " added as co-handler successfully.");
            }
            resp.sendRedirect(req.getContextPath() + "/case?id=" + cs.getCaseId());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
