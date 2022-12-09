/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.projectdesign;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.ballerina.architecturemodelgenerator.ComponentModel;
import io.ballerina.architecturemodelgenerator.ProjectComponentRequest;
import io.ballerina.architecturemodelgenerator.ProjectComponentResponse;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Test project-design-service invocation.
 *
 * @since 2201.3.1
 */
public class ProjectDesignServiceTests {
    private static final Path RES_DIR = Paths.get("src", "test", "resources").toAbsolutePath();
    private static final String BALLERINA = "ballerina";
    private static final String RESULTS = "results";
    private static final String PROJECT_DESIGN_SERVICE = "projectDesignService/getProjectComponentModels";
    Gson gson = new GsonBuilder().serializeNulls().create();


    @Test(description = "test model generation for multimodule project")
    public void testMultiModuleProject() throws IOException, ExecutionException, InterruptedException {
        Path projectPath = RES_DIR.resolve(BALLERINA).resolve(
                Path.of("reservation_api", "reservation_service.bal"));
        Path expectedJsonPath = RES_DIR.resolve(RESULTS).resolve(Path.of("reservation_api_model.json"));

        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        TestUtil.openDocument(serviceEndpoint, projectPath);

        ProjectComponentRequest request = new ProjectComponentRequest();
        request.setDocumentUris(List.of(projectPath.toString()));

        CompletableFuture<?> result = serviceEndpoint.request(PROJECT_DESIGN_SERVICE, request);
        ProjectComponentResponse response = (ProjectComponentResponse) result.get();

        JsonObject generatedJson = response.getComponentModels().get("test/reservation_api:0.1.0");
        ComponentModel generatedModel = gson.fromJson(generatedJson, ComponentModel.class);
        ComponentModel expectedModel = getComponentFromGivenJsonFile(expectedJsonPath);

        generatedModel.getServices().forEach((id, service) -> {
            String generatedService = gson.toJson(service).replaceAll("\\s+", "");
            String expectedService = gson.toJson(expectedModel.getServices().get(id))
                    .replaceAll("\\s+", "")
                    .replaceAll("\\{srcPath}", RES_DIR.toAbsolutePath().toString());
            Assert.assertEquals(generatedService, expectedService);
        });
    }

    @Test(description = "test model generation for multiple projects with grpc and http services")
    public void testGRPCWorkspaceTest() throws IOException, ExecutionException, InterruptedException {

        Path project1 = RES_DIR.resolve(BALLERINA).resolve(
                Path.of("microservice_grpc/cart", "cart_service.bal").toString());

        Path project2 = RES_DIR.resolve(BALLERINA).resolve(
                Path.of("microservice_grpc/checkout", "checkout_service.bal").toString());

        Path project3 = RES_DIR.resolve(BALLERINA).resolve(
                Path.of("microservice_grpc/frontend", "service.bal").toString());

        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();

        ProjectComponentRequest request = new ProjectComponentRequest();
        request.setDocumentUris(List.of(project1.toString(), project2.toString(), project3.toString()));

        CompletableFuture<?> result = serviceEndpoint.request(PROJECT_DESIGN_SERVICE, request);
        ProjectComponentResponse response = (ProjectComponentResponse) result.get();

        response.getComponentModels().forEach((key, value) -> {
            String jsonFileName = key.split("/")[1].split(":")[0] + ".json";
            Path expectedJsonPath = RES_DIR.resolve(RESULTS).resolve(Path.of(jsonFileName));
            ComponentModel generatedModel = gson.fromJson(value, ComponentModel.class);
            try {
                ComponentModel expectedModel = getComponentFromGivenJsonFile(expectedJsonPath.toAbsolutePath());
                generatedModel.getServices().forEach((id, service) -> {
                    String generatedService = gson.toJson(service).replaceAll("\\s+", "");
                    String expectedService = gson.toJson(expectedModel.getServices().get(id))
                            .replaceAll("\\s+", "")
                            .replaceAll("\\{srcPath}", RES_DIR.toAbsolutePath().toString());
                    Assert.assertEquals(generatedService, expectedService);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static ComponentModel getComponentFromGivenJsonFile(Path expectedFilePath) throws IOException {
        Stream<String> lines = Files.lines(expectedFilePath);
        String content = lines.collect(Collectors.joining(System.lineSeparator()));
        lines.close();
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.fromJson(content, ComponentModel.class);
    }
}
