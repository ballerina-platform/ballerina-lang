package org.ballerinalang.langserver.codeaction;

import org.testng.annotations.DataProvider;

public class GenerateModuleForClientDeclCodeActionTest extends AbstractCodeActionTest{

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][] {
                {"generate_module_for_client_decl1.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "generate_module_for_client_decl";
    }
}
