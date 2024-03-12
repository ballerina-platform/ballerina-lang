package io.ballerina.cli.diagnostics;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class DiagnosticAnnotationTest {

    @Test
    public void testColorAnnotations() {
        String errorColor = DiagnosticAnnotation.SEVERITY_COLORS.get(DiagnosticSeverity.ERROR);
        String warningColor = DiagnosticAnnotation.SEVERITY_COLORS.get(DiagnosticSeverity.WARNING);
        String hintColor = DiagnosticAnnotation.SEVERITY_COLORS.get(DiagnosticSeverity.HINT);
        String message = "Hello World!";

        String error_message = DiagnosticAnnotation.getColoredString(message, errorColor, true);
        String expected =
                DiagnosticAnnotation.JANSI_ANNOTATOR + "red" + " " + message + DiagnosticAnnotation.JANSI_RESET;
        Assert.assertEquals(error_message, expected);

        String warning_message = DiagnosticAnnotation.getColoredString(message, warningColor, true);
        expected = DiagnosticAnnotation.JANSI_ANNOTATOR + "yellow" + " " + message + DiagnosticAnnotation.JANSI_RESET;
        Assert.assertEquals(warning_message, expected);

        String hint_message = DiagnosticAnnotation.getColoredString(message, hintColor, true);
        expected = DiagnosticAnnotation.JANSI_ANNOTATOR + "blue" + " " + message + DiagnosticAnnotation.JANSI_RESET;
        Assert.assertEquals(hint_message, expected);
    }

    @Test
    public void testTruncation() {
        String message = "This is a long message that needs to be truncated";
        int importantPartStart = 15;
        int importantPartLength = 8;
        Map<Integer, String> truncatedLines = new HashMap<>();
        truncatedLines.put(30, "This is a long message that...");
        truncatedLines.put(25, "... a long message tha...");
        truncatedLines.put(20, "... long message ...");

        int[] lengths = {30, 25, 20};
        for (int length : lengths) {
            DiagnosticAnnotation.TruncateResult truncatedMessage =
                    DiagnosticAnnotation.truncate(message, length, importantPartStart, importantPartLength);
            String expected = truncatedLines.get(length);
            Assert.assertEquals(truncatedMessage.line(), expected);
        }
    }

}
