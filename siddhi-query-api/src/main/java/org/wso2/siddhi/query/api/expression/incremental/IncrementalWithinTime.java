/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.query.api.expression.incremental;

import org.wso2.siddhi.query.api.aggregation.Within;
import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Incremental within time expression
 */
public class IncrementalWithinTime extends Expression {

    private static final long serialVersionUID = 1L;

    private Within within;
    private Expression timeExpression;

    public IncrementalWithinTime(Within within, Expression timeExpression) {
        this.within = within;
        this.timeExpression = timeExpression;
    }

    public Within getWithin() {
        return this.within;
    }

    public Expression getTimeExpression() {
        return this.timeExpression;
    }

    @Override
    public String toString() {
        return "IncrementalTime{" + "within=" + within + ", " + "timeExpression=" + timeExpression + ", " + "} ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IncrementalWithinTime that = (IncrementalWithinTime) o;

        if (within != null ? !within.equals(that.within) : that.within != null) {
            return false;
        }

        if (timeExpression != null ? !timeExpression.equals(that.timeExpression) : that.timeExpression != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = within != null ? within.hashCode() : 0;
        result = 31 * result + (timeExpression != null ? timeExpression.hashCode() : 0);
        return result;
    }
}
