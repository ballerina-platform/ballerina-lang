package org.ballerinalang.testerina.core;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.values.BString;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Evaluates assertion values to find the difference inline.
 */
public class AssertionDiffEvaluator {

    public static String join(String... args) {
        return Joiner.on(' ').join(args);
    }

    public static BString getStringDiff(
            BString actual, BString expected) {
        DiffRowGenerator generator =
                DiffRowGenerator.create()
                        .showInlineDiffs(true)
                        .inlineDiffByWord(false)
                        .oldTag(f -> "~")
                        .newTag(f -> "*")
                        .build();
        List<DiffRow> rows;
        try {
            rows = generator.generateDiffRows(Arrays.asList(expected.toString()), Arrays.asList(actual.toString()));
        } catch (DiffException e) {
            return BStringUtils.fromString("Error: Error while generating diff.");
        }

        int maxExpectedLineLength =
                findMaxLineLength(rows.stream().map(DiffRow::getOldLine).collect(Collectors.toList()));
        int maxActualLineLength =
                findMaxLineLength(rows.stream().map(DiffRow::getNewLine).collect(Collectors.toList()));

        SideBySideRowFormatter sideBySideRowFormatter =
                new SideBySideRowFormatter(maxExpectedLineLength, maxActualLineLength);

        return BStringUtils.fromString("" +
                "\nDiff\t:\n\n" + Joiner.on('\n')
                .join(
                        sideBySideRowFormatter.formatRow("Expected", "Actual", ' '),
                        sideBySideRowFormatter.formatRow("", "", '-'),
                        rows.stream()
                                .map(
                                        row ->
                                                sideBySideRowFormatter.formatRow(row.getOldLine(), row.getNewLine(),
                                                        ' '))
                                .toArray()) + "\n\n");
    }

    private static int findMaxLineLength(Collection<String> lines) {
        return lines.stream()
                .max(Comparator.comparingInt(String::length))
                .map(String::length)
                .orElse(0);
    }

    private static class SideBySideRowFormatter {
        private final int maxExpectedLineLength;
        private final int maxActualLineLength;

        private SideBySideRowFormatter(int maxExpectedLineLength, int maxActualLineLength) {
            this.maxExpectedLineLength = maxExpectedLineLength;
            this.maxActualLineLength = maxActualLineLength;
        }

        public String formatRow(String expected, String actual, char padChar) {
            return String.format(
                    "|%s|%s|",
                    Strings.padEnd(expected, maxExpectedLineLength, padChar),
                    Strings.padEnd(actual, maxActualLineLength, padChar));
        }
    }
}


