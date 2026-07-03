package com.speakout.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import com.speakout.dao.EvidenceDAO;
import com.speakout.dao.ReportDAO;
import com.speakout.bean.EvidenceBean;
import com.speakout.bean.ReportBean;
import com.speakout.bean.UserBean;
import com.speakout.util.Uploads;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Streams an evidence file to the reporter who uploaded it or to staff. */
public class EvidenceServlet extends HttpServlet {

    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserBean user = (UserBean) req.getSession().getAttribute("user");
        try {
            EvidenceBean ev = evidenceDAO.findById(req.getParameter("id"));
            ReportBean r = ev == null ? null : reportDAO.findById(ev.getReportId());
            if (ev == null || r == null
                    || (!r.getReporterId().equals(user.getUserId()) && !user.isStaff())) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Path file = Uploads.baseDir().resolve(ev.getFileUrl()).normalize();
            if (!file.startsWith(Uploads.baseDir()) || !Files.exists(file)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File is no longer available.");
                return;
            }
            String mime = Files.probeContentType(file);
            resp.setContentType(mime == null ? "application/octet-stream" : mime);
            resp.setContentLengthLong(Files.size(file));
            resp.setHeader("Content-Disposition", "inline; filename=\"" + ev.getFileName() + "\"");
            Files.copy(file, resp.getOutputStream());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
