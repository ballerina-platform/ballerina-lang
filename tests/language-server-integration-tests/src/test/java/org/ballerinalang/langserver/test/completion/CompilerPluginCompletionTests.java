package org.ballerinalang.langserver.test.completion;

import org.ballerinalang.langserver.codeaction.AbstractCodeActionTest;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.completion.CompletionTest;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Completion tests for compiler plugin based completion providers.
 */
public class CompilerPluginCompletionTests extends CompletionTest {

    @BeforeSuite
    public void compilePlugins() {
        BCompileUtil.compileAndCacheBala("compiler_plugin_tests/package_comp_plugin_with_completions");
    }

    @Override
    @Test(dataProvider = "completion-data-provider")
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        super.test(config, configPath);
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"compiler_plugin_with_completions_config1.json", getTestResourceDir()},
                {"compiler_plugin_with_completions_config2.json", getTestResourceDir()},
                {"compiler_plugin_completion_single_file_config1.json", getTestResourceDir()}
        };
    }

    @Override
    public String getTestResourceDir() {
        return "compiler-plugins";
    }

}

