package org.wso2.siddhi.query.api.aggregation;

/**
 * Created by upul on 2/23/17.
 */
public class YearSpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.YEAR;

    public YearSpecifier() {

    }

    public TimeSpecifier.Duration getDuration() {
        return duration;
    }
}
