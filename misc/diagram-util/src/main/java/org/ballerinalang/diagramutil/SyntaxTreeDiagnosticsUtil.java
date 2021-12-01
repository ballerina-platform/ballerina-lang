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
package org.ballerinalang.diagramutil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.Location;

/**
 * This is the SyntaxTreeDiagnosticsUtil class for diagnostics related utils used in the syntax tree generation.
 *
 * @since 2.0.0
 */

public class SyntaxTreeDiagnosticsUtil {
    public static JsonArray getDiagnostics(Iterable<Diagnostic> diagnostics) {
        JsonArray diagnosticsArray = new JsonArray();
        for (Diagnostic diagnostic : diagnostics) {
            JsonObject diagnosticJson = new JsonObject();
            diagnosticJson.addProperty("message", diagnostic.message());
            diagnosticJson.add("range" , getLocation(diagnostic.location()));
            DiagnosticInfo diagnosticInfo = diagnostic.diagnosticInfo();
            if (diagnosticInfo != null) {
                JsonObject diagnosticInfoJson = new JsonObject();
                diagnosticInfoJson.addProperty("code", diagnosticInfo.code());
                diagnosticInfoJson.addProperty("severity", diagnosticInfo.severity().name());
                diagnosticJson.add("diagnosticInfo", diagnosticInfoJson);
            }
            diagnosticsArray.add(diagnosticJson);
        }

        return diagnosticsArray;
    }

    public static JsonObject getLocation(Location location) {
        JsonObject jsonLocation = new JsonObject();
        jsonLocation.addProperty("startLine", location.lineRange().startLine().line());
        jsonLocation.addProperty("endLine", location.lineRange().endLine().line());
        jsonLocation.addProperty("startColumn", location.textRange().startOffset());
        jsonLocation.addProperty("endColumn", location.textRange().endOffset());
        return jsonLocation;
    }
}
