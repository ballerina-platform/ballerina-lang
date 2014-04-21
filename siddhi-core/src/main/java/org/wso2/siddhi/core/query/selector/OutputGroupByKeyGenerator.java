/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.selector;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.util.parser.ExecutorParser;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.List;

public class OutputGroupByKeyGenerator {

    private ExpressionExecutor[] groupByExecutors = null;

    public OutputGroupByKeyGenerator(List<Variable> groupByList,
                                     List<QueryEventSource> queryEventSourceList,
                                     SiddhiContext siddhiContext) {

        if (groupByList.size() > 0) {
            groupByExecutors = new ExpressionExecutor[groupByList.size()];
            for (int i = 0, expressionsSize = groupByList.size(); i < expressionsSize; i++) {
                groupByExecutors[i] = ExecutorParser.parseExpression(groupByList.get(i), queryEventSourceList, null, false, siddhiContext);
            }
        }
    }

    protected String constructEventKey(AtomicEvent event) {
        if (groupByExecutors != null) {
            StringBuilder sb = new StringBuilder();
            for (ExpressionExecutor executor : groupByExecutors) {
                sb.append(executor.execute(event)).append("::");
            }
            return sb.toString();
        } else {
            return null;
        }
    }
}
