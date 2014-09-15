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

public class SumAttributeAggregator implements AttributeAggregator {

    private Attribute.Type type;
    private SumAttributeAggregator sumOutputAttributeAggregator;

    public void init(Attribute.Type type){
        this.type =type;
        switch (type){
            case FLOAT:
                sumOutputAttributeAggregator = new SumAttributeAggregatorFloat();
                break;
            case INT:
                sumOutputAttributeAggregator = new SumAttributeAggregatorInt();
                break;
            case LONG:
                sumOutputAttributeAggregator = new SumAttributeAggregatorLong();
                break;
            case DOUBLE:
                sumOutputAttributeAggregator = new SumAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("Sum not supported for " + type);
        }

    }
    public Attribute.Type getReturnType() {
        return sumOutputAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object obj) {
        return sumOutputAttributeAggregator.processAdd(obj);
    }

    @Override
    public Object processRemove(Object obj) {
        return sumOutputAttributeAggregator.processRemove(obj);
    }

    @Override
    public AttributeAggregator newInstance() {
        return sumOutputAttributeAggregator.newInstance();
    }

    @Override
    public void destroy() {

    }

    class SumAttributeAggregatorDouble extends SumAttributeAggregator {

        private double value = 0.0;
        private final Attribute.Type type = Attribute.Type.DOUBLE;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object obj) {
            value += (Double) obj;
            return value;
        }

        @Override
        public Object processRemove(Object obj) {
            value -= (Double) obj;
            return value;
        }

        @Override
        public AttributeAggregator newInstance() {
            return new SumAttributeAggregatorDouble();
        }

    }

    class SumAttributeAggregatorFloat extends SumAttributeAggregator {

        private double value = 0.0;
        private final Attribute.Type type = Attribute.Type.DOUBLE;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object obj) {
            value += ((Float) obj).doubleValue();
            return value;
        }

        @Override
        public Object processRemove(Object obj) {
            value -= ((Float) obj).doubleValue();
            return value;
        }

        @Override
        public AttributeAggregator newInstance() {
            return new SumAttributeAggregatorFloat();
        }

    }

    class SumAttributeAggregatorInt extends SumAttributeAggregator {

        private long value = 0L;
        private final Attribute.Type type = Attribute.Type.LONG;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object obj) {
            value += ((Integer) obj).longValue();
            return value;
        }

        @Override
        public Object processRemove(Object obj) {
            value -= ((Integer) obj).longValue();
            return value;
        }

        @Override
        public AttributeAggregator newInstance() {
            return new SumAttributeAggregatorInt();
        }

    }

    class SumAttributeAggregatorLong extends SumAttributeAggregator {

        private long value = 0L;
        private final Attribute.Type type = Attribute.Type.LONG;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object obj) {
            value += (Long) obj;
            return value;
        }

        @Override
        public Object processRemove(Object obj) {
            value -= (Long) obj;
            return value;
        }

        @Override
        public AttributeAggregator newInstance() {
            return new SumAttributeAggregatorLong();
        }

    }

}
