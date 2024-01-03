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
import java.util.List;

/**
 * Tests semantic tokens feature in Language Server.
 */
public class SemanticTokensTest {

    private static final String DATA = "data";
    private static final String CONFIG = "config";
    private static final String EXPECTED = "expected";
    private static final String RESULT = "result";

    private static final List<String> TOKEN_TYPES = SemanticTokensUtils.getTokenTypes();

    private static final List<String> TOKEN_MODIFIERS = SemanticTokensUtils.getTokenTypeModifiers();

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
        JsonArray decoded = decodeIntArray(responseJsonArray);
        Assert.assertEquals(decoded, expectedJsonArray, "SemanticTokensTest fails with " +
                expected + "test case.");
    }

    /**
     * Decodes token types, modifiers, and absolute positions of semantic tokens.
     *
     * @param in API resulting integer array with semantic token data
     * @return Decoded array
     */
    private JsonArray decodeIntArray(JsonArray in) {
        JsonArray out = new JsonArray();
        int prevLine = -1;
        int preColumn = -1;
        for (int i = 0; i < in.size(); i = i + 5) {
            int line = in.get(i).getAsInt();
            int column = in.get(i + 1).getAsInt();
            if (prevLine == -1) {
                prevLine = line;
                preColumn = column;
            } else {
                if (line == 0) {
                    preColumn += column;
                } else {
                    prevLine += line;
                    preColumn = column;
                }
            }
            out.add(prevLine);
            out.add(preColumn);
            out.add(in.get(i + 2).getAsInt());
            out.add(TOKEN_TYPES.get(in.get(i + 3).getAsInt()));
            int tokenModifiers = in.get(i + 4).getAsInt();
            JsonArray modifiers = new JsonArray();
            int count = 0;
            while (tokenModifiers != 0) {
                if (tokenModifiers % 2 == 1) {
                    modifiers.add(TOKEN_MODIFIERS.get(count));
                }
                tokenModifiers = tokenModifiers / 2;
                count++;
            }
            out.add(modifiers);
        }
        return out;
    }

    @AfterClass
    public void cleanupLanguageServer() {

        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider(name = "semantictokens-data-provider")
    public Object[][] getDataProvider() {

        return new Object[][]{
                {new String[]{"project", "main.bal"}, "default-module_expected.json"},
                {new String[]{"project", "modules", "module1", "main.bal"}, "module_expected.json"},
                {new String[]{"project", "tests", "test.bal"}, "tests_expected.json"},
                {new String[]{"single-file", "main.bal"}, "single-file_expected.json"},
                {new String[]{"service", "main.bal"}, "service_expected.json"},
                {new String[]{"single-file", "check.bal"}, "check_expected.json"},
                {new String[]{"single-file", "record.bal"}, "record_expected.json"}
        };
    }
}
