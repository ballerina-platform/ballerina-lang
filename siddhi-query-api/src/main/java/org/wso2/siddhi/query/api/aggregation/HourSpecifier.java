package org.wso2.siddhi.query.api.aggregation;

/**
 * Created by upul on 2/23/17.
 */
public class HourSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.HOUR;

    public HourSpecifier(){

    }

    public TimeSpecifier.Duration getDuration(){
        return this.duration;
    }
}
