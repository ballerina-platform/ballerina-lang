package org.ballerinalang.diagramutil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;

public class DiagnosticUtil {
    public static JsonArray getDiagnostics(Iterable<Diagnostic> diagnostics){
        JsonArray diagnosticsArray = new JsonArray();
        for (Diagnostic diagnostic : diagnostics) {
            JsonObject diagnosticJson = new JsonObject();
            diagnosticJson.addProperty("message", diagnostic.message());
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
}
