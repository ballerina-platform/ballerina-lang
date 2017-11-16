package org.ballerinalang.composer.service.workspace.langserver;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ballerinalang.composer.service.workspace.langserver.definitions.FunctionDefinition;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.ballerinalang.composer.service.workspace.langserver.dto.RequestMessage;
import org.ballerinalang.composer.service.workspace.langserver.util.FileUtils;
import org.ballerinalang.composer.service.workspace.langserver.util.MessageUtil;
import org.ballerinalang.composer.service.workspace.langserver.util.ServerManager;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import java.util.List;

/**
 * Completion Test Interface
 */
public abstract class CompletionTest {

    private static final Logger LOGGER = Logger.getLogger(FunctionDefinition.class);

    @Test(dataProvider = "completion-data-provider")
    public void test(String config, String configPath) {
        String configJsonPath =  configPath + config;
        JsonObject jsonObject = null;
        jsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonArray expectedItems = jsonObject.get("items").getAsJsonArray();
        JsonObject positionObj = jsonObject.get("position").getAsJsonObject();

        String balPath = jsonObject.get("source").getAsString();
        Position position = new Position();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());
        String content = FileUtils.fileContent(balPath);
        RequestMessage requestMessage = MessageUtil.getRequestMessage(content, position, MessageUtil.MESSAGE_ID);
        List<CompletionItem> responseItemList = ServerManager.getCompletions(requestMessage);
        List<CompletionItem> expectedList = MessageUtil.getExpectedItemList(expectedItems);

        Assert.assertEquals(true, MessageUtil.isSubList(expectedList, responseItemList));
    }

    @DataProvider(name = "completion-data-provider")
    public abstract Object[][] dataProvider();

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            LOGGER.error("Test Failed for: [" + result.getParameters()[1] + result.getParameters()[0] + "]");
        }
    }
}
