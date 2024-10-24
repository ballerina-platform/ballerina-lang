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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.projects.Settings;
import io.ballerina.trigger.entity.BallerinaTriggerListRequest;
import io.ballerina.trigger.entity.BallerinaTriggerListResponse;
import io.ballerina.trigger.entity.BallerinaTriggerRequest;
import io.ballerina.trigger.entity.CentralTriggerListResult;
import io.ballerina.trigger.entity.Constants;
import io.ballerina.trigger.entity.Trigger;
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    public static final String BALLERINA = "ballerina";
    public static final String BALLERINAX = "ballerinax";
    private LanguageClient languageClient;
    private static final Map<String, String> IN_BUILT_TRIGGERS = Map.of(
        "10006", "ftp",
        "10005", "jms",
        "10004", "mqtt",
        "10003", "nats",
        "10002", "rabbitmq",
        "10001", "kafka"
    );

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
                Settings settings = RepoUtils.readSettings();
                CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                        initializeProxy(settings.getProxy()), settings.getProxy().username(),
                        settings.getProxy().password(), getAccessTokenOfCLI(settings),
                        settings.getCentral().getConnectTimeout(),
                        settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                        settings.getCentral().getCallTimeout(), settings.getCentral().getMaxRetries());
                JsonElement triggerSearchResult = client.getTriggers(request.getQueryMap(),
                        "any", RepoUtils.getBallerinaVersion());
                CentralTriggerListResult centralTriggerListResult = new Gson().fromJson(
                        triggerSearchResult.getAsString(), CentralTriggerListResult.class);
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
    public CompletableFuture<BallerinaTriggerListResponse> triggersNew(BallerinaTriggerListRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaTriggerListResponse triggersList = new BallerinaTriggerListResponse();
            try {
                triggersList.addInBuiltTriggers(getInBuiltTriggers());
                Settings settings = RepoUtils.readSettings();
                CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                        initializeProxy(settings.getProxy()), settings.getProxy().username(),
                        settings.getProxy().password(), getAccessTokenOfCLI(settings),
                        settings.getCentral().getConnectTimeout(),
                        settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                        settings.getCentral().getCallTimeout(), settings.getCentral().getMaxRetries());
                JsonElement triggerSearchResult = client.getTriggers(request.getQueryMap(),
                        "any", RepoUtils.getBallerinaVersion());
                CentralTriggerListResult centralTriggerListResult = new Gson().fromJson(
                        triggerSearchResult.getAsString(), CentralTriggerListResult.class);
                triggersList.addCentralTriggers(centralTriggerListResult.getTriggers());
                return triggersList;
            } catch (CentralClientException | SettingsTomlException e) {
                String msg = "Operation 'ballerinaTrigger/triggers' failed!";
                this.languageClient.logMessage(new MessageParams(MessageType.Error, msg));
                return triggersList;
            }
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
                return getInBuiltTrigger(request.getPackageName()).orElseGet(JsonObject::new);
            }
            if (request.getTriggerId() != null) {
                Optional<JsonObject> trigger = getInBuiltTriggerById(request.getTriggerId());
                if (trigger.isPresent()) {
                    return trigger.get();
                }
            }

            Optional<JsonObject> trigger = getTriggerFromCentral(request);
            return trigger.orElseGet(JsonObject::new);
        });
    }

    private static boolean expectsTriggerByName(BallerinaTriggerRequest request) {
        return request.getTriggerId() == null && request.getOrgName() != null && request.getPackageName() != null
                && (request.getOrgName().trim().equals(BALLERINA) || request.getOrgName().equals(BALLERINAX));
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
        } catch (CentralClientException | SettingsTomlException e) {
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

    private List<Trigger> getInBuiltTriggers() {
        return IN_BUILT_TRIGGERS.values().stream()
                .map(this::getInBuiltTriggerInfo)
                .flatMap(Optional::stream)
                .map(s -> new Gson().fromJson(s, Trigger.class))
                .toList();
    }

    private Optional<JsonObject> getInBuiltTriggerById(String triggerId) {
        if (!IN_BUILT_TRIGGERS.containsKey(triggerId)) {
            return Optional.empty();
        }
        return getInBuiltTrigger(IN_BUILT_TRIGGERS.get(triggerId));
    }

    private Optional<JsonObject> getInBuiltTrigger(String triggerName) {
        return getInBuiltTriggerInfo(triggerName)
                .map(s -> new Gson().fromJson(s, JsonObject.class));
    }

    private Optional<String> getInBuiltTriggerInfo(String triggerName) {
        URL triggerURL = BallerinaTriggerService.class.getClassLoader()
                .getResource("inbuilt-triggers/" + triggerName + ".json");
        if (triggerURL == null) {
            String msg = String.format("Trigger info file not found for the trigger: %s", triggerName);
            this.languageClient.logMessage(new MessageParams(MessageType.Error, msg));
            return Optional.empty();
        }

        try {
            return Optional.of(Files.readString(Path.of(triggerURL.getPath())));
        } catch (IOException e) {
            String msg = String.format("Error occurred while reading the trigger info file for the trigger: %s",
                    triggerName);
            this.languageClient.logMessage(new MessageParams(MessageType.Error, msg));
            return Optional.empty();
        }
    }
}
