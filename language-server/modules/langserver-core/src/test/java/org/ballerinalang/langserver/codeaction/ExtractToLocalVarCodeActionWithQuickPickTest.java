package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test class to test the functionality of the extract to local variable code action with quick pick capability.
 * Since:2201.3.3
 */
public class ExtractToLocalVarCodeActionWithQuickPickTest extends AbstractCodeActionTest {

    @Override
    protected void setupLanguageServer(TestUtil.LanguageServerBuilder builder) {
        builder.withInitOption(InitializationOptions.KEY_QUICKPICK_SUPPORT, true);
    }

    @Override
    protected Path getConfigJsonPath(String configFilePath) {
        return FileUtils.RES_DIR.resolve("codeaction")
                .resolve(getResourceDir())
                .resolve("config-quick-pick-capability")
                .resolve(configFilePath);
    }

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @Override
    @Test(dataProvider = "negative-test-data-provider")
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"extractToLocalVarExpr1.json"},
                {"extractToLocalVarExpr2.json"},
                {"extractToLocalVarExpr3.json"},
                {"extractToLocalVarExpr4.json"},
                {"extractToLocalVarExpr5.json"},
                {"extractToLocalVarExpr6.json"},
                {"extractToLocalVarExpr7.json"}
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"negativeExtractToLocalVarExpr.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "extract-to-local-variable";
    }
}
