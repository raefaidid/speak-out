package com.speakout.util;

import jakarta.servlet.http.HttpSession;

/**
 * One-shot success/error banner shown by the shared nav fragment on the next
 * page load (the fragment removes the attributes after rendering).
 */
public final class Flash {

    private Flash() {
    }

    public static void success(HttpSession session, String message) {
        session.setAttribute("flash", message);
        session.setAttribute("flashType", "success");
    }

    public static void error(HttpSession session, String message) {
        session.setAttribute("flash", message);
        session.setAttribute("flashType", "danger");
    }
}
