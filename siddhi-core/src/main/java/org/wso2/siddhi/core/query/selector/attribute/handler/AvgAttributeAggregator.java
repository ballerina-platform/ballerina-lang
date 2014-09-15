/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.selector.attribute.handler;

import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.query.api.definition.Attribute;

public class AvgAttributeAggregator implements AttributeAggregator {

    private Attribute.Type type;
    private AvgAttributeAggregator avgOutputAttributeAggregator;

    public void init(Attribute.Type type){
        this.type =type;
        switch (type){
            case FLOAT:
                avgOutputAttributeAggregator = new AvgAttributeAggregatorFloat();
                break;
            case INT:
                avgOutputAttributeAggregator = new AvgAttributeAggregatorInt();
                break;
            case LONG:
                avgOutputAttributeAggregator = new AvgAttributeAggregatorLong();
                break;
            case DOUBLE:
                avgOutputAttributeAggregator = new AvgAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("Avg not supported for " + type);
        }

    }

    public Attribute.Type getReturnType() {
        return avgOutputAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object obj) {
        return avgOutputAttributeAggregator.processAdd(obj);
    }

    @Override
    public Object processRemove(Object obj) {
        return avgOutputAttributeAggregator.processRemove(obj);
    }

    @Override
    public AttributeAggregator newInstance() {
        return avgOutputAttributeAggregator.newInstance();
    }

    @Override
    public void destroy() {

    }

    class AvgAttributeAggregatorDouble extends AvgAttributeAggregator {

        private double value = 0.0;
        private long count=0;
        private final Attribute.Type type = Attribute.Type.DOUBLE;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object obj) {
            count++;
            value += (Double) obj;
            if (count == 0) {
                return 0;
            }
            return value / count;
        }

        @Override
        public Object processRemove(Object obj) {
            count--;
            value -= (Double) obj;
            if (count == 0) {
                return 0;
            }
            return value / count;
        }

        @Override
        public AttributeAggregator newInstance() {
            return new AvgAttributeAggregatorDouble();
        }

    }

    class AvgAttributeAggregatorFloat extends AvgAttributeAggregator {

        private double value = 0.0;
        private long count=0;
        private final Attribute.Type type = Attribute.Type.DOUBLE;


        public Attribute.Type getReturnType() {
            return this.type;
        }

        @Override
        public Object processAdd(Object obj) {
            count++;
            value += (Float) obj;
            if(count==0){
                return 0;
            }
            return value/count;
        }

        @Override
        public Object processRemove(Object obj) {
            count--;
            value -= (Float) obj;
            if(count==0){
                return 0;
            }
            return value/count;
        }

        @Override
        public AttributeAggregator newInstance() {
            return new AvgAttributeAggregatorFloat();
        }


    }

    class AvgAttributeAggregatorInt extends AvgAttributeAggregator {

        private double value = 0.0;
        private long count = 0;
        private final Attribute.Type type = Attribute.Type.DOUBLE;

        public Attribute.Type getReturnType() {
            return this.type;
        }

        @Override
        public Object processAdd(Object obj) {
            count++;
            value += (Integer) obj;
            if (count == 0) {
                return 0;
            }
            return value / count;
        }

        @Override
        public Object processRemove(Object obj) {
            count--;
            value -= (Integer) obj;
            if (count == 0) {
                return 0;
            }
            return value / count;
        }

        @Override
        public AttributeAggregator newInstance() {
            return new AvgAttributeAggregatorInt();
        }
    }

    class AvgAttributeAggregatorLong extends AvgAttributeAggregator {

        private double value = 0.0;
        private long count=0;
        private final Attribute.Type type = Attribute.Type.DOUBLE;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object obj) {
            count++;
            value += (Long) obj;
            if(count==0){
                return 0;
            }
            return value/count;
        }

        @Override
        public Object processRemove(Object obj) {
            count--;
            value -= (Long) obj;
            if(count==0){
                return 0;
            }
            return value/count;
        }

        @Override
        public AttributeAggregator newInstance() {
            return new AvgAttributeAggregatorLong();
        }

    }


}
