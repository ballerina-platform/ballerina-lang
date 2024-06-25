package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test cases for {@link org.ballerinalang.langserver.codeaction.providers.ConvertToConfigurableCodeAction}
 *
 * @since 2201.10.0
 */
public class ConvertToConfigurableCodeActionTest extends AbstractCodeActionTest {

    @Override
    @Test(dataProvider = "codeaction-data-provider")
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
                {"extractIntToConstant.json"},
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"negativeTest1.json"},
                {"negativeTest2.json"},
                {"negativeTest3.json"},
                {"negativeTest4.json"},
                {"negativeTest5.json"},
                {"negativeTest6.json"},
                {"negativeTest7.json"},
                {"negativeTest8.json"},
                {"negativeTest9.json"},
                {"negativeTest10.json"},
                {"negativeTest11.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "convert-to-configurable";
    }
}
