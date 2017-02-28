package org.wso2.siddhi.query.api.aggregation;

public class SecondSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.SECONDS;

    public SecondSpecifier() {

    }

    public TimeSpecifier.Duration getDuration() {
        return this.duration;
    }
}
