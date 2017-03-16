package org.wso2.siddhi.query.api.aggregation;

import java.util.ArrayList;
import java.util.List;

public class TimePeriod {

    public enum Duration {SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS}

    public enum Operator {RANGE, INTERVAL}

    private Operator operator;
    private List<Duration> durations;

    private TimePeriod(Operator operator) {
        this.durations = new ArrayList<Duration>();
        this.operator = operator;
    }

    public Operator getOperator() {
        return this.operator;
    }

    public List<Duration> getDurations() {
        return this.durations;
    }

    public static TimePeriod range(Duration left, Duration right) { // range sec ... min
        TimePeriod timePeriod = new TimePeriod(Operator.INTERVAL);
        timePeriod.durations.add(left);
        timePeriod.durations.add(right);
        return timePeriod;
    }

    public static TimePeriod interval(Duration... durations) { // interval sec, min, time
        TimePeriod timePeriod = new TimePeriod(Operator.RANGE);
        for (Duration duration : durations) {
            timePeriod.durations.add(duration);
        }
        return timePeriod;
    }
}
