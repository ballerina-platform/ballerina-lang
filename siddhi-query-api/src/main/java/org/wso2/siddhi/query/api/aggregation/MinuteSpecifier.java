package org.wso2.siddhi.query.api.aggregation;

/**
 * Created by upul on 2/22/17.
 */
public class MinuteSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.DAY;

    public MinuteSpecifier() {

    }

    public TimeSpecifier.Duration getDuration() {
        return duration;
    }
}
