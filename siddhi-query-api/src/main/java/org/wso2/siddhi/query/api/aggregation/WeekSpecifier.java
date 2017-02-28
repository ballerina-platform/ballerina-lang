package org.wso2.siddhi.query.api.aggregation;

public class WeekSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.WEEKS;

    private WeekSpecifier() {

    }

    public TimeSpecifier.Duration getDuration() {
        return duration;
    }
}
