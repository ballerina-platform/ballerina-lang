package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.condition.In;

import java.util.*;

public class IncrementalCalculator {
    // has the property called Duration it can be SECOND, MINUTE and etc
    // list of functions operators
    // Also need to have a link to the parent for instance a link from SECOND to MINUTE
    // Next, based on basic calculators, we initialize a set of basic calculators
    //

    private TimePeriod.Duration duration;
    private IncrementalCalculator child;
    private List<AttributeFunction> functionAttributes;
    private List<IncrementalAggregator> incrementalAggregators;

    private IncrementalCalculator(TimePeriod.Duration duration, IncrementalCalculator child,
                                  List<AttributeFunction> functionAttributes) {
        this.duration = duration;
        this.child = child;
        this.functionAttributes = functionAttributes;
        this.incrementalAggregators = createIncrementalAggregators(this.functionAttributes);
    }

    private List<IncrementalAggregator> createIncrementalAggregators(List<AttributeFunction> functionAttributes) {
        List<IncrementalAggregator> incrementalAggregators = new ArrayList<>();
        for (AttributeFunction function : functionAttributes) {
            if (function.getName().equals("avg")) {
                AvgIncrementalAttributeAggregator average = new AvgIncrementalAttributeAggregator(function);
                incrementalAggregators.add(average);
            } else {
                // TODO: 3/10/17 Exception....
            }
        }
        return incrementalAggregators;
    }

    public Set<Expression> getBaseAggregators() {
        Set<Expression> baseAggregators = new HashSet<>();
        for (IncrementalAggregator aggregator : this.incrementalAggregators) {
            Expression[] bases = aggregator.getBaseAggregators();
            baseAggregators.addAll(Arrays.asList(bases));
        }
        return baseAggregators;
    }

    public static IncrementalCalculator second(List<AttributeFunction> functionAttributes, IncrementalCalculator child) {
        IncrementalCalculator second = new IncrementalCalculator(TimePeriod.Duration.SECONDS, child, functionAttributes);
        return second;
    }

    public static IncrementalCalculator minute(List<AttributeFunction> functionAttributes, IncrementalCalculator child) {
        IncrementalCalculator minute = new IncrementalCalculator(TimePeriod.Duration.MINUTES, child, functionAttributes);
        return minute;
    }

    public static IncrementalCalculator hour(List<AttributeFunction> functionAttributes, IncrementalCalculator child) {
        IncrementalCalculator hour = new IncrementalCalculator(TimePeriod.Duration.HOURS, child, functionAttributes);
        return hour;
    }

    public static IncrementalCalculator day(List<AttributeFunction> functionAttributes, IncrementalCalculator child) {
        IncrementalCalculator day = new IncrementalCalculator(TimePeriod.Duration.DAYS, child, functionAttributes);
        return day;
    }

    public static IncrementalCalculator week(List<AttributeFunction> functionAttributes, IncrementalCalculator child) {
        IncrementalCalculator week = new IncrementalCalculator(TimePeriod.Duration.WEEKS, child, functionAttributes);
        return week;
    }

    public static IncrementalCalculator month(List<AttributeFunction> functionAttributes, IncrementalCalculator child) {
        IncrementalCalculator month = new IncrementalCalculator(TimePeriod.Duration.MONTHS, child, functionAttributes);
        return month;
    }

    public static IncrementalCalculator year(List<AttributeFunction> functionAttributes, IncrementalCalculator child) {
        IncrementalCalculator year = new IncrementalCalculator(TimePeriod.Duration.YEARS, child, functionAttributes);
        return year;
    }
}