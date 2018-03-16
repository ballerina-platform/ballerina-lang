/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.model.tree.statements;

/**
 * The interface with the APIs to implement the "Query" in ballerina streams SQLish syntax.
 * <pre>Grammar:
 *     QUERY Identifier LEFT_BRACE streamingQueryStatement RIGHT_BRACE
 *
 * E.g.
 * query q1{
 *          from testStream
 *          where x > 50
 *          select 10 as mo
 *          group by mo
 *          insert into test1
 *      }
 * </pre>
 *
 * @since 0.965.0
 */

public interface QueryStatementNode extends StatementNode {

    void setIdentifier(String identifier);

    void setStreamingQueryStatement(StreamingQueryStatementNode streamingQueryStatement);

    String getIdentifier();

    StreamingQueryStatementNode getStreamingQueryStatement();
}
