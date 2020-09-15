package org.ballerinalang.testerina.core;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Evaluates assertion values to find the difference inline.
 */
public class AssertionDiffEvaluator {
    public static BString getStringDiff(String actual, String expected) throws BallerinaException {
        String value = "";
        try {
            Patch<String> patch = DiffUtils.diffInline(actual, expected);
            for (AbstractDelta<String> delta : patch.getDeltas()) {
                value = value.concat(delta.toString());
            }
        } catch (DiffException e) {
            throw new BallerinaException("Diff inline failed");
        }
        return new BString(value);
    }
}
