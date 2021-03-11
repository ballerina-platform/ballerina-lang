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
package io.ballerina.tools.diagnostics;

import java.util.Collections;
import java.util.List;

/**
 * This class consists of static methods that can be used to create {@code Diagnostic} instances.
 *
 * @since 2.0.0
 */
public class DiagnosticFactory {

    private DiagnosticFactory() {
    }

    /**
     * Creates a {@code Diagnostic} instances from the given details.
     *
     * @param diagnosticInfo static diagnostic information
     * @param location       the location of the diagnostic
     * @param args           Arguments to diagnostic message format
     * @return a {@code Diagnostic} instance
     */
    public static Diagnostic createDiagnostic(DiagnosticInfo diagnosticInfo,
                                              Location location,
                                              Object... args) {
        return new DefaultDiagnostic(diagnosticInfo, location, Collections.emptyList(), args);
    }

    /**
     * Creates a {@code Diagnostic} instances from the given details.
     *
     * @param diagnosticInfo static diagnostic information
     * @param location       the location of the diagnostic
     * @param properties     adding properties associated with the diagnostic
     * @param args           Arguments to diagnostic message format
     * @return a {@code Diagnostic} instance
     */
    public static Diagnostic createDiagnostic(DiagnosticInfo diagnosticInfo,
                                              Location location,
                                              List<DiagnosticProperty<?>> properties,
                                              Object... args) {
        return new DefaultDiagnostic(diagnosticInfo, location, properties, args);
    }
}
