/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.core.model.statements;

import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.NodeVisitor;

/**
 * {@code TryCatchStmt} represents a try/catch statement.
 *
 * @since 0.8.0
 */
public class TryCatchStmt extends AbstractStatement {
    private Statement tryBlock;
//    private List<ExceptionType> catchExceptions;
    private Statement catchBlock;

    public TryCatchStmt(NodeLocation location) {
        super(location);
    }

//    public TryCatchStmt(Statement tryBlock, List<ExceptionType> catchExceptions, Statement catchBlock) {
//        this.tryBlock = tryBlock;
//        this.catchExceptions = catchExceptions;
//        this.catchBlock = catchBlock;
//    }

    @Override
    public void accept(NodeVisitor visitor) {
//        visitor.accept(this);
    }

    @Override
    public void execute(NodeExecutor executor) {

    }
}
