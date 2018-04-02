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

package org.ballerinalang.model.tree.clauses;

import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.expressions.LambdaFunctionNode;

/**
 * The interface with the APIs to implement the straming action in ballerina streams/table SQLish syntax.
 * <pre>Grammar:
 *     EQUAL_GT LEFT_PARENTHESIS formalParameterList? RIGHT_PARENTHESIS callableUnitBody
 *
 * E.g.
 *      from teacherStream where age &gt; 18 window lengthBatch(3) select status, count(status) as totalCount
 *      group by status having totalCount &gt; 1 =&gt; ( Teacher[] teachers) {
 *          //Do something with Teacher[] array (Streaming action)
 *      };
 * </pre>
 *
 * @since 0.965.0
 */

public interface StreamActionNode extends Node {

    void setInvokableBody(LambdaFunctionNode lambdaFunction);

    LambdaFunctionNode getInvokableBody();
}
