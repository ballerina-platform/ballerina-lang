package org.wso2.siddhi.core.aggregation;

import org.junit.Test;
import org.wso2.siddhi.core.util.parser.AggregationParser;
import org.wso2.siddhi.core.util.parser.AggregationRuntime;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;

public class AggregationTestCase {

    @Test
    public void basicTest() {

        AggregationDefinition aggregationDefinition = AggregationDefinition.id("StockAggregation").from(
                InputStream.stream("StockStream")).
                select(Selector.selector().
                        select("symbol", Expression.variable("symbol").ofStream("StockStream")).
                        select("avgPrice", Expression.function("avg", Expression.variable("price"))).
                        groupBy(Expression.variable("symbol").ofStream("StockStream"))).
                aggregateBy(Expression.variable("timeStamp")).every(
                TimePeriod.interval(TimePeriod.Duration.SECONDS, TimePeriod.Duration.MINUTES));

        AggregationRuntime aggregationRuntime =  AggregationParser.parse(aggregationDefinition);

    }
}
