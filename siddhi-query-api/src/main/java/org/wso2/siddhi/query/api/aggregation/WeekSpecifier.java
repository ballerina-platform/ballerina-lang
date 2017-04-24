package org.wso2.siddhi.query.api.aggregation;

public class WeekSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.WEEKS;

    public WeekSpecifier() {

    }

    public TimeSpecifier.Duration getDuration() {
        return duration;
    }
}
