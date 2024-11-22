/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.ballerina.projects.Settings;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.wso2.ballerinalang.util.RepoUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Central package descriptor holder.
 *
 * @since 2201.8.0
 */
public class CentralPackageDescriptorLoader {
    public static final LanguageServerContext.Key<CentralPackageDescriptorLoader> CENTRAL_PACKAGE_HOLDER_KEY =
            new LanguageServerContext.Key<>();
    private final List<LSPackageLoader.ModuleInfo> centralPackages = new ArrayList<>();
    private static final String GET_PACKAGES_QUERY =
            "{\"query\": \"{packages(orgName:\\\"%s\\\" limit: %s) {packages {name version organization}}}\"}";
    private boolean isLoaded = false;

    private final LSClientLogger clientLogger;

    public static CentralPackageDescriptorLoader getInstance(LanguageServerContext context) {
        CentralPackageDescriptorLoader centralPackageDescriptorLoader = context.get(CENTRAL_PACKAGE_HOLDER_KEY);
        if (centralPackageDescriptorLoader == null) {
            centralPackageDescriptorLoader = new CentralPackageDescriptorLoader(context);
        }
        return centralPackageDescriptorLoader;
    }

    private CentralPackageDescriptorLoader(LanguageServerContext context) {
        context.put(CENTRAL_PACKAGE_HOLDER_KEY, this);
        this.clientLogger = LSClientLogger.getInstance(context);
    }

    public CompletableFuture<List<LSPackageLoader.ModuleInfo>> getCentralPackages() {
        return CompletableFuture.supplyAsync(() -> {
            if (!isLoaded) {
                //Load packages from central
                clientLogger.logTrace("Loading packages from Ballerina Central");
                centralPackages.addAll(this.getCentralGraphQLPackages());
                clientLogger.logTrace("Successfully loaded packages from Ballerina Central");
            }
            isLoaded = true;
            return this.centralPackages;
        });
    }

    private List<LSPackageLoader.ModuleInfo> getCentralGraphQLPackages() {
        try {
            Settings settings = RepoUtils.readSettings();
            CentralAPIClient centralAPIClient = new CentralAPIClient(RepoUtils.getRemoteRepoGraphQLURL(),
                    ProjectUtils.initializeProxy(settings.getProxy()), ProjectUtils.getAccessTokenOfCLI(settings));
            String query = String.format(GET_PACKAGES_QUERY, "ballerinax", 800);
            JsonElement allPackagesResponse = centralAPIClient.getCentralPackagesUsingGraphQL(query, "any",
                    RepoUtils.getBallerinaVersion());
            CentralPackageGraphQLResponse response = new Gson().fromJson(allPackagesResponse.getAsString(),
                    CentralPackageGraphQLResponse.class);
            return response.data.packages.packages;

        } catch (Exception e) {
            // ignore
        }
        return Collections.emptyList();
    }

    public record CentralPackageGraphQLResponse(Packages data) {
    }

    public record Packages(PackageList packages) {
    }

    public record PackageList(List<LSPackageLoader.ModuleInfo> packages) {
    }
}
