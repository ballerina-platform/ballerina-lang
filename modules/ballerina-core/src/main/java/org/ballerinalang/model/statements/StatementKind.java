/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.model.statements;

/**
 * Represents Ballerina Statement Kinds.
 *
 * @since 0.89
 */
public enum StatementKind {

    COMMENT(1),
    ASSIGN(2),
    VARIABLE_DEF(3),
    FUNCTION_INVOCATION(4),
    ACTION_INVOCATION(5),
    RETURN(6),
    REPLY(7),
    IF_ELSE(8),
    WHILE(9),
    BREAK(10),
    CONTINUE(11),
    TRY_CATCH(12),
    THROW(13),
    FORK_JOIN(14),
    TRANSACTION(15),
    ABORT(16),
    WORKER_INVOCATION(17),
    WORKER_REPLY(18),
    TRANSFORM(19),

    // Blocks
    CALLABLE_UNIT_BLOCK(30),
    TRANSACTION_BLOCK(31),
    COMMITTED_BLOCK(32),
    ABORTED_BLOCK(33),
    WHILE_BLOCK(34),
    THEN_BLOCK(35),
    ELSE_IF_BLOCK(36),
    ELSE_BLOCK(37),
    TRY_BLOCK(38),
    CATCH_BLOCK(39),
    FINALLY_BLOCK(40),
    JOIN_BLOCK(41),
    TIMEOUT_BLOCK(42),
    TRANSFORM_BLOCK(43),

    ;
    private final int key;

    StatementKind(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
