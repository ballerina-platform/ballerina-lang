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
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.model.Package;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.extensions.ballerina.connector.CentralPackageListResult;
import org.wso2.ballerinalang.util.RepoUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                this.getPackagesFromCentral().forEach(packageInfo -> {
                    PackageOrg packageOrg = PackageOrg.from(packageInfo.getOrganization());
                    PackageName packageName = PackageName.from(packageInfo.getName());
                    PackageVersion packageVersion = PackageVersion.from(packageInfo.getVersion());
                    centralPackages.add(new LSPackageLoader.ModuleInfo(packageOrg, packageName, packageVersion, null));
                });
                clientLogger.logTrace("Successfully loaded packages from Ballerina Central");
            }
            isLoaded = true;
            return this.centralPackages;
        });
    }

    private List<Package> getPackagesFromCentral() {
        List<Package> packageList = new ArrayList<>();
        try {
            for (int page = 0;; page++) {
                Settings settings = RepoUtils.readSettings();

                CentralAPIClient centralAPIClient = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                        ProjectUtils.initializeProxy(settings.getProxy()), ProjectUtils.getAccessTokenOfCLI(settings));
                CentralPackageDescriptor descriptor = new CentralPackageDescriptor("ballerinax", 10, page * 10);

                JsonElement newClientConnectors = centralAPIClient.getPackages(descriptor.getQueryMap(),
                        "any", RepoUtils.getBallerinaVersion());

                CentralPackageListResult packageListResult = new Gson().fromJson(newClientConnectors.getAsString(),
                        CentralPackageListResult.class);
                packageList.addAll(packageListResult.getPackages());
                int listResultCount = packageListResult.getCount();

                if (packageList.size() == listResultCount || descriptor.getOffset() >= listResultCount) {
                    break;
                }
            }

        } catch (Exception e) {
            // ignore
        }
        return packageList;
    }

    /**
     * Central package descriptor.
     */
    public static class CentralPackageDescriptor {
        private String organization;
        private int limit;
        private int offset;

        public CentralPackageDescriptor(String organization, int limit, int offset) {
            this.organization = organization;
            this.limit = limit;
            this.offset = offset;
        }

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public Map<String, String> getQueryMap() {
            Map<String, String> params = new HashMap();
            params.put("readme", "false");

            if (getOrganization() != null) {
                params.put("org", getOrganization());
            }

            if (getLimit() != 0) {
                params.put("limit", Integer.toString(getLimit()));
            }

            if (getOffset() != 0) {
                params.put("offset", Integer.toString(getOffset()));
            }

            return params;
        }
    }
}
