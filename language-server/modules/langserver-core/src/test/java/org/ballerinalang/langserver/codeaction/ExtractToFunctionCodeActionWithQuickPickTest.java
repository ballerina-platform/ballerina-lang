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
 * Test class to test the functionality of the extract to function code action with quick pick capability.
 * Since:2201.3.3
 */
public class ExtractToFunctionCodeActionWithQuickPickTest extends AbstractCodeActionTest {

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

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"extractToFunctionExpr1.json"},
                {"extractToFunctionExpr2.json"},
                {"extractToFunctionExpr3.json"},
                {"extractToFunctionExpr4.json"},
                {"extractToFunctionExpr5.json"},
                {"extractToFunctionExpr6.json"},
                {"extractToFunctionExpr7.json"},
                {"extractToFunctionExpr8.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "extract-to-function";
    }
}
