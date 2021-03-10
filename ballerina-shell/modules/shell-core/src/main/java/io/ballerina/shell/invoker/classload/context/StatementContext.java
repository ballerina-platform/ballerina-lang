/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package io.ballerina.shell.invoker.classload.context;

/**
 * A context representing a statement/expression.
 *
 * @since 2.0.0
 */
public class StatementContext {
    private final String code;
    private final boolean statement;

    /**
     * Creates a statement/expression context.
     *
     * @param snippet   Snippet content.
     * @param statement Whether this is a statement.
     */
    public StatementContext(String snippet, boolean statement) {
        this.code = snippet;
        this.statement = statement;
    }

    public String code() {
        return code;
    }

    public boolean statement() {
        return statement;
    }
}
