package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test Cases for CodeActions.
 *
 * @since 2.0.0
 */
public class MakePublicCodeActionTest extends AbstractCodeActionTest {
    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config, String source) throws IOException, WorkspaceDocumentException {
        super.test(config, source);
    }

    @Override
    public String getResourceDir() {
        return "make-type-public";
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][] {
                {"convert_to_public_class_config1.json",
                        "publicCodeAction/modules/module1/convert_to_public_class_source1.bal"},
                {"convert_to_public_record_config2.json",
                        "publicCodeAction/modules/module2/convert_to_public_record_source2.bal"},
                {"convert_to_public_record_config3.json",
                        "publicCodeAction/modules/module3/convert_to_public_record_source3.bal"}
        };
    }
}
