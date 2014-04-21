package org.wso2.siddhi.query.api;

import org.junit.Test;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.query.Query;

public class PartitionQueryTestCase {

	@Test
	public void testCreatingPartitionTestCase() {
        Query query = QueryFactory.createQuery();
        query.from(
                QueryFactory.inputStream("cseEventStream").
                        filter(Condition.and(Condition.compare(Expression.add(Expression.value(7), Expression.value(9.5)),
                                                               Condition.Operator.GREATER_THAN,
                                                               Expression.variable("price")),
                                             Condition.compare(Expression.value(100),
                                                               Condition.Operator.GREATER_THAN_EQUAL,
                                                               Expression.variable("volume")
                                             )
                        )
                        ).window("lengthBatch", Expression.value(50))
        );
        query.insertInto("StockQuote");
        query.select(
                QueryFactory.outputSelector().
                        select("symbol", Expression.variable("symbol")).
                        select("avgPrice", "avg", Expression.variable("symbol")).
                        groupBy("symbol").
                        having(Condition.compare(Expression.variable("avgPrice"),
                                                 Condition.Operator.GREATER_THAN_EQUAL,
                                                 Expression.value(50)
                        ))
        );
        query.partitionBy("symbolPartition");
	}

}
