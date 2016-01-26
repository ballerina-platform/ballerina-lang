/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.execution.query.output.stream;

import org.wso2.siddhi.query.api.expression.Expression;

public class InsertOverwriteStream extends OutputStream {
    protected Expression onOverwriteExpression;

    public InsertOverwriteStream(String tableId, OutputEventType outputEventType, Expression onOverwriteExpression) {
        this.id = tableId;
        this.outputEventType = outputEventType;
        this.onOverwriteExpression = onOverwriteExpression;
    }

    public InsertOverwriteStream(String tableId, Expression onOverwriteExpression) {
        this.id = tableId;
        this.outputEventType = OutputEventType.CURRENT_EVENTS;
        this.onOverwriteExpression = onOverwriteExpression;
    }

    public void setOnOverwriteExpression(Expression onOverwriteExpression) {
        this.onOverwriteExpression = onOverwriteExpression;
    }

    public Expression getOnOverwriteExpression() {
        return onOverwriteExpression;
    }

    @Override
    public String toString() {
        return "UpdateStream{" +
                "onOverwriteExpression=" + onOverwriteExpression +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InsertOverwriteStream)) return false;
        if (!super.equals(o)) return false;

        InsertOverwriteStream that = (InsertOverwriteStream) o;

        if (onOverwriteExpression != null ? !onOverwriteExpression.equals(that.onOverwriteExpression) : that.onOverwriteExpression != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (onOverwriteExpression != null ? onOverwriteExpression.hashCode() : 0);
        return result;
    }
}
