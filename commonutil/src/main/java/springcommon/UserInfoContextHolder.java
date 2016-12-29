package springcommon;

/**
 * Created by I311352 on 12/29/2016.
 */
public class UserInfoContextHolder {

    private static final ThreadLocal<UserInfo> userInfoLocal = new InheritableThreadLocal<UserInfo>() {
        @Override
        protected UserInfo initialValue() {
            return new UserInfo();
        }
    };

    public static void setUserInfo(UserInfo userInfo) {
        if (userInfo != null) {
            userInfoLocal.set(userInfo);
        }
    }

    public static void clear() {
        userInfoLocal.remove();
    }


    public static void setUserInfo(Long tenantId, Long userId, Long employeeId, String locale, String messageId,
                                   String traceId, String sessionId, String jwt) {
        UserInfo info = userInfoLocal.get();
        info.employeeId = employeeId;
        info.userId = userId;
        info.tenantId = tenantId;
        info.locale = locale;
        info.messageId = messageId;
        info.traceId = traceId;
        info.sessionId = sessionId;
        info.userType = "key";
        info.jwt = jwt;
    }

    public static Long getTenantId() {
        return userInfoLocal.get().tenantId;
    }

    public static Long getUserId() {
        return userInfoLocal.get().userId;
    }

    public static Long getEmployeeId() {
        return userInfoLocal.get().employeeId;
    }

    public static String getLocale() {
        return userInfoLocal.get().locale;
    }

    public static String getMessageId() {
        return userInfoLocal.get().messageId;
    }

    public static String getTraceId() {
        return userInfoLocal.get().traceId;
    }

    public static String getSessionId() {
        return userInfoLocal.get().sessionId;
    }

    public static String getUserType() {
        return userInfoLocal.get().userType;
    }

    public static String getSource() {
        return userInfoLocal.get().source;
    }

    public static String getSystemLocale(){
        return  userInfoLocal.get().systemlocale;
    }

    public static String getRoId(){
        return userInfoLocal.get().roId;
    }

    public static String getJwt(){
        return userInfoLocal.get().jwt;
    }

    public static class UserInfo {
        private Long tenantId = null;
        private Long userId = null;
        private Long employeeId = null;
        private String locale = null;
        private String messageId = null;
        private String traceId = null;
        private String sessionId = null;
        private String userType = null;
        private String source = null;
        private String systemlocale=null;
        private String roId=null;
        private String jwt = null;

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getSystemlocale() {
            return systemlocale;
        }

        public void setSystemlocale(String systemlocale) {
            this.systemlocale = systemlocale;
        }

        public String getRoId() {
            return roId;
        }

        public void setRoId(String roId) {
            this.roId = roId;
        }

        public String getJwt() {
            return jwt;
        }

        public void setJwt(String jwt) {
            this.jwt = jwt;
        }


    }
}

