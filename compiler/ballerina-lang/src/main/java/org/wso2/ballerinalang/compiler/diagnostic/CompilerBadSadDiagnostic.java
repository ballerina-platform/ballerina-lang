/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.diagnostic;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;

import java.util.List;

/**
 * Represents a bad sad diagnostic from the ballerina compiler.
 *
 * @since 2.0.0
 */
public class CompilerBadSadDiagnostic extends Diagnostic {

    private final Location location;
    private final String msg;
    private final DiagnosticCode diagnosticCode = DiagnosticErrorCode.BAD_SAD_FROM_COMPILER;
    private final DiagnosticInfo diagnosticInfo = new DiagnosticInfo(diagnosticCode.diagnosticId(),
            diagnosticCode.messageKey(), diagnosticCode.severity());
    private final Object exception;

    public CompilerBadSadDiagnostic(Location location, Throwable throwable) {
        this.location = location;
        this.msg = "Compilation failed due to" + (throwable.getMessage() != null ? ": " + throwable.getMessage() :
                " an unhandled exception");
        this.exception = throwable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location location() {
        return location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiagnosticInfo diagnosticInfo() {
        return diagnosticInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String message() {
        return msg;
    }

    @Override
    public List<DiagnosticProperty<?>> properties() {
        return null;
    }


    public Object getException() {
        return exception;
    }

    public DiagnosticCode getCode() {
        return diagnosticCode;
    }

    @Override
    public String toString() {
        return String.format("error: %s", message());
    }
}
