package build.tool.runner.diagnostics;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.util.List;

public class ToolDiagnostic extends Diagnostic {
    private final DiagnosticInfo diagnosticInfo;
    private final Location location;
    private final String message;

    public ToolDiagnostic(DiagnosticInfo diagnosticInfo, String message) {
        this.location = new NullLocation();
        this.diagnosticInfo = diagnosticInfo;
        this.message = message;
    }

    @Override
    public Location location() {
        return location;
    }

    @Override
    public DiagnosticInfo diagnosticInfo() {
        return diagnosticInfo;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public List<DiagnosticProperty<?>> properties() {
        return null;
    }

    private static class NullLocation implements Location {

        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from("openAPI sample build tool", from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }
}
