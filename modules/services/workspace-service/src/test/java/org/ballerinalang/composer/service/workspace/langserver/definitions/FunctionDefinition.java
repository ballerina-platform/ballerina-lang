package org.ballerinalang.composer.service.workspace.langserver.definitions;

import org.ballerinalang.composer.service.workspace.langserver.CompletionTest;
import org.testng.annotations.DataProvider;

import java.io.File;

/**
 * Completion item tests for function definition
 */
public class FunctionDefinition extends CompletionTest {
    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][] {
                {"emptyLinePrimitiveDataTypes.json", "completions" + File.separator + "function" + File.separator},
                {"nonEmptyLinePrimitiveDataTypes.json", "completions" + File.separator + "function" + File.separator},
                {"userDefinedStructEmptyLine.json", "completions" + File.separator + "function" + File.separator},
                {"userDefinedStructNonEmptyLine.json", "completions" + File.separator + "function" + File.separator},
                {"userDefinedFunctionsEmptyLine.json", "completions" + File.separator + "function" + File.separator},
                {"userDefinedFunctionsNonEmptyLine.json", "completions" + File.separator + "function" + File.separator},
                {"importPackagesEmptyLine.json", "completions" + File.separator + "function" + File.separator},
                {"importPackagesNonEmptyLine.json", "completions" + File.separator + "function" + File.separator},
                {"allVisibleSymbolsEmptyLine.json", "completions" + File.separator + "function" + File.separator},
                {"languageConstructsEmptyLine.json", "completions" + File.separator + "function" + File.separator},
                {"languageConstructsNonEmptyLine.json", "completions" + File.separator + "function" + File.separator}
        };
    }
}
