package org.ballerinalang.langserver.semantictokens;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Tests semantic tokens feature in Language Server.
 */
public class SemanticTokensTest {

    private static final String DATA = "data";
    private static final String CONFIG = "config";
    private static final String EXPECTED = "expected";
    private static final String RESULT = "result";

    private static final Path RESOURCE_ROOT = FileUtils.RES_DIR.resolve("semantictokens");

    private Endpoint serviceEndpoint;

    @BeforeClass
    public void init() {

        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test semantic tokens", dataProvider = "semantictokens-data-provider")
    public void semanticTokensTestCase(String[] filePaths, String expected) throws IOException {

        Path sourcePath = RESOURCE_ROOT.resolve(CONFIG);
        for (String file : filePaths) {
            sourcePath = sourcePath.resolve(file);
        }
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String response = TestUtil.getSemanticTokensResponse(serviceEndpoint, sourcePath.toAbsolutePath().toString());
        compareResponse(expected, response);
    }

    /**
     * Compares actual response and expected response.
     *
     * @param expected Expected response
     * @param response JSON rpc response
     */
    private void compareResponse(String expected, String response) {

        Path expectedPath = RESOURCE_ROOT.resolve(EXPECTED).resolve(expected);
        JsonArray expectedJsonArray =
                FileUtils.fileContentAsObject(expectedPath.toAbsolutePath().toString()).getAsJsonArray(DATA);
        JsonArray responseJsonArray =
                JsonParser.parseString(response).getAsJsonObject().getAsJsonObject(RESULT).getAsJsonArray(DATA);
        Assert.assertEquals(responseJsonArray, expectedJsonArray, "SemanticTokensTest fails with " + expected +
                "test case.");
    }

    @AfterClass
    public void cleanupLanguageServer() {

        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider(name = "semantictokens-data-provider")
    public Object[][] getDataProvider() {

        return new Object[][]{
                {new String[]{"project", "main.bal"}, "default-module_expected.json"},
                {new String[]{"project", "modules", "module1", "main.bal"},
                        "module_expected.json"},
                {new String[]{"single-file", "main.bal"}, "single-file_expected.json"}
        };
    }
}
