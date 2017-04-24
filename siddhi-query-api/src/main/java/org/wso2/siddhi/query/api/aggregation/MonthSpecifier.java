package org.wso2.siddhi.query.api.aggregation;

public class MonthSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.MONTHS;

    public MonthSpecifier() {

    }

    public TimeSpecifier.Duration getDuration() {
        return duration;
    }
}
