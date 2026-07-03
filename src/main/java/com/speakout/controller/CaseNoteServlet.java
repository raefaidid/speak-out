package com.speakout.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.speakout.dao.CaseDAO;
import com.speakout.dao.CaseNoteDAO;
import com.speakout.bean.CaseBean;
import com.speakout.bean.UserBean;
import com.speakout.util.Flash;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Screen 9: add an investigation note (internal or reporter-visible). */
public class CaseNoteServlet extends HttpServlet {

    private final CaseDAO caseDAO = new CaseDAO();
    private final CaseNoteDAO noteDAO = new CaseNoteDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        String body = req.getParameter("body") == null ? "" : req.getParameter("body").trim();
        String visibility = "reporter-visible".equals(req.getParameter("visibility"))
                ? "reporter-visible" : "internal";
        try {
            CaseBean cs = CaseServlet.accessibleCase(req.getParameter("id"), user, caseDAO);
            if (cs == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (body.isEmpty()) {
                Flash.error(req.getSession(), "The note cannot be empty.");
            } else {
                noteDAO.insert(cs.getCaseId(), user.getUserId(), body, visibility);
                Flash.success(req.getSession(), "Investigation note added successfully.");
            }
            resp.sendRedirect(req.getContextPath() + "/case?id=" + cs.getCaseId());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
