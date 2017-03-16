package org.wso2.siddhi.query.api.aggregation;

public class HourSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.HOURS;

    public HourSpecifier() {

    }

    public TimeSpecifier.Duration getDuration() {
        return this.duration;
    }
}
