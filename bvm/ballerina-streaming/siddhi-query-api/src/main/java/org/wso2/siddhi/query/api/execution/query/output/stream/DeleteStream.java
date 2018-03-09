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
package org.wso2.siddhi.query.api.execution.query.output.stream;

import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Query output stream deleting entry from table
 */
public class DeleteStream extends OutputStream {

    protected Expression onDeleteExpression;

    public DeleteStream(String tableId, OutputEventType outputEventType, Expression onDeleteExpression) {
        this.id = tableId;
        this.outputEventType = outputEventType;
        this.onDeleteExpression = onDeleteExpression;
    }

    public DeleteStream(String tableId, Expression onDeleteExpression) {
        this.id = tableId;
        this.outputEventType = OutputEventType.CURRENT_EVENTS;
        this.onDeleteExpression = onDeleteExpression;
    }

    public Expression getOnDeleteExpression() {
        return onDeleteExpression;
    }

    public void setOnDeleteExpression(Expression onDeleteExpression) {
        this.onDeleteExpression = onDeleteExpression;
    }

    @Override
    public String toString() {
        return "DeleteStream{" +
                "onDeleteExpression=" + onDeleteExpression +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeleteStream)) {
            return false;
        }

        DeleteStream that = (DeleteStream) o;

        if (onDeleteExpression != null ? !onDeleteExpression.equals(that.onDeleteExpression) : that
                .onDeleteExpression != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return onDeleteExpression != null ? onDeleteExpression.hashCode() : 0;
    }
}
