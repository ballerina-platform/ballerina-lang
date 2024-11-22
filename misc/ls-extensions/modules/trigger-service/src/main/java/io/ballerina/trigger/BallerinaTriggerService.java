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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import io.ballerina.projects.Settings;
import io.ballerina.trigger.entity.BallerinaTriggerListRequest;
import io.ballerina.trigger.entity.BallerinaTriggerListResponse;
import io.ballerina.trigger.entity.BallerinaTriggerRequest;
import io.ballerina.trigger.entity.CentralTriggerListResult;
import io.ballerina.trigger.entity.Constants;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;

/**
 * Implementation of the Ballerina Trigger Service.
 * This is being used to fetch the list of triggers from the Ballerina Central and
 * Get the details of a selected trigger
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("ballerinaTrigger")
public class BallerinaTriggerService implements ExtendedLanguageServerService {
    private LanguageClient languageClient;
    private final Map<String, InBuiltTrigger> inBuiltTriggers;
    private static final Type MAP_TYPE = MapTypeToken.TYPE;


    // Represents the in-built trigger basic information
    public record InBuiltTrigger(String name, String orgName, String packageName, List<String> keywords) {
        public InBuiltTrigger(String name, String orgName, String packageName, List<String> keywords) {
            this.name = name;
            this.orgName = orgName;
            this.packageName = packageName;
            // Make a defensive copy of the list to ensure immutability
            this.keywords = keywords == null ? List.of() : new ArrayList<>(keywords);
        }

        @Override
        public List<String> keywords() {
            // Return an unmodifiable or defensive copy, as needed
            return List.copyOf(keywords);
        }
    }

    public BallerinaTriggerService() {
        InputStream propertiesStream = getClass().getClassLoader()
                .getResourceAsStream("inbuilt-triggers/properties.json");
        Map<String, InBuiltTrigger> triggers = Map.of();
        if (propertiesStream != null) {
            try (JsonReader reader = new JsonReader(new InputStreamReader(propertiesStream, StandardCharsets.UTF_8))) {
                triggers = new Gson().fromJson(reader, MAP_TYPE);
            } catch (IOException e) {
                // Ignore
            }
        }
        this.inBuiltTriggers = triggers;
    }

    // Static inner class to hold the type token
    private static class MapTypeToken {
        private static final Type TYPE = new TypeToken<Map<String, InBuiltTrigger>>() { }.getType();
    }

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager,
                     LanguageServerContext serverContext) {
        this.languageClient = serverContext.get(ExtendedLanguageClient.class);
    }

    @JsonRequest
    public CompletableFuture<BallerinaTriggerListResponse> triggers(BallerinaTriggerListRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaTriggerListResponse triggersList = new BallerinaTriggerListResponse();
            try {
                CentralTriggerListResult centralTriggerListResult = getCentralTriggerListResult(request);
                triggersList.setCentralTriggers(centralTriggerListResult.getTriggers());
                return triggersList;
            } catch (CentralClientException | SettingsTomlException e) {
                String msg = "Operation 'ballerinaTrigger/triggers' failed!";
                this.languageClient.logMessage(new MessageParams(MessageType.Error, msg));
                return triggersList;
            }
        });
    }

    @JsonRequest
    public CompletableFuture<JsonObject> triggersNew(BallerinaTriggerListRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject triggersList = new JsonObject();
            List<JsonObject> inBuiltTriggers = getInBuiltTriggers(request);
            triggersList.add("central", new Gson().toJsonTree(inBuiltTriggers));
            return triggersList;
        });
    }

    @JsonRequest
    public CompletableFuture<JsonObject> trigger(BallerinaTriggerRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<JsonObject> trigger = getTriggerFromCentral(request);
            return trigger.orElseGet(JsonObject::new);
        });
    }

    @JsonRequest
    public CompletableFuture<JsonObject> triggerNew(BallerinaTriggerRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            if (expectsTriggerByName(request)) {
                return getTriggerByName(request).orElseGet(JsonObject::new);
            }
            if (request.getTriggerId() != null) {
                Optional<JsonObject> trigger = getInBuiltTriggerJsonById(request.getTriggerId());
                if (trigger.isPresent()) {
                    return trigger.get();
                }
            }
            return new JsonObject();
        });
    }

    private static boolean expectsTriggerByName(BallerinaTriggerRequest request) {
        return request.getTriggerId() == null && request.getOrgName() != null && request.getPackageName() != null;
    }

    private Optional<JsonObject> getTriggerByName(BallerinaTriggerRequest request) {
        return getInBuiltTriggerJson(request.getPackageName());
    }

    private static CentralTriggerListResult getCentralTriggerListResult(BallerinaTriggerListRequest triggerListRequest)
            throws SettingsTomlException, CentralClientException {
        Settings settings = RepoUtils.readSettings();
        CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings),
                settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout(), settings.getCentral().getMaxRetries());
        JsonElement triggerSearchResult = client.getTriggers(triggerListRequest.getQueryMap(),
                "any", RepoUtils.getBallerinaVersion());
        return new Gson().fromJson(triggerSearchResult.getAsString(), CentralTriggerListResult.class);
    }

    private Optional<JsonObject> getTriggerFromCentral(BallerinaTriggerRequest request) {
        JsonObject trigger;
        try {
            Settings settings = RepoUtils.readSettings();
            CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                    initializeProxy(settings.getProxy()), settings.getProxy().username(),
                    settings.getProxy().password(),
                    getAccessTokenOfCLI(settings),
                    settings.getCentral().getConnectTimeout(),
                    settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                    settings.getCentral().getCallTimeout(), settings.getCentral().getMaxRetries());
            if (request.getTriggerId() != null) {
                trigger = client.getTrigger(request.getTriggerId(), "any", RepoUtils.getBallerinaVersion());
                return Optional.of(trigger);
            }
        } catch (CentralClientException e) {
            String msg = "Operation 'ballerinaTrigger/trigger' failed!";
            this.languageClient.logMessage(new MessageParams(MessageType.Error, msg));
        }
        return Optional.empty();
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    @Override
    public String getName() {
        return Constants.CAPABILITY_NAME;
    }

    private List<JsonObject> getInBuiltTriggers(BallerinaTriggerListRequest request) {
        return inBuiltTriggers.values().stream()
                .filter(inBuiltTrigger -> filterInBuiltTriggers(inBuiltTrigger, request))
                .map(inBuiltTrigger -> getInBuiltTriggerJson(inBuiltTrigger.name()))
                .flatMap(Optional::stream)
                .limit(request.getLimit() > 0 ? request.getLimit() : Long.MAX_VALUE)
                .toList();
    }

    private boolean filterInBuiltTriggers(InBuiltTrigger inBuiltTrigger, BallerinaTriggerListRequest request) {
        return (request.getOrganization() == null || request.getOrganization().equals(inBuiltTrigger.orgName())) &&
                (request.getPackageName() == null || request.getPackageName().equals(inBuiltTrigger.packageName())) &&
                (request.getKeyword() == null || inBuiltTrigger.keywords().stream()
                        .anyMatch(keyword -> keyword.equalsIgnoreCase(request.getKeyword()))) &&
                (request.getQuery() == null || inBuiltTrigger.keywords().stream()
                        .anyMatch(keyword -> keyword.contains(request.getQuery())));
    }

    private Optional<JsonObject> getInBuiltTriggerJsonById(String triggerId) {
        if (!inBuiltTriggers.containsKey(triggerId)) {
            return Optional.empty();
        }
        return getInBuiltTriggerJson(inBuiltTriggers.get(triggerId).name());
    }

    private Optional<JsonObject> getInBuiltTriggerJson(String triggerName) {
        if (inBuiltTriggers.values().stream()
                .noneMatch(inBuiltTrigger -> inBuiltTrigger.name().equals(triggerName))) {
            return Optional.empty();
        }
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(
                String.format("inbuilt-triggers/%s.json", triggerName));
        if (resourceStream == null) {
            String msg = String.format("Trigger info file not found for the trigger: %s", triggerName);
            this.languageClient.logMessage(new MessageParams(MessageType.Error, msg));
            return Optional.empty();
        }

        try (JsonReader reader = new JsonReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8))) {
            return Optional.of(new Gson().fromJson(reader, JsonObject.class));
        } catch (IOException e) {
            String msg = String.format("Error occurred while reading the trigger info file for the trigger: %s",
                    triggerName);
            this.languageClient.logMessage(new MessageParams(MessageType.Error, msg));
            return Optional.empty();
        }
    }
}
