package org.wso2.siddhi.core.util;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.ComplexEvent;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: suho
 * Date: 11/11/12
 * Time: 9:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogHelper {

    public static void logMethod(Logger log, ComplexEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("=>"+event + " @ " + Arrays.deepToString(new Object[]{Thread.currentThread().getStackTrace()[2]})+" threadId:"+Thread.currentThread().getName());
        }
    }

    public static void logMethod(Logger log, AtomicEvent atomicEvent) {
        if (log.isDebugEnabled()) {
            log.debug("=>"+atomicEvent + " @ " + Arrays.deepToString(new Object[]{Thread.currentThread().getStackTrace()[2]})+" threadId:"+Thread.currentThread().getName());
        }
    }

    public static void debugLogMessage(Logger log, ComplexEvent complexEvent, String message) {
            log.debug("=>"+complexEvent+" "+message );
    }

    public static void debugLogMessage(Logger log, AtomicEvent atomicEvent, String message) {
        log.debug("=>"+atomicEvent+" "+message );
    }
}
