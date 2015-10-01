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
package org.wso2.siddhi.query.api.execution.query.input.stream;


import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.output.stream.ReturnStream;

import java.util.UUID;

public class AnonymousInputStream extends SingleInputStream {
    private Query query;

    public AnonymousInputStream(Query query) {
        super("Anonymous-" + UUID.randomUUID());
        if (query.getOutputStream() != null && !(query.getOutputStream() instanceof ReturnStream)) {
            throw new ExecutionPlanValidationException("OutputStream of the query is not on type Return!");
        }
        this.query = query;

    }

    public Query getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return "AnonymousInputStream{" +
                "query=" + query +
                "} ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnonymousInputStream)) return false;

        AnonymousInputStream that = (AnonymousInputStream) o;

        if (query != null ? !query.equals(that.query) : that.query != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return query != null ? query.hashCode() : 0;
    }
}
