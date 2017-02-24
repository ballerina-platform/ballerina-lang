package org.wso2.siddhi.query.api.aggregation;

public class TimeSpecifier {

    public enum Duration {SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR}

    public static TimeSpecifier second() {
        return new SecondSpecifier();
    }

    public static TimeSpecifier minute() {
        return new MinuteSpecifier();
    }

    public static TimeSpecifier hour() {
        return new HourSpecifier();
    }

    public static TimeSpecifier day() {
        return new DaySpecifier();
    }

    public static TimeSpecifier week() {
        return new WeekSpecifier();
    }

    public static TimeSpecifier month() {
        return new MonthSpecifier();
    }

    public static TimeSpecifier year() {
        return new YearSpecifier();
    }

    public static RangeTimeSpecifier range(TimeSpecifier left, TimeSpecifier right) {
        return new RangeTimeSpecifier(left, right);
    }

    public static ExactTimeSpecifier exact() {
        return new ExactTimeSpecifier();
    }

//    public void test(){
//        TimeSpecifier.exact(TimeSpecifier.minute(), TimeSpecifier.second());
//        TimeSpecifier.exact(TimeSpecifier.second(),
//                (TimeSpecifier.exact( TimeSpecifier.second(), TimeSpecifier.minute())));
//    }
}
