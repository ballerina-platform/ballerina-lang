package org.ballerinalang.testerina.core;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Arrays;
import java.util.List;

/**
 * Evaluates assertion values to find the difference inline.
 */
public class AssertionDiffEvaluator {
    public static BString getStringDiff(String actual, String expected) throws BallerinaException {
        String value = "Diff\t:\n~expected~\n**actual**\n";
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(false)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();
        try {   
            List<DiffRow> rows = generator.generateDiffRows(
                    Arrays.asList(expected),
                    Arrays.asList(actual));
            for (DiffRow row : rows) {
                value = value.concat(row.getOldLine() + "\n" + row.getNewLine() + "\n");
            }
        } catch (DiffException e) {
            throw new BallerinaException("Diff inline failed");
        }
        return new BString(value);
    }
}


