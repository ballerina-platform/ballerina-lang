package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.TestUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test class to test the extract to transform function code action.
 *
 * @since 2201.9.0
 */
public class ExtractToTransformFunctionCodeActionTest extends AbstractCodeActionTest {

    @Override
    protected void setupLanguageServer(TestUtil.LanguageServerBuilder builder) {
        builder.withInitOption(InitializationOptions.KEY_POSITIONAL_RENAME_SUPPORT, true);
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
                {"extract_to_transform_function1.json"},
                {"extract_to_transform_function2.json"},
                {"extract_to_transform_function3.json"},
                {"extract_to_transform_function4.json"},
                {"extract_to_transform_function5.json"},
                {"extract_to_transform_function6.json"},
                {"extract_to_transform_function7.json"},
                {"extract_to_transform_function8.json"},
                {"extract_to_transform_function9.json"},
                {"extract_to_transform_function10.json"},
        };
    }

    @Override
    public String getResourceDir() {
        return "extract-to-transform-function";
    }
}