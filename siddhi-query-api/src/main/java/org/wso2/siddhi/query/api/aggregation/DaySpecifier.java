package org.wso2.siddhi.query.api.aggregation;

/**
 * Created by upul on 2/23/17.
 */
public class DaySpecifier extends TimeSpecifier {
    private TimeSpecifier.Duration duration = Duration.DAY;

    public DaySpecifier() {

    }

    public DaySpecifier.Duration getDuration() {
        return this.duration;
    }
}
