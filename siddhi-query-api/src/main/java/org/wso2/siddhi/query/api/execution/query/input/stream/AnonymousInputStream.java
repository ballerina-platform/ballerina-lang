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
package org.wso2.siddhi.query.api.execution.query.input.stream;


import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.output.stream.ReturnStream;

import java.util.UUID;

/**
 * Anonymous input query stream
 */
public class AnonymousInputStream extends SingleInputStream {
    private Query query;

    public AnonymousInputStream(Query query) {
        super("Anonymous-" + UUID.randomUUID());
        if (query.getOutputStream() != null && !(query.getOutputStream() instanceof ReturnStream)) {
            throw new SiddhiAppValidationException("OutputStream of the query is not on type Return!");
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AnonymousInputStream that = (AnonymousInputStream) o;

        return (query != null ? query.equals(that.query) : that.query == null) || super.equals(o);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (query != null ? query.hashCode() : 0);
        return result;
    }
}
