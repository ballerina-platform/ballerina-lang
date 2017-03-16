package org.wso2.siddhi.query.api.aggregation;

public class YearSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.YEARS;

    public YearSpecifier() {

    }

    public TimeSpecifier.Duration getDuration() {
        return duration;
    }
}
