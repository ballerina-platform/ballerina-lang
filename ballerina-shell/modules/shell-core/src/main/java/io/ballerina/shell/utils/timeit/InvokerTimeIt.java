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

package io.ballerina.shell.utils.timeit;

import io.ballerina.shell.DiagnosticReporter;
import io.ballerina.shell.exceptions.BallerinaShellException;
import io.ballerina.shell.exceptions.InvokerException;

/**
 * Utility calls for performance measurements.
 *
 * @since 2.0.0
 */
public class InvokerTimeIt {
    /**
     * Times the operation and sends a debug message. Statistics will also be calculated.
     * This just extends the functionality of {@code TimeIt} to be used for invoker operations.
     *
     * @param category  Category of the entry.
     * @param owner     Diagnostic owner. The time debug data would be emitted to this class.
     * @param operation Operation callback to run.
     * @throws InvokerException If the operation failed.
     */
    public static <T> T timeIt(String category, DiagnosticReporter owner,
                               TimedOperation<T> operation) throws InvokerException {
        try {
            return TimeIt.timeIt(category, owner, operation);
        } catch (InvokerException e) {
            throw e;
        } catch (BallerinaShellException e) {
            throw new RuntimeException(e);
        }
    }
}
