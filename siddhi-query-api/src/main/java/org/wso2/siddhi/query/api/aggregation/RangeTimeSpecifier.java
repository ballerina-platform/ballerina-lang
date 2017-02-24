package org.wso2.siddhi.query.api.aggregation;


public class RangeTimeSpecifier extends TimeSpecifier {

    private TimeSpecifier leftSpecifier;
    private TimeSpecifier rightSpecifier;

    public RangeTimeSpecifier(TimeSpecifier left, TimeSpecifier right) {
        this.leftSpecifier = left;
        this.rightSpecifier = right;
    }

    public TimeSpecifier getLeftSpecifier() {
        return this.leftSpecifier;
    }

    public TimeSpecifier getRightSpecifier() {
        return this.rightSpecifier;
    }
}
