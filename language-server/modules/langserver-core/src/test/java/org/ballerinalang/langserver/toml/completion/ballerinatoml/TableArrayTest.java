package org.ballerinalang.langserver.toml.completion.ballerinatoml;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.toml.completion.TomlCompletionTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * TableArray test.
 */
public class TableArrayTest extends TomlCompletionTest {

    @Test(dataProvider = "completion-data-provider")
    @Override
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        super.test(config, configPath);
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return getConfigsList();
    }

    @Override
    public String getTestResourceDir() {
        return "table_array_context";
    }
}
