package org.ballerinalang.testerina.core;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.values.BString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Evaluates assertion values to find the difference inline.
 */
public class AssertionDiffEvaluator {

    private static final String PARTITION_REGEX = "(?<=\\G.{80})";
    private static final int MAX_ARG_LENGTH = 80;

    private static List<String> getValueList(String value) {
        String[] valueArray = value.split("\n");
        List<String> valueList = new ArrayList<>();
        for (int i = 0; i < valueArray.length; i++) {
            if (valueArray[i].length() > MAX_ARG_LENGTH) {
                String[] partitions = valueArray[i].split(PARTITION_REGEX);
                valueList.addAll(Arrays.asList(partitions));
            }
        }
        return valueList;
    }

    public static BString getStringDiff(BString expected, BString actual) {
        List<String> expectedValueList = getValueList(expected.toString());
        List<String> difference = null;
        String output = "\n";
        try {
            Patch<String> patch = DiffUtils.diff(expectedValueList, getValueList(actual.toString()));
            difference = UnifiedDiffUtils.generateUnifiedDiff("expected", "actual",
                    expectedValueList, patch, MAX_ARG_LENGTH);
        } catch (DiffException e) {
            output = "\nWarning: Could not generate diff.";
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
                    output = output.concat(line + "\n");
                }
            }
        }
        return BStringUtils.fromString(output);
    }

}

