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
package io.ballerina.tools.diagnostics;

import java.util.List;

/**
 * A diagnostic represents a compiler error, a warning or a message at a specific location in the source file.
 *
 * @since 2.0.0
 */
public abstract class Diagnostic {

    /**
     * Returns the location of the diagnostic.
     *
     * @return {@link Location} of the diagnostic
     */
    public abstract Location location();

    /**
     * Returns the abstract shape of the diagnostic that independent of the location and message arguments.
     *
     * @return the abstract shape of the {@link Diagnostic}
     */
    public abstract DiagnosticInfo diagnosticInfo();

    /**
     * Returns the diagnostic message.
     *
     * @return diagnostic message
     */
    public abstract String message();

    /**
     * Returns the associated additional properties.
     *
     * @return {@link DiagnosticProperty} of the diagnostic
     */
    public abstract List<DiagnosticProperty<?>> properties();

    @Override
    public String toString() {
        String location;
        if (location().lineRange().fileName().isEmpty()) {
            location = "";
        } else {
            location = " [" +
                    location().lineRange().fileName() + ":" + location().lineRange() + "]";
        }
        return diagnosticInfo().severity().toString() + location + " " + message();
    }
}
