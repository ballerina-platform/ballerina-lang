package org.wso2.siddhi.query.test;

import junit.framework.Assert;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.condition.Condition.Operator;
import org.wso2.siddhi.query.api.definition.partition.PartitionDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class DefinePartitionTestCase {

    @Test
    public void testPartitionDefinition() throws RecognitionException, SiddhiParserException {
        PartitionDefinition partitionDefinition =
                SiddhiCompiler.parsePartitionDefinition("define partition bySymbolLarge by cseStream.symbol, range cseStream.volume >= 100 as 'Large'");
        Assert.assertEquals(new PartitionDefinition().name("bySymbolLarge")
                                    .partitionBy(Expression.variable("cseStream", "symbol"))
                                    .partitionBy(Condition.compare(Expression.variable("cseStream", "volume"),
                                                                   Operator.GREATER_THAN_EQUAL,
                                                                   Expression.value(100)), "Large"),
                            partitionDefinition);
    }

    @Test
    public void testPartitionDefinition2() throws RecognitionException, SiddhiParserException {
        PartitionDefinition partitionDefinition =
                SiddhiCompiler.parsePartitionDefinition("define partition bySymbolLarge by cseStream.symbol, range cseStream.volume >= 100 as 'Large'");
        Assert.assertNotSame(new PartitionDefinition().name("bySymbolLarge")
                                    .partitionBy(Expression.variable("cseStream", "symbol"))
                                    .partitionBy(Condition.compare(Expression.variable("cseStream", "volume"),
                                                                   Operator.GREATER_THAN,
                                                                   Expression.value(100)), "Large"),
                            partitionDefinition);
    }

}
