package org.wso2.carbon.transport.http.netty.statistics;

import java.util.Map;

/**
 * Created by chamile on 2/9/16.
 */
public interface MetricsStaticsHolder {

    boolean startTimer(String timer);

    boolean stopTimer(String timer);

    void setStatics(String timer, Long duration);

    Map<String, Long> getStatics(String timer);

    String getType();
}
