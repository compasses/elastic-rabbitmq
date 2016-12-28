package sync.handler;

import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import sync.common.ESHandleMessage;

/**
 * Created by I311352 on 10/17/2016.
 */
public interface ESHandler {
    public void onMessage(ESHandleMessage message);
}
