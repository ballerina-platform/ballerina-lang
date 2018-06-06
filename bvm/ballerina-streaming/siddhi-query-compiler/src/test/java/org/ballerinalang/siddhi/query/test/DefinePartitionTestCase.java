/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.query.test;

import org.ballerinalang.siddhi.query.api.execution.partition.Partition;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;
import org.ballerinalang.siddhi.query.compiler.SiddhiCompiler;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Partition testing testcase.
 */
public class DefinePartitionTestCase {
    @Test
    public void test1() {
        Partition partition = SiddhiCompiler.parsePartition("partition with (200>volume as 'LessValue' or 200<=volume" +
                " as 'HighValue' of cseEventStream) begin from cseEventStream select sum(volume) as sumvolume insert " +
                "into StockStream ;  end ");

        AssertJUnit.assertEquals(Partition.partition().
                        with("cseEventStream",
                                Partition.range("LessValue",
                                        Expression.compare(
                                                Expression.value(200),
                                                Compare.Operator.GREATER_THAN,
                                                Expression.variable("volume"))
                                ),
                                Partition.range("HighValue",
                                        Expression.compare(
                                                Expression.value(200),
                                                Compare.Operator.LESS_THAN_EQUAL,
                                                Expression.variable("volume"))
                                )).toString().split("queryList")[0],
                partition.toString().split("queryList")[0]);
    }

    @Test
    public void test2() {
        Partition partition = SiddhiCompiler.parsePartition("partition with (symbol of cseEventStream) begin from " +
                "cseEventStream select sum(volume) as sumvolume insert into StockStream ;  end ");

        AssertJUnit.assertEquals(Partition.partition().
                        with("cseEventStream", Expression.variable("symbol")).toString().split("queryList")[0],
                partition.toString().split("queryList")[0]);
    }
}
