/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.trigger;

import com.google.gson.JsonObject;
import io.ballerina.trigger.entity.BallerinaTriggerListRequest;
import io.ballerina.trigger.entity.BallerinaTriggerListResponse;
import io.ballerina.trigger.entity.BallerinaTriggerRequest;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Tests for Trigger Service.
 */
public class TriggerServiceTest {

    private static final String BALLERINA_TRIGGERS = "ballerinaTrigger/triggers";
    private static final String BALLERINA_TRIGGER = "ballerinaTrigger/trigger";
    private static final String BALLERINA_TRIGGERS_NEW = "ballerinaTrigger/triggersNew";
    private static final String BALLERINA_TRIGGER_NEW = "ballerinaTrigger/triggerNew";

    @Test(description = "Test triggers endpoint of trigger service")
    public void testTriggersService() throws ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();

        BallerinaTriggerListRequest request = new BallerinaTriggerListRequest();
        CompletableFuture<?> result = serviceEndpoint.request(BALLERINA_TRIGGERS, request);
        BallerinaTriggerListResponse response = (BallerinaTriggerListResponse) result.get();

        Assert.assertTrue(!response.getCentralTriggers().isEmpty());
    }

    @Test(description = "Test new triggers endpoint of trigger service")
    public void testTriggersNewService() throws ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();

        BallerinaTriggerListRequest request = new BallerinaTriggerListRequest();
        CompletableFuture<?> result = serviceEndpoint.request(BALLERINA_TRIGGERS_NEW, request);
        JsonObject response = (JsonObject) result.get();

        Assert.assertTrue(response.has("central"));
        Assert.assertFalse(response.getAsJsonArray("central").isEmpty());
        Assert.assertTrue(response.getAsJsonArray("central").asList().stream()
                .anyMatch(trigger -> trigger.getAsJsonObject().get("moduleName").getAsString().contains("kafka")));
        Assert.assertTrue(response.getAsJsonArray("central").asList().stream()
                .anyMatch(trigger -> trigger.getAsJsonObject().get("moduleName").getAsString().contains("ftp")));
    }

    @Test(description = "Test trigger endpoint of trigger service")
    public void testTriggerService() throws ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();

        BallerinaTriggerRequest request = new BallerinaTriggerRequest("2");
        CompletableFuture<?> result = serviceEndpoint.request(BALLERINA_TRIGGER, request);
        JsonObject response = (JsonObject) result.get();

        Assert.assertEquals(response.get("id").getAsString(), "2");
    }

    @Test(description = "Test new trigger endpoint of trigger service")
    public void testTriggerNewService() throws ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();

        BallerinaTriggerRequest request = new BallerinaTriggerRequest("2");
        CompletableFuture<?> result = serviceEndpoint.request(BALLERINA_TRIGGER_NEW, request);
        JsonObject response = (JsonObject) result.get();

        Assert.assertEquals(response.get("id").getAsString(), "2");
        Assert.assertEquals(response.get("moduleName").getAsString(), "rabbitmq");
    }

    @Test(description = "Test new trigger endpoint of trigger service without id")
    public void testTriggerNewServiceWithoutId() throws ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();

        BallerinaTriggerRequest request = new BallerinaTriggerRequest("ballerinax", "kafka",  "kafka",
                "*", "Kafka Event Listener");
        CompletableFuture<?> result = serviceEndpoint.request(BALLERINA_TRIGGER_NEW, request);
        JsonObject response = (JsonObject) result.get();

        Assert.assertEquals(response.get("id").getAsString(), "1");
        Assert.assertEquals(response.get("moduleName").getAsString(), "kafka");

        request = new BallerinaTriggerRequest("ballerina", "mqtt",  "mqtt",
                "*", "MQTT Event Listener");
        result = serviceEndpoint.request(BALLERINA_TRIGGER_NEW, request);
        response = (JsonObject) result.get();

        Assert.assertEquals(response.get("id").getAsString(), "4");
        Assert.assertEquals(response.get("moduleName").getAsString(), "mqtt");
    }

    @Test(description = "Test new triggers endpoint of trigger service with query")
    public void testTriggersNewServiceWithQuery() throws ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();

        BallerinaTriggerListRequest request = new BallerinaTriggerListRequest();
        request.setQuery("kafka");
        CompletableFuture<?> result = serviceEndpoint.request(BALLERINA_TRIGGERS_NEW, request);
        JsonObject response = (JsonObject) result.get();

        Assert.assertTrue(response.has("central"));
        Assert.assertFalse(response.getAsJsonArray("central").isEmpty());
        Assert.assertTrue(response.getAsJsonArray("central").asList().stream()
                .anyMatch(trigger -> trigger.getAsJsonObject().get("moduleName").getAsString().contains("kafka")));
        Assert.assertTrue(response.getAsJsonArray("central").asList().stream()
                .noneMatch(trigger -> trigger.getAsJsonObject().get("moduleName").getAsString().contains("ftp")));
    }

    @Test(description = "Test new triggers endpoint of trigger service with organization")
    public void testTriggersNewServiceWithOrg() throws ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();

        BallerinaTriggerListRequest request = new BallerinaTriggerListRequest();
        request.setOrganization("ballerina");
        CompletableFuture<?> result = serviceEndpoint.request(BALLERINA_TRIGGERS_NEW, request);
        JsonObject response = (JsonObject) result.get();

        Assert.assertTrue(response.has("central"));
        Assert.assertFalse(response.getAsJsonArray("central").isEmpty());
        Assert.assertTrue(response.getAsJsonArray("central").asList().stream()
                .anyMatch(trigger -> trigger.getAsJsonObject().get("moduleName").getAsString().contains("mqtt")));
        Assert.assertTrue(response.getAsJsonArray("central").asList().stream()
                .anyMatch(trigger -> trigger.getAsJsonObject().get("moduleName").getAsString().contains("ftp")));
        Assert.assertTrue(response.getAsJsonArray("central").asList().stream()
                .noneMatch(trigger -> trigger.getAsJsonObject().get("moduleName").getAsString().contains("kafka")));
    }

    @Test(description = "Test new triggers endpoint of trigger service with limit")
    public void testTriggersNewServiceWithLimit() throws ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();

        BallerinaTriggerListRequest request = new BallerinaTriggerListRequest();
        request.setLimit(10);
        CompletableFuture<?> result = serviceEndpoint.request(BALLERINA_TRIGGERS_NEW, request);
        JsonObject response = (JsonObject) result.get();

        Assert.assertTrue(response.has("central"));
        Assert.assertFalse(response.getAsJsonArray("central").isEmpty());
        Assert.assertTrue(response.getAsJsonArray("central").size() == 9);

        request.setLimit(2);
        result = serviceEndpoint.request(BALLERINA_TRIGGERS_NEW, request);
        response = (JsonObject) result.get();

        Assert.assertTrue(response.has("central"));
        Assert.assertFalse(response.getAsJsonArray("central").isEmpty());
        Assert.assertTrue(response.getAsJsonArray("central").size() == 2);
    }
}
