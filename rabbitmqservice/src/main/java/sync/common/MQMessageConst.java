package sync.common;

/**
 * Created by I311352 on 12/28/2016.
 */
public class MQMessageConst {
    public static final String H_MESSAGEID = "X-Message-ID";
    public static final String H_TRACEID = "X-Trace-ID";
    public static final String H_TENANTID = "X-Tenant-ID";
    public static final String H_EMPLOYEEID = "X-Employee-ID";
    public static final String H_USERID = "X-User-ID";
    public static final String H_USERTYPE = "X-User-Type";
    public static final String H_USER_LOCALE = "X-Locale";
    public static final String H_SYSTEM_LOCALE = "X-System-Locale";
    public static final String H_SESSIONID = "X-Session-ID";
    public static final String H_SOURCE = "X-Source";
    public static final String H_ORIGIN_USER_ID = "X-Origin-User-ID";
    public static final String H_ORIGIN_EMPLOYEE_ID = "X-Origin-Employee-ID";
    public static final String H_ACCESS_TOKEN = "X-Access-Token";
    public static final String H_ORIGIN_USER_TYPE = "X-Origin-User-Type";
    public static final String H_RO_ID = "X-Ro-ID";
    public static String H_RETRIGGER_FLAG = "false";
    public static final String H_EVENT_TYPE = "X-Event-Type";
    public static final String H_JOBNAME = "header_jobname";
    public static final String H_SIMPLEJOBNAME = "header_simplejobname";
    public static final String H_IMPERSONATE = "header_impersonate";
    public static final String H_STARTTIME = "header_starttime";
    public static final String H_REPEATCOUNT = "header_repeatcount";
    public static final String QUARTZMSGQUEUENAME = "quartz.msg.queue";
    public static final String QUARTZMSGUSERDATAMAP = "header_quartzmsg_userdatamap";
    public static final String H_INTERVALINSECONDS = "header_intervalInSeconds";
    public static final String H_JOBDATA = "header_jobData";
    public static final String H_JOBPREFIX = "header_jobPrefix";

    public MQMessageConst() {
    }
}
