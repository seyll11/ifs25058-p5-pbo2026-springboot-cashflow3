package org.delcom.ifs00000springbootcashflow.framework.auth;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.delcom.ifs00000springbootcashflow.domain.entity.User;
import org.delcom.ifs00000springbootcashflow.domain.repository.IUserRepository;

/**
 * Sesi login: menyimpan id user di VaadinSession + HTTP session + cookie bertanda tangan (HMAC).
 * Flag LOGGED_OUT_KEY mencegah sesi dipulihkan otomatis dari cookie setelah logout.
 */
public final class AuthSession {

    public static final String COOKIE_NAME = "cashflow_uid";
    static final String USER_ID_KEY = "cashflow.userId";
    static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    private static final int COOKIE_MAX_AGE = 8 * 60 * 60;

    private static final byte[] SECRET = resolveSecret();

    private AuthSession() {
    }

    public static void login(User user) {
        VaadinSession vs = VaadinSession.getCurrent();
        vs.setAttribute(USER_ID_KEY, user.id());
        vs.setAttribute(LOGGED_OUT_KEY, null);
        WrappedSession http = vs.getSession();
        if (http != null) {
            http.setAttribute(USER_ID_KEY, user.id());
            http.removeAttribute(LOGGED_OUT_KEY);
        }
        writeCookie(sign(user.id()), COOKIE_MAX_AGE);
    }

    public static void logout() {
        VaadinSession vs = VaadinSession.getCurrent();
        vs.setAttribute(USER_ID_KEY, null);
        vs.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        WrappedSession http = vs.getSession();
        if (http != null) {
            http.removeAttribute(USER_ID_KEY);
            http.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        }
        writeCookie("", 0);
    }

    /** User yang sedang login, atau null. Memulihkan sesi setelah refresh bila belum logout. */
    public static User currentUser(IUserRepository users) {
        VaadinSession vs = VaadinSession.getCurrent();
        if (vs == null) {
            return null;
        }
        Object id = vs.getAttribute(USER_ID_KEY);
        if (id instanceof Integer uid) {
            return users.findById(uid).orElse(null);
        }
        if (isLoggedOut(vs)) {
            return null;
        }
        Integer restored = restoredId(vs);
        if (restored == null) {
            return null;
        }
        Optional<User> user = users.findById(restored);
        user.ifPresent(u -> vs.setAttribute(USER_ID_KEY, u.id()));
        return user.orElse(null);
    }

    private static boolean isLoggedOut(VaadinSession vs) {
        if (Boolean.TRUE.equals(vs.getAttribute(LOGGED_OUT_KEY))) {
            return true;
        }
        WrappedSession http = vs.getSession();
        return http != null && Boolean.TRUE.equals(http.getAttribute(LOGGED_OUT_KEY));
    }

    private static Integer restoredId(VaadinSession vs) {
        WrappedSession http = vs.getSession();
        if (http != null && http.getAttribute(USER_ID_KEY) instanceof Integer uid) {
            return uid;
        }
        VaadinRequest request = VaadinRequest.getCurrent();
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie c : request.getCookies()) {
            if (COOKIE_NAME.equals(c.getName())) {
                return verify(c.getValue());
            }
        }
        return null;
    }

    private static void writeCookie(String value, int maxAge) {
        VaadinResponse response = VaadinResponse.getCurrent();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    // ---- penandatanganan cookie: "<id>.<hmac-hex>" ----

    static String sign(int userId) {
        return userId + "." + hmac(Integer.toString(userId));
    }

    static Integer verify(String cookieValue) {
        if (cookieValue == null) {
            return null;
        }
        int dot = cookieValue.indexOf('.');
        if (dot <= 0) {
            return null;
        }
        String idPart = cookieValue.substring(0, dot);
        String sig = cookieValue.substring(dot + 1);
        if (!MessageDigest.isEqual(hmac(idPart).getBytes(StandardCharsets.UTF_8), sig.getBytes(StandardCharsets.UTF_8))) {
            return null;
        }
        try {
            return Integer.valueOf(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC gagal", e);
        }
    }

    private static byte[] resolveSecret() {
        String configured = System.getProperty("SESSION_SECRET");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("SESSION_SECRET");
        }
        if (configured != null && !configured.isBlank()) {
            return configured.getBytes(StandardCharsets.UTF_8);
        }
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        return random;
    }
}
