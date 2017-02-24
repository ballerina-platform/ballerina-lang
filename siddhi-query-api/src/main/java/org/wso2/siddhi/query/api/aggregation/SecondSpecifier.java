package org.wso2.siddhi.query.api.aggregation;

/**
 * Created by upul on 2/21/17.
 */
public class SecondSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.SECOND;

    public SecondSpecifier() {

    }

    public TimeSpecifier.Duration getDuration() {
        return this.duration;
    }
}
