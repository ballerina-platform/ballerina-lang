package org.wso2.siddhi.query.api.aggregation;

/**
 * Created by upul on 2/23/17.
 */
public class MonthSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.MONTH;

    public MonthSpecifier(){

    }

    public TimeSpecifier.Duration getDuration(){
        return duration;
    }
}
