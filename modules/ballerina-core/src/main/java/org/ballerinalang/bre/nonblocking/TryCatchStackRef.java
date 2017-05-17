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
package org.ballerinalang.bre.nonblocking;

import org.ballerinalang.bre.StackFrame;
import org.ballerinalang.model.statements.TryCatchStmt;

/**
 * Stack Reference to CatchBlocks and their stack.
 */
public class TryCatchStackRef {

    public TryCatchStmt tryCatchStmt;
    public StackFrame stackFrame;

    public TryCatchStackRef(TryCatchStmt tryCatchStmt, StackFrame stackFrame) {
        this.tryCatchStmt = tryCatchStmt;
        this.stackFrame = stackFrame;
    }

    public TryCatchStmt getTryCatchStmt() {
        return tryCatchStmt;
    }

    public void setTryCatchStmt(TryCatchStmt tryCatchStmt) {
        this.tryCatchStmt = tryCatchStmt;
    }

    public StackFrame getStackFrame() {
        return stackFrame;
    }

    public void setStackFrame(StackFrame stackFrame) {
        this.stackFrame = stackFrame;
    }
}
