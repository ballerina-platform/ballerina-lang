/*
 * Copyright (c) 2026, WSO2 LLC. (http://wso2.com).
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
package org.ballerinalang.oci;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests for {@link OciClient}, run against a minimal fake registry.
 */
class OciClientTest {

    private HttpServer registry;
    private int tagsStatus;
    private String tagsBody;
    private Path balaPath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        balaPath = Files.writeString(tempDir.resolve("pkg-1.0.0.bala"), "bala");
        registry = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        // Answers the tags list as configured by each test; any other call (i.e. the push itself) fails
        registry.createContext("/", exchange -> {
            boolean isTagsList = exchange.getRequestURI().getPath().endsWith("/tags/list");
            int status = isTagsList ? tagsStatus : 500;
            byte[] body = (isTagsList ? tagsBody : "{}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        registry.start();
    }

    @AfterEach
    void tearDown() {
        registry.stop(0);
    }

    @Test
    void pushIsRejectedWhenTheVersionAlreadyExists() {
        respondToTagsList(200, "{\"name\":\"myorg/pkg\",\"tags\":[\"0.9.0\",\"1.0.0\"]}");

        OciClientException exception = Assertions.assertThrows(OciClientException.class, this::push);

        Assertions.assertTrue(exception.getMessage().contains("already exists in the registry"),
                exception.getMessage());
    }

    @Test
    void pushProceedsWhenTheRepositoryDoesNotExistYet() {
        respondToTagsList(404, "{\"errors\":[{\"code\":\"NAME_UNKNOWN\",\"message\":\"repository not found\"}]}");

        OciClientException exception = Assertions.assertThrows(OciClientException.class, this::push);

        // Got past the existence check to the push itself, which the fake registry fails
        Assertions.assertEquals("failed to push OCI artifact to the registry", exception.getMessage());
    }

    @Test
    void pushFailsClosedWhenTheTagsCannotBeListed() {
        respondToTagsList(401, "{\"errors\":[{\"code\":\"UNAUTHORIZED\",\"message\":\"unauthorized\"}]}");

        OciClientException exception = Assertions.assertThrows(OciClientException.class, this::push);

        // Never attempted the push, which could otherwise silently overwrite an existing version
        Assertions.assertTrue(exception.getMessage().startsWith("failed to check whether version '1.0.0'"),
                exception.getMessage());
    }

    @Test
    void distributionIndexTagIsKeyedByUpdate() {
        Assertions.assertEquals("v2201-13-0", OciClient.distributionIndexTag("2201.13.2"));
        Assertions.assertEquals("v2201-14-0", OciClient.distributionIndexTag("2201.14.0-SNAPSHOT"));
        Assertions.assertThrows(OciClientException.class, () -> OciClient.distributionIndexTag("unknown"));
    }

    private void respondToTagsList(int status, String body) {
        this.tagsStatus = status;
        this.tagsBody = body;
    }

    private void push() {
        OciClient client = new OciClient("http://localhost:" + registry.getAddress().getPort(), "", "");
        client.pushOCIArtifact("myorg", "pkg", "1.0.0", "any", "2201.13.0", balaPath);
    }
}
