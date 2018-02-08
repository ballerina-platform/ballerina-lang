package org.ballerinalang.test.services.testutils;

import org.ballerinalang.net.http.Constants;

public class CookieUtils {

    public static Cookie getCookie(String headerValue) {
        String[] headerSplits = headerValue.split(";");
        Cookie cookie = new Cookie();

        if (headerSplits.length > 0) {
            cookie.name = headerSplits[0].split("=")[0];
            cookie.value = headerSplits[0].split("=")[1];

            for (String headerSplit : headerSplits) {
                if (headerSplit.trim().equals(Constants.HTTP_ONLY)) {
                    cookie.httpOnly = true;
                } else if (headerSplit.trim().equals(Constants.SECURE)) {
                    cookie.secure = true;
                } else if (headerSplit.trim().startsWith(Constants.PATH)) {
                    cookie.path = headerSplit.split("=")[1];
                }
            }
        }

        return cookie;
    }

    public static class Cookie {
        public String name;
        public String value;
        public String path;
        public boolean secure;
        public boolean httpOnly;

        @Override
        public String toString() {
            return "Cookie{" +
                    "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    ", path='" + path + '\'' +
                    ", secure=" + secure +
                    ", httpOnly=" + httpOnly +
                    '}';
        }
    }
}
