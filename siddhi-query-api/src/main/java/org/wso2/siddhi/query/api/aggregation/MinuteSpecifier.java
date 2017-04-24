package org.wso2.siddhi.query.api.aggregation;

public class MinuteSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.DAYS;

    public MinuteSpecifier() {

    }

    public TimeSpecifier.Duration getDuration() {
        return duration;
    }
}
