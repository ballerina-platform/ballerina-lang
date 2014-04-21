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
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.query.selector.attribute.handler.OutputAttributeAggregator;
import org.wso2.siddhi.core.query.selector.attribute.handler.avg.AvgOutputAttributeAggregatorDouble;
import org.wso2.siddhi.core.query.selector.attribute.handler.avg.AvgOutputAttributeAggregatorFloat;
import org.wso2.siddhi.core.query.selector.attribute.handler.avg.AvgOutputAttributeAggregatorInt;
import org.wso2.siddhi.core.query.selector.attribute.handler.avg.AvgOutputAttributeAggregatorLong;
import org.wso2.siddhi.core.query.selector.attribute.handler.count.CountOutputAttributeAggregator;
import org.wso2.siddhi.core.query.selector.attribute.handler.max.MaxOutputAttributeAggregatorDouble;
import org.wso2.siddhi.core.query.selector.attribute.handler.max.MaxOutputAttributeAggregatorFloat;
import org.wso2.siddhi.core.query.selector.attribute.handler.max.MaxOutputAttributeAggregatorInt;
import org.wso2.siddhi.core.query.selector.attribute.handler.max.MaxOutputAttributeAggregatorLong;
import org.wso2.siddhi.core.query.selector.attribute.handler.min.MinOutputAttributeAggregatorDouble;
import org.wso2.siddhi.core.query.selector.attribute.handler.min.MinOutputAttributeAggregatorFloat;
import org.wso2.siddhi.core.query.selector.attribute.handler.min.MinOutputAttributeAggregatorInt;
import org.wso2.siddhi.core.query.selector.attribute.handler.min.MinOutputAttributeAggregatorLong;
import org.wso2.siddhi.core.query.selector.attribute.handler.sum.SumOutputAttributeAggregatorDouble;
import org.wso2.siddhi.core.query.selector.attribute.handler.sum.SumOutputAttributeAggregatorFloat;
import org.wso2.siddhi.core.query.selector.attribute.handler.sum.SumOutputAttributeAggregatorInt;
import org.wso2.siddhi.core.query.selector.attribute.handler.sum.SumOutputAttributeAggregatorLong;
import org.wso2.siddhi.query.api.definition.Attribute;

public class OutputAttributeHandlerParser {


    public static OutputAttributeAggregator createSumAggregator(Attribute.Type[] types) {
        if (types.length > 1) {
            throw new QueryCreationException("Sum can only have one parameter");
        }
        Attribute.Type type = types[0];
        switch (type) {
            case STRING:
                throw new OperationNotSupportedException("Sum not supported for string");
            case INT:
                return new SumOutputAttributeAggregatorInt();
            case LONG:
                return new SumOutputAttributeAggregatorLong();
            case FLOAT:
                return new SumOutputAttributeAggregatorFloat();
            case DOUBLE:
                return new SumOutputAttributeAggregatorDouble();
            case BOOL:
                throw new OperationNotSupportedException("Sum not supported for bool");
        }
        throw new OperationNotSupportedException("Sum not supported for " + type);
    }

    public static OutputAttributeAggregator createAvgAggregator(Attribute.Type[] types) {
        if (types.length > 1) {
            throw new QueryCreationException("Avg can only have one parameter");
        }
        Attribute.Type type = types[0];
        switch (type) {
            case STRING:
                throw new OperationNotSupportedException("Avg not supported for string");
            case INT:
                return new AvgOutputAttributeAggregatorInt();
            case LONG:
                return new AvgOutputAttributeAggregatorLong();
            case FLOAT:
                return new AvgOutputAttributeAggregatorFloat();
            case DOUBLE:
                return new AvgOutputAttributeAggregatorDouble();
            case BOOL:
                throw new OperationNotSupportedException("Avg not supported for bool");
        }
        throw new OperationNotSupportedException("Avg not supported for " + type);
    }

    public static OutputAttributeAggregator createMaxAggregator(Attribute.Type[] types) {
        if (types.length > 1) {
            throw new QueryCreationException("Max can only have one parameter");
        }
        Attribute.Type type = types[0];
        switch (type) {
            case STRING:
                throw new OperationNotSupportedException("Max not supported for string");
            case INT:
                return new MaxOutputAttributeAggregatorInt();
            case LONG:
                return new MaxOutputAttributeAggregatorLong();
            case FLOAT:
                return new MaxOutputAttributeAggregatorFloat();
            case DOUBLE:
                return new MaxOutputAttributeAggregatorDouble();
            case BOOL:
                throw new OperationNotSupportedException("Max not supported for bool");
        }
        throw new OperationNotSupportedException("Max not supported for " + type);
    }


    public static OutputAttributeAggregator createMinAggregator(Attribute.Type[] types) {
        if (types.length > 1) {
            throw new QueryCreationException("Min can only have one parameter");
        }
        Attribute.Type type = types[0];
        switch (type) {
            case STRING:
                throw new OperationNotSupportedException("Min not supported for string");
            case INT:
                return new MinOutputAttributeAggregatorInt();
            case LONG:
                return new MinOutputAttributeAggregatorLong();
            case FLOAT:
                return new MinOutputAttributeAggregatorFloat();
            case DOUBLE:
                return new MinOutputAttributeAggregatorDouble();
            case BOOL:
                throw new OperationNotSupportedException("Min not supported for bool");
        }
        throw new OperationNotSupportedException("Min not supported for " + type);
    }

    public static OutputAttributeAggregator createCountAggregator(Attribute.Type[] types) {
        if (types.length > 1) {
            throw new QueryCreationException("Avg can only have one parameter");
        }
        return new CountOutputAttributeAggregator();
    }

}
