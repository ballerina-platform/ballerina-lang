package org.wso2.siddhi.query.api.aggregation;

public class HourSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.HOURS;

    private HourSpecifier() {

    }

    public TimeSpecifier.Duration getDuration() {
        return this.duration;
    }
}
