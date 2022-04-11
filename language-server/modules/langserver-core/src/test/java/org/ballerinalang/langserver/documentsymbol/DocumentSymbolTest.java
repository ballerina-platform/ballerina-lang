package org.ballerinalang.langserver.documentsymbol;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
 * Document symbol test.
 */
public class DocumentSymbolTest {

    private Path configRoot;
    private Path sourceRoot;
    protected Gson gson = new Gson();
    protected JsonParser parser = new JsonParser();
    protected Endpoint serviceEndpoint;

    @BeforeClass
    public void init() throws Exception {
        this.configRoot = FileUtils.RES_DIR.resolve("documentsymbol").resolve("config");
        this.sourceRoot = FileUtils.RES_DIR.resolve("documentsymbol").resolve("source");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test Document Symbol", dataProvider = "testDataProvider")
    public void test(String configPath) throws IOException {
        JsonObject resultJson = FileUtils.fileContentAsObject(configRoot.resolve(configPath).toString());
        JsonObject source = resultJson.getAsJsonObject("source");
        Path sourcePath = sourceRoot.resolve(source.get("file").getAsString());
        TestUtil.openDocument(serviceEndpoint, sourcePath);

        String response = TestUtil.getDocumentSymbolResponse(serviceEndpoint, sourcePath.toString());
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        JsonArray expected = resultJson.getAsJsonArray("result");
        JsonArray actual = parser.parse(response).getAsJsonObject().getAsJsonArray("result");
        Assert.assertEquals(actual, expected);
    }

    @DataProvider
    public Object[] testDataProvider() {    
        return new Object[]{"config1.json", "config2.json"};
    }

    @AfterClass
    public void shutDownLanguageServer() throws IOException {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

}
