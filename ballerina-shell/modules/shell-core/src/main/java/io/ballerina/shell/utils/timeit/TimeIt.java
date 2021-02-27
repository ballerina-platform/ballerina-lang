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

import io.ballerina.shell.Diagnostic;
import io.ballerina.shell.DiagnosticReporter;
import io.ballerina.shell.exceptions.BallerinaShellException;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

/**
 * Utility calls for performance measurements.
 *
 * @since 2.0.0
 */
public class TimeIt {

    private static final HashMap<String, OperationTimeEntry> operationTimes = new HashMap<>();

    /**
     * Times the operation and sends a debug message.
     * Statistics will also be calculated.
     *
     * @param category  Category of the entry.
     * @param owner     Diagnostic owner. The time debug data would be emitted to this class.
     * @param operation Operation callback to run.
     * @throws BallerinaShellException If the operation failed.
     */
    public static <T> T timeIt(String category, DiagnosticReporter owner,
                               TimedOperation<T> operation) throws BallerinaShellException {
        Instant start = Instant.now();
        T result = operation.run();
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        OperationTimeEntry entry = operationTimes.getOrDefault(category, new OperationTimeEntry());
        entry.addDuration(duration);
        Duration mean = entry.mean();
        operationTimes.put(category, entry);

        String message = String.format("Task %s took %s ms. Average is %s ms.",
                category, duration.toMillis(), mean.toMillis());
        owner.addDiagnostic(Diagnostic.debug(message));

        return result;
    }
}
