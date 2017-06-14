package org.wso2.siddhi.query.api.aggregation;

import java.util.ArrayList;
import java.util.List;

public class TimePeriod {

    public enum Duration {SECONDS, MINUTES, HOURS, DAYS, MONTHS, YEARS}

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

    // TODO: 5/12/17 check whether names "range", "interval" are ok
    public static TimePeriod range(Duration left, Duration right) { // range sec ... min
        TimePeriod timePeriod = new TimePeriod(Operator.RANGE);
        timePeriod.durations.add(left);
        timePeriod.durations.add(right);
        return timePeriod;
    }

    public static TimePeriod interval(Duration... durations) { // interval sec, min, time
        TimePeriod timePeriod = new TimePeriod(Operator.INTERVAL);
        for (Duration duration : durations) {
            timePeriod.durations.add(duration);
        }
        return timePeriod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimePeriod that = (TimePeriod) o;

        if (operator != that.operator) return false;
        return durations != null ? durations.equals(that.durations) : that.durations == null;
    }

    @Override
    public int hashCode() {
        int result = operator.hashCode();
        result = 31 * result + (durations != null ? durations.hashCode() : 0);
        return result;
    }
}
