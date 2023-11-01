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
                        settings.getCentral().getCallTimeout());
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
    public CompletableFuture<JsonObject> trigger(BallerinaTriggerRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<JsonObject> trigger = getTriggerFromCentral(request);
            return trigger.orElseGet(JsonObject::new);
        });
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
                    settings.getCentral().getCallTimeout());
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
}
