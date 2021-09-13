package org.ballerinalang.langserver.hover;

import com.google.gson.JsonParser;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test performance of hover feature in language server.
 *
 * @since 2.0.0
 */
public class HoverPerformanceTest extends HoverProviderTest {
    private JsonParser parser = new JsonParser();

    @Test(description = "Test Hover provider", dataProvider = "hover-data-provider")
    public void testHover(String config) throws IOException {
        super.testHover(config);
    }

    @Override
    public String getResponse(Path sourcePath, Position position) {
        long start = System.currentTimeMillis();
        String responseString = parser.parse(TestUtil
                .getHoverResponse(sourcePath.toString(), position, serviceEndpoint)).getAsJsonObject().toString();
        long end = System.currentTimeMillis();
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        long actualResponseTime = end - start;
        int expectedResponseTime = Integer.parseInt(System.getProperty("responseTimeThreshold"))/2;
        Assert.assertTrue(actualResponseTime < expectedResponseTime,
                String.format("Expected response time = %d, received %d.", expectedResponseTime, actualResponseTime));
        return responseString;
    }

    @Override
    @DataProvider(name = "hover-data-provider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"hover_performance.json"},
        };
    }
}
