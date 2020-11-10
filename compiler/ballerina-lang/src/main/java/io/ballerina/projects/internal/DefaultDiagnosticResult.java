/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.internal;

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.Collection;

/**
 * The default implementaion of the {@code DiagnosticResult}.
 * <p>
 * Having this class in the internal package, restricts api users from creating
 * new instances of {@code DiagnosticResult}.
 *
 * @since 2.0.0
 */
public class DefaultDiagnosticResult extends DiagnosticResult {

    public DefaultDiagnosticResult(Collection<Diagnostic> allDiagnostics) {
        super(allDiagnostics);
    }
}
