package org.wso2.siddhi.query.api.aggregation;

/**
 * Created by upul on 2/23/17.
 */
public class WeekSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.WEEK;

    public WeekSpecifier(){

    }

    public TimeSpecifier.Duration getDuration(){
        return duration;
    }
}
