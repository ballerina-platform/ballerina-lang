package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test cases for {@link org.ballerinalang.langserver.codeaction.providers.AddElvisOperatorCodeAction}.
 *
 * @since 2201.2.1
 */
public class AddElvisOperatorCodeActionTest extends AbstractCodeActionTest {

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @Test(dataProvider = "negative-codeaction-data-provider")
    @Override
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"add_elvis_operator_in_mapping_constructor.json"},
                {"add_elvis_operator_in_variable_declaration1.json"},
                {"add_elvis_operator_in_variable_declaration2.json"},
                {"add_elvis_operator_in_positional_argument.json"},
                {"add_elvis_operator_in_return_statement.json"},
                {"add_elvis_operator_in_let_var1.json"},
                {"add_elvis_operator_in_let_var2.json"}
        };
    }

    @DataProvider(name = "negative-codeaction-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"negative_add_elvis_operator_in_mapping_constructor.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "add-elvis-operator";
    }
}
