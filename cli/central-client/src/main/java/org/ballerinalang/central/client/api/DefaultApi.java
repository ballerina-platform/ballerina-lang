/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.central.client.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ballerinalang.central.client.ApiClient;
import org.ballerinalang.central.client.ApiException;
import org.ballerinalang.central.client.Pair;
import org.ballerinalang.central.client.model.ModuleJsonSchema;
import org.ballerinalang.central.client.model.PackageJsonSchema;
import org.ballerinalang.central.client.model.PackageStatsJsonSchema;
import org.ballerinalang.central.client.model.ResolvePackagesJsonSchema;
import org.ballerinalang.central.client.model.ResolvedPackagesJsonSchema;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

public class DefaultApi {
    private final HttpClient memberVarHttpClient;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;

    public DefaultApi() {
        this(new ApiClient());
    }

    public DefaultApi(ApiClient apiClient) {
        memberVarHttpClient = apiClient.getHttpClient();
        memberVarBaseUri = apiClient.getBaseUri();
        memberVarInterceptor = apiClient.getRequestInterceptor();
        memberVarReadTimeout = apiClient.getReadTimeout();
        memberVarResponseInterceptor = apiClient.getResponseInterceptor();
    }

    /**
     * Get module information
     *
     * @param orgNamePath     The organization name of the module. (required)
     * @param packageNamePath The name of the package. (required)
     * @param version         The version or version range of the package. (required)
     * @param moduleNamePath  The name of the module. (required)
     * @return ModuleJsonSchema
     * @throws ApiException if fails to make API call
     */
    public ModuleJsonSchema getModule(String orgNamePath, String packageNamePath, String version, String moduleNamePath)
            throws ApiException {
        // verify the required parameter 'orgNamePath' is set
        if (orgNamePath == null) {
            throw new ApiException(400, "Missing the required parameter 'orgNamePath' when calling getModule");
        }
        // verify the required parameter 'packageNamePath' is set
        if (packageNamePath == null) {
            throw new ApiException(400, "Missing the required parameter 'packageNamePath' when calling getModule");
        }
        // verify the required parameter 'version' is set
        if (version == null) {
            throw new ApiException(400, "Missing the required parameter 'version' when calling getModule");
        }
        // verify the required parameter 'moduleNamePath' is set
        if (moduleNamePath == null) {
            throw new ApiException(400, "Missing the required parameter 'moduleNamePath' when calling getModule");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/packages/{orgNamePath}/{packageNamePath}/{version}/{moduleNamePath}"
                .replace("{orgNamePath}", ApiClient.urlEncode(orgNamePath.toString()))
                .replace("{packageNamePath}", ApiClient.urlEncode(packageNamePath.toString()))
                .replace("{version}", ApiClient.urlEncode(version.toString()))
                .replace("{moduleNamePath}", ApiClient.urlEncode(moduleNamePath.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        try {
            localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
            if (memberVarReadTimeout != null) {
                localVarRequestBuilder.timeout(memberVarReadTimeout);
            }
            if (memberVarInterceptor != null) {
                memberVarInterceptor.accept(localVarRequestBuilder);
            }
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient
                    .send(localVarRequestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            if (localVarResponse.statusCode() / 100 != 2) {
                throw new ApiException(localVarResponse.statusCode(), "getModule call received non-success response",
                        localVarResponse.headers(),
                        localVarResponse.body() == null ? null : new String(localVarResponse.body().readAllBytes()));
            }
            try (Reader reader = new InputStreamReader(localVarResponse.body(), StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, ModuleJsonSchema.class);
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    /**
     * Get package with version
     *
     * @param orgNamePath     The organization name of the package. (required)
     * @param packageNamePath The name of the package. (required)
     * @param version         The version or version range of the module. (required)
     * @return PackageJsonSchema
     * @throws ApiException if fails to make API call
     */
    public PackageJsonSchema getPackage(String orgNamePath, String packageNamePath, String version)
            throws ApiException {
        // verify the required parameter 'orgNamePath' is set
        if (orgNamePath == null) {
            throw new ApiException(400, "Missing the required parameter 'orgNamePath' when calling getPackage");
        }
        // verify the required parameter 'packageNamePath' is set
        if (packageNamePath == null) {
            throw new ApiException(400, "Missing the required parameter 'packageNamePath' when calling getPackage");
        }
        // verify the required parameter 'version' is set
        if (version == null) {
            throw new ApiException(400, "Missing the required parameter 'version' when calling getPackage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/packages/{orgNamePath}/{packageNamePath}/{version}"
                .replace("{orgNamePath}", ApiClient.urlEncode(orgNamePath.toString()))
                .replace("{packageNamePath}", ApiClient.urlEncode(packageNamePath.toString()))
                .replace("{version}", ApiClient.urlEncode(version.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        try {
            localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
            if (memberVarReadTimeout != null) {
                localVarRequestBuilder.timeout(memberVarReadTimeout);
            }
            if (memberVarInterceptor != null) {
                memberVarInterceptor.accept(localVarRequestBuilder);
            }
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient
                    .send(localVarRequestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            if (localVarResponse.statusCode() / 100 != 2) {
                throw new ApiException(localVarResponse.statusCode(), "getPackage call received non-success response",
                        localVarResponse.headers(),
                        localVarResponse.body() == null ? null : new String(localVarResponse.body().readAllBytes()));
            }
            try (Reader reader = new InputStreamReader(localVarResponse.body(), StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, PackageJsonSchema.class);
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    /**
     * Get the versions package
     *
     * @param orgNamePath     The organization name of the package. (required)
     * @param packageNamePath The name of the package. (required)
     * @return List&lt;String&gt;
     * @throws ApiException if fails to make API call
     */
    public List<String> getPackageVersions(String orgNamePath, String packageNamePath) throws ApiException {
        // verify the required parameter 'orgNamePath' is set
        if (orgNamePath == null) {
            throw new ApiException(400, "Missing the required parameter 'orgNamePath' when calling getPackageVersions");
        }
        // verify the required parameter 'packageNamePath' is set
        if (packageNamePath == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'packageNamePath' when calling getPackageVersions");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/packages/{orgNamePath}/{packageNamePath}"
                .replace("{orgNamePath}", ApiClient.urlEncode(orgNamePath.toString()))
                .replace("{packageNamePath}", ApiClient.urlEncode(packageNamePath.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        try {
            localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
            if (memberVarReadTimeout != null) {
                localVarRequestBuilder.timeout(memberVarReadTimeout);
            }
            if (memberVarInterceptor != null) {
                memberVarInterceptor.accept(localVarRequestBuilder);
            }
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient
                    .send(localVarRequestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            if (localVarResponse.statusCode() / 100 != 2) {
                throw new ApiException(localVarResponse.statusCode(),
                        "getPackageVersions call received non-success response", localVarResponse.headers(),
                        localVarResponse.body() == null ? null : new String(localVarResponse.body().readAllBytes()));
            }
            try (Reader reader = new InputStreamReader(localVarResponse.body(), StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, new TypeToken<List<String>>() {
                }.getType());
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    /**
     * Get stats for package
     *
     * @param orgNamePath     The organization name of the package. (required)
     * @param packageNamePath The name of the package. (required)
     * @param version         The version or version range of the module. (required)
     * @return PackageStatsJsonSchema
     * @throws ApiException if fails to make API call
     */
    public PackageStatsJsonSchema getPullStatsForPackage(String orgNamePath, String packageNamePath, String version)
            throws ApiException {
        // verify the required parameter 'orgNamePath' is set
        if (orgNamePath == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'orgNamePath' when calling getPullStatsForPackage");
        }
        // verify the required parameter 'packageNamePath' is set
        if (packageNamePath == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'packageNamePath' when calling getPullStatsForPackage");
        }
        // verify the required parameter 'version' is set
        if (version == null) {
            throw new ApiException(400, "Missing the required parameter 'version' when calling getPullStatsForPackage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/packages/stats/{orgNamePath}/{packageNamePath}/{version}"
                .replace("{orgNamePath}", ApiClient.urlEncode(orgNamePath.toString()))
                .replace("{packageNamePath}", ApiClient.urlEncode(packageNamePath.toString()))
                .replace("{version}", ApiClient.urlEncode(version.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        try {
            localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
            if (memberVarReadTimeout != null) {
                localVarRequestBuilder.timeout(memberVarReadTimeout);
            }
            if (memberVarInterceptor != null) {
                memberVarInterceptor.accept(localVarRequestBuilder);
            }
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient
                    .send(localVarRequestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            if (localVarResponse.statusCode() / 100 != 2) {
                throw new ApiException(localVarResponse.statusCode(),
                        "getPullStatsForPackage call received non-success response", localVarResponse.headers(),
                        localVarResponse.body() == null ? null : new String(localVarResponse.body().readAllBytes()));
            }
            try (Reader reader = new InputStreamReader(localVarResponse.body(), StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, PackageStatsJsonSchema.class);
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    /**
     * Pushing a package to registry.
     *
     * @param body *.balo file. (required)
     * @throws ApiException if fails to make API call
     */
    public void pushPackage(File body) throws ApiException {
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling pushPackage");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/packages";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = Files.readAllBytes(body.toPath());
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
            if (memberVarReadTimeout != null) {
                localVarRequestBuilder.timeout(memberVarReadTimeout);
            }
            if (memberVarInterceptor != null) {
                memberVarInterceptor.accept(localVarRequestBuilder);
            }
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient
                    .send(localVarRequestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            if (localVarResponse.statusCode() / 100 != 2) {
                throw new ApiException(localVarResponse.statusCode(), "pushPackage call received non-success response",
                        localVarResponse.headers(),
                        localVarResponse.body() == null ? null : new String(localVarResponse.body().readAllBytes()));
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    /**
     * Resolve module versions
     *
     * @param resolvePackagesJsonSchema (optional)
     * @return ResolvedPackagesJsonSchema
     * @throws ApiException if fails to make API call
     */
    public ResolvedPackagesJsonSchema resolvePackageDependencies(ResolvePackagesJsonSchema resolvePackagesJsonSchema)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/resolve-dependencies";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = toByteArray(resolvePackagesJsonSchema);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
            if (memberVarReadTimeout != null) {
                localVarRequestBuilder.timeout(memberVarReadTimeout);
            }
            if (memberVarInterceptor != null) {
                memberVarInterceptor.accept(localVarRequestBuilder);
            }
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient
                    .send(localVarRequestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            if (localVarResponse.statusCode() / 100 != 2) {
                throw new ApiException(localVarResponse.statusCode(),
                        "resolvePackageDependencies call received non-success response", localVarResponse.headers(),
                        localVarResponse.body() == null ? null : new String(localVarResponse.body().readAllBytes()));
            }
            try (Reader reader = new InputStreamReader(localVarResponse.body(), StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, ResolvedPackagesJsonSchema.class);
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    /**
     * Search packages in registry
     *
     * @param limit            The number of packages in the result. (optional)
     * @param offset           Offset for the results. (optional)
     * @param sort             Sort order. (optional, default to &quot;null&quot;)
     * @param org              Search by organization name. (optional)
     * @param module           Search by package name. (optional)
     * @param ballerinaVersion Search by ballerina version. (optional)
     * @param platform         Search by package platform. (optional, default to &quot;null&quot;)
     * @param description      Search by module.md/description. (optional)
     * @param template         Search by whether a package is a template or not. (optional)
     * @param keyword          Search by keywords. (optional, default to &quot;null&quot;)
     * @param myPackage        Filter modules by current user.  An empty array is returned if anonymous user.
     *                         (optional, default to false)
     * @return List&lt;ModuleJsonSchema&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ModuleJsonSchema> searchPackage(Integer limit, Integer offset, String sort, String org, String module,
            String ballerinaVersion, String platform, String description, Boolean template, String keyword,
            Boolean myPackage) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/packages";

        List<Pair> localVarQueryParams = new ArrayList<>();
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParams.addAll(ApiClient.parameterToPairs("offset", offset));
        localVarQueryParams.addAll(ApiClient.parameterToPairs("sort", sort));
        localVarQueryParams.addAll(ApiClient.parameterToPairs("org", org));
        localVarQueryParams.addAll(ApiClient.parameterToPairs("module", module));
        localVarQueryParams.addAll(ApiClient.parameterToPairs("ballerina_version", ballerinaVersion));
        localVarQueryParams.addAll(ApiClient.parameterToPairs("platform", platform));
        localVarQueryParams.addAll(ApiClient.parameterToPairs("description", description));
        localVarQueryParams.addAll(ApiClient.parameterToPairs("template", template));
        localVarQueryParams.addAll(ApiClient.parameterToPairs("keyword", keyword));
        localVarQueryParams.addAll(ApiClient.parameterToPairs("my-package", myPackage));

        if (!localVarQueryParams.isEmpty()) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/json");

        try {
            localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
            if (memberVarReadTimeout != null) {
                localVarRequestBuilder.timeout(memberVarReadTimeout);
            }
            if (memberVarInterceptor != null) {
                memberVarInterceptor.accept(localVarRequestBuilder);
            }
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient
                    .send(localVarRequestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            if (localVarResponse.statusCode() / 100 != 2) {
                throw new ApiException(localVarResponse.statusCode(),
                        "searchPackage call received non-success response", localVarResponse.headers(),
                        localVarResponse.body() == null ? null : new String(localVarResponse.body().readAllBytes()));
            }
            try (Reader reader = new InputStreamReader(localVarResponse.body(), StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, new TypeToken<List<ModuleJsonSchema>>() {
                }.getType());
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    static byte[] toByteArray(Object object) {
        ObjectOutputStream out;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("object to byte array error:" + e.getMessage(), e);
        }
    }
}
