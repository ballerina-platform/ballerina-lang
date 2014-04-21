/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.test.standard.partition;

import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.DifferentDefinitionAlreadyExistException;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.definition.partition.PartitionDefinition;
import org.wso2.siddhi.query.api.expression.Expression;

public class DefinePartitionTestCase {

    @Test
    public void testSingleDefinition() {
        SiddhiManager siddhiManager = new SiddhiManager();
        PartitionDefinition partitionDefinition = QueryFactory.createPartitionDefinition();
        partitionDefinition.name("testPartition")
                .partitionBy(
                        Expression.variable("StockStream", "symbol"))
                .partitionBy(
                        Condition.compare(
                                Expression.variable("cseEventStream", "price"),
                                Condition.Operator.GREATER_THAN_EQUAL,
                                Expression.value(100)),
                        "large");
        siddhiManager.definePartition(partitionDefinition);
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testAddingTwoSameDefinition1() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.definePartition(QueryFactory.createPartitionDefinition().name("testPartition").partitionBy(Expression.variable("StockStream", "symbol")).partitionBy(Condition.compare(Expression.variable("cseEventStream", "price"), Condition.Operator.GREATER_THAN_EQUAL, Expression.value(100)), "large"));
        siddhiManager.definePartition(QueryFactory.createPartitionDefinition().name("testPartition").partitionBy(Expression.variable("StockStream", "symbol")).partitionBy(Condition.compare(Expression.variable("cseEventStream", "prices"), Condition.Operator.GREATER_THAN_EQUAL, Expression.value(100)), "large"));
    }

    @Test
    public void testAddingTwoSameDefinition2() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.definePartition(QueryFactory.createPartitionDefinition().name("testPartition").partitionBy(Expression.variable("StockStream", "symbol")).partitionBy(Condition.compare(Expression.variable("cseEventStream", "price"), Condition.Operator.GREATER_THAN_EQUAL, Expression.value(100)), "large"));
        siddhiManager.definePartition(QueryFactory.createPartitionDefinition().name("testPartition").partitionBy(Expression.variable("StockStream", "symbol")).partitionBy(Condition.compare(Expression.variable("cseEventStream", "price"), Condition.Operator.GREATER_THAN_EQUAL, Expression.value(100)), "large"));
    }

    @Test
    public void testSingleDefinition2() {
        SiddhiManager siddhiManager = new SiddhiManager();
        PartitionDefinition partitionDefinition = QueryFactory.createPartitionDefinition();
        partitionDefinition.name("testPartition")
                .partitionBy(
                        Expression.variable("StockStream", "symbol"))
                .partitionBy(
                        Condition.compare(
                                Expression.variable("cseEventStream", "price"),
                                Condition.Operator.GREATER_THAN_EQUAL,
                                Expression.value(100)),
                        "large");
        siddhiManager.definePartition(partitionDefinition);

        siddhiManager.definePartition("define partition testPartition by StockStream.symbol,range cseEventStream.price>=100 as 'large' ;");
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testAddingTwoSameDefinition3() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.definePartition(QueryFactory.createPartitionDefinition().name("testPartition").partitionBy(Expression.variable("StockStream", "symbol")).partitionBy(Condition.compare(Expression.variable("cseEventStream", "price"), Condition.Operator.GREATER_THAN_EQUAL, Expression.value(100)), "large"));
        siddhiManager.definePartition("define partition testPartition by StockStream.symbols ,range cseEventStream.price>=100 as 'large'  ;");
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testAddingTwoSameDefinition4() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.definePartition("define partition testPartition by StockStream.symbol ,range cseEventStream.price>=100 as 'large' ;");
        siddhiManager.definePartition("define partition testPartition by StockStream.symbols ,range cseEventStream.price>=100 as 'large'  ;");
    }

    @Test
    public void testAddingTwoSameDefinition5() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.definePartition("define partition testPartition by StockStream.symbol ,range cseEventStream.price>=100 as 'large' ;");
        siddhiManager.definePartition("define partition testPartition by StockStream.symbol ,range cseEventStream.price>=100 as 'large'  ;");
    }

}
