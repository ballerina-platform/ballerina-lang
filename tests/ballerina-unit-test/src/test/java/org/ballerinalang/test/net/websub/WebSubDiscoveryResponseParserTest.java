/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.net.websub;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class tests parsing the response to a WebSub discovery request, to identify the hub(s) and topic.
 *
 * @since 0.983.0
 */
public class WebSubDiscoveryResponseParserTest {

    private CompileResult result;

    private static final String HUB_ONE = "https://hub.ballerina.com";
    private static final String HUB_TWO = "https://two.hub.ballerina.com";
    private static final String HUB_THREE = "https://three.hub.ballerina.com";

    private static final String TOPIC_ONE = "https://topic.ballerina.com";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/net/websub/discovery/test_discovery_response_parsing.bal");
    }

    @Test(description = "Test discovery parsing for topic and single hub", dataProvider = "topicAndSingleHubTests")
    public void testTopicAndSingleHub(String testFunction) {
        BValue[] returns = BRunUtil.invoke(result, testFunction, new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), TOPIC_ONE, "incorrect topic extraction from discovery response");
        Assert.assertTrue(returns[1] instanceof BStringArray);
        BStringArray hubs = (BStringArray) returns[1];
        Assert.assertEquals(hubs.size(), 1, "incorrect no. of hubs extracted from discovery response");
        Assert.assertEquals(hubs.get(0), HUB_ONE, "incorrect hub extraction from discovery response");
    }

    @Test(description = "Test discovery parsing for topic and multiple hubs",
            dataProvider = "topicAndMultipleHubsTests")
    public void testTopicAndMultipleHubs(String testFunction) {
        BValue[] returns = BRunUtil.invoke(result, testFunction, new BValue[0]);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), TOPIC_ONE, "incorrect topic extraction from discovery response");
        Assert.assertTrue(returns[1] instanceof BStringArray);
        BStringArray hubs = (BStringArray) returns[1];
        Assert.assertEquals(hubs.size(), 3, "incorrect no. of hubs extracted from discovery response");
        Assert.assertEquals(hubs.get(0), HUB_ONE, "incorrect first hub extraction from discovery response");
        Assert.assertEquals(hubs.get(1), HUB_TWO, "incorrect second hub extraction from discovery response");
        Assert.assertEquals(hubs.get(2), HUB_THREE, "incorrect third hub extraction from discovery response");
    }

    @Test(description = "Test discovery parsing with unavailable topic", dataProvider = "topicUnavailableTests")
    public void testUnavailableTopic(String testFunction) {
        BValue[] returns = BRunUtil.invoke(result, testFunction, new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BMap) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "Hub and/or Topic URL(s) not identified in link header of discovery response",
                            "invalid error message on unavailable topic");
    }

    @Test(description = "Test discovery parsing with unavailable hub", dataProvider = "hubUnavailableTests")
    public void testUnavailableHub(String testFunction) {
        BValue[] returns = BRunUtil.invoke(result, testFunction, new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BMap) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "Hub and/or Topic URL(s) not identified in link header of discovery response",
                            "invalid error message on unavailable hub");
    }

    @Test(description = "Test discovery parsing for response without link headers")
    public void testMissingLinkHeader() {
        BValue[] returns = BRunUtil.invoke(result, "testMissingLinkHeader", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BMap) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "Link header unavailable in discovery response",
                            "invalid error message on unavailable link headers(s)");
    }

    @Test(description = "Test discovery parsing with multiple topics", dataProvider = "multipleTopicsTests")
    public void testMultipleTopics(String testFunction) {
        BValue[] returns = BRunUtil.invoke(result, testFunction, new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BMap) ((BError) returns[0]).getDetails()).get("message").stringValue(),
                            "Link Header contains > 1 self URLs",
                            "invalid error message on > 1 topics");
    }

    @DataProvider(name = "topicAndSingleHubTests")
    public Object[][] topicAndSingleHubTests() {
        return new Object[][] {
                { "testTopicAndSingleHubAsSingleLinkHeader" },
                { "testTopicAndSingleHubAsMultipleLinkHeaders" }
        };
    }

    @DataProvider(name = "topicAndMultipleHubsTests")
    public Object[][] topicAndMultipleHubsTests() {
        return new Object[][] {
                { "testTopicAndMultipleHubsAsSingleLinkHeader" },
                { "testTopicAndMultipleHubsAsMultipleLinkHeaders" }
        };
    }

    @DataProvider(name = "topicUnavailableTests")
    public Object[][] topicUnavailableTests() {
        return new Object[][] {
                { "testMissingTopicWithSingleLinkHeader" },
                { "testMissingTopicWithMultipleLinkHeaders" }
        };
    }

    @DataProvider(name = "hubUnavailableTests")
    public Object[][] hubUnavailableTests() {
        return new Object[][] {
                { "testMissingHubWithSingleLinkHeader" },
                { "testMissingHubWithMultipleLinkHeaders" }
        };
    }

    @DataProvider(name = "multipleTopicsTests")
    public Object[][] multipleTopicsTests() {
        return new Object[][] {
                { "testSingleLinkHeaderWithMultipleTopics" },
                { "testMultipleLinkHeadersWithMultipleTopics" }
        };
    }
}
