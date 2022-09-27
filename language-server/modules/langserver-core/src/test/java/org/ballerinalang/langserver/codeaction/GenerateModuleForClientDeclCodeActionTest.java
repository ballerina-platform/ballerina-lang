package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

public class GenerateModuleForClientDeclCodeActionTest extends AbstractCodeActionTest{

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][] {
                {"generate_module_for_client_decl1.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "generate-module-for-client-decl";
    }
}
