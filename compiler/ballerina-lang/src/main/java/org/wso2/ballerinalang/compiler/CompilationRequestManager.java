/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler;

import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.StaleCompilationRequestException;

/**
 * This class is responsible for collecting compilation requests that are coming in from various sources and ensuring
 * that the latest request is maintained.
 *
 * @since 2.0.0
 */
public class CompilationRequestManager {

    private static final CompilerContext.Key<CompilationRequestManager> COMPILATION_REQUEST_MANAGER_KEY =
            new CompilerContext.Key<>();

    private CompilationRequest currentCompileRequest;

    private CompilationRequestManager(CompilerContext context) {
        context.put(COMPILATION_REQUEST_MANAGER_KEY, this);
    }

    public static synchronized CompilationRequestManager getInstance(CompilerContext context) {
        CompilationRequestManager compilationRequestManager = context.get(COMPILATION_REQUEST_MANAGER_KEY);
        if (compilationRequestManager == null) {
            compilationRequestManager = new CompilationRequestManager(context);
        }
        return compilationRequestManager;
    }

    /**
     * This method accepts a {@link CompilationRequest} instance and compares it against the currently held compilation
     * request. If this new request is more recent than the currently held request, it will discard the current request
     * and set this new request as the current request. The provided request must not be null.
     *
     * @param request A request to perform a compilation
     */
    public synchronized void submit(CompilationRequest request) {
        if (currentCompileRequest == null || currentCompileRequest.getTimestamp().isBefore(request.getTimestamp())) {
            currentCompileRequest = request;
        }
    }

    /**
     * Given a compilation request, this method will check if it is still valid. If there is a more recent request, this
     * will throw a {@link StaleCompilationRequestException} to signal to the one initiating this compilation that it
     * should abort this compilation.
     *
     * @param request The request to be checked if it was cancelled
     */
    public synchronized void throwIfCancelled(CompilationRequest request) {
        if (!this.currentCompileRequest.equals(request)) {
            throw new StaleCompilationRequestException(currentCompileRequest, request);
        }
    }
}
