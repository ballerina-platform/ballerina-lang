/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.interpreter;

import org.wso2.ballerina.core.interpreter.nonblocking.BLangExecutionVisitor;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code ExecutableMemLocation} interface makes an {@link org.wso2.ballerina.core.interpreter.MemoryLocation}
 * accessible in BLangExecutionVisitor.
 *
 * @since 0.8.0
 */
public interface ExecutableMemLocation {

    /**
     * Executes and Returns the result of this expression.
     *
     * @param executor instance of a {@code {@link BLangExecutionVisitor }}
     * @return result of the expression
     */
    BValue access(BLangExecutionVisitor executor);
}
