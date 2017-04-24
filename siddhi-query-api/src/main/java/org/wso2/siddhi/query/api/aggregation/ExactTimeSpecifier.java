package org.wso2.siddhi.query.api.aggregation;

import java.util.ArrayList;
import java.util.List;

public class ExactTimeSpecifier extends TimeSpecifier {
    private List<TimeSpecifier> exactTimeSpecifiers = null;
    private TimeSpecifier right;

    public ExactTimeSpecifier() {
        this.exactTimeSpecifiers = new ArrayList<TimeSpecifier>();
    }

    public ExactTimeSpecifier add(TimeSpecifier timeSpecifier) {
        this.exactTimeSpecifiers.add(timeSpecifier);
        return this;
    }

    public List<TimeSpecifier> getExactTimeSpecifiers() {
        return this.exactTimeSpecifiers;
    }
}
