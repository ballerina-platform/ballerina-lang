package org.ballerinalang.testerina.core;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.values.BString;

import java.util.Arrays;
import java.util.List;

/**
 * Evaluates assertion values to find the difference inline.
 */
public class AssertionDiffEvaluator {

    private static final int MAX_ARG_LENGTH = 80;

    public static BString getStringDiff(BString expected, BString actual) {
        List<String> difference = null;
        String output = "";
        try {
            Patch<String> patch = DiffUtils.diff(Arrays.asList(expected.toString().split("(?!^)")),
                    Arrays.asList(actual.toString().split("(?!^)")));
            difference = UnifiedDiffUtils.generateUnifiedDiff("expected", "actual",
                    Arrays.asList(expected.toString().split("(?!^)")), patch, MAX_ARG_LENGTH);
        } catch (DiffException e) {
            output = "Warning: Could not generate diff.";
        }
        if (difference != null) {
            for (String line : difference) {
                if (line.startsWith("+") || line.startsWith("-")) {
                    if (output.endsWith("\n")) {
                        output = output.concat(line + "\n");
                    } else {
                        output = output.concat("\n" + line + "\n");
                    }
                } else if (line.startsWith("@@ -")) {
                    output = output.concat(line + "\n\n");
                } else {
                    output = output.concat(line.replaceFirst(" ", ""));
                }
            }
        }
        return BStringUtils.fromString(output);
    }
}

