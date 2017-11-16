package org.ballerinalang.composer.service.workspace.langserver.compilationunit;

import org.ballerinalang.composer.service.workspace.langserver.CompletionTest;
import org.testng.annotations.DataProvider;

import java.io.File;

/**
 * Top level completion tests (compilation unit level)
 */
public class TopLevelCompletions extends CompletionTest {
    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][] {
//                {"topLevelEmptyFirstLine.json", "completions" + File.separator + "function" + File.separator},
                {"topLevelNonEmptyFirstLine.json", "completions" + File.separator + "toplevel" + File.separator}
        };
    }
}
