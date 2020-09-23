package org.ballerinalang.testerina.core;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.values.BString;

import java.util.Arrays;
import java.util.List;

/**
 * Evaluates assertion values to find the difference inline.
 */
public class AssertionDiffEvaluator {
    public static BString getStringDiff(BString actual, BString expected) {
        String value = "Diff\t:\n~expected~\n**actual**\n";
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(false)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();
        try {   
            List<DiffRow> rows = generator.generateDiffRows(
                    Arrays.asList(expected.toString()),
                    Arrays.asList(actual.toString()));
            for (DiffRow row : rows) {
                value = value.concat(row.getOldLine() + "\n" + row.getNewLine() + "\n");
            }
        } catch (DiffException e) {
            value = "Error: Error while generating diff.";
        }
        return BStringUtils.fromString(value);

    }
}


