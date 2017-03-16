package org.wso2.siddhi.query.api.aggregation;

public class DaySpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.DAYS;

    public DaySpecifier() {

    }

    public DaySpecifier.Duration getDuration() {
        return this.duration;
    }
}
