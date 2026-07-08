/*
 *  Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.harbor;

import com.google.cloud.tools.jib.blob.Blob;
import com.google.cloud.tools.jib.blob.Blobs;
import com.google.cloud.tools.jib.http.*;
import com.google.cloud.tools.jib.image.json.*;
import com.google.cloud.tools.jib.json.JsonTemplateMapper;
import com.google.cloud.tools.jib.registry.RegistryClient;
import com.google.cloud.tools.jib.api.*;
import com.google.cloud.tools.jib.api.buildplan.AbsoluteUnixPath;
import com.google.cloud.tools.jib.api.buildplan.ImageFormat;
import com.google.cloud.tools.jib.event.EventHandlers;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.ballerinalang.central.client.CentralClientConstants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HarborClient {

    private static final String BALA_EXTENSION = ".bala";
    private static final Logger LOGGER = Logger.getLogger(HarborClient.class.getName());

    /** Maximum number of attempts for transient-failure-prone OCI pull operations. */
    private static final int MAX_PULL_RETRIES = 3;

    /** Initial delay in milliseconds before the first retry. Doubles on each subsequent attempt. */
    private static final long INITIAL_RETRY_DELAY_MS = 1000;

    private String harborUrl;
    private String username;
    private String password;

    public HarborClient(String harborUrl, String username, String password) {
        this.harborUrl = harborUrl;
        this.username = username;
        this.password = password;
    }

    /**
     * Executes the given action with retry logic and exponential backoff.
     * <p>
     * OCI registry operations (especially the first pull after startup) can fail transiently
     * due to HTTP→HTTPS failover, auth token exchange latency, or TCP connection warm-up.
     * Retrying with backoff handles these cases gracefully.
     *
     * @param action      the operation to attempt
     * @param operationName a human-readable label used in log messages
     * @throws HarborClientException if all retry attempts are exhausted
     */
    private void withRetry(RunnableWithException action, String operationName) throws Exception {
        int attempt = 0;
        long delayMs = INITIAL_RETRY_DELAY_MS;
        while (true) {
            try {
                action.run();
                return;
            } catch (Exception e) {
                attempt++;
                if (attempt >= MAX_PULL_RETRIES) {
                    throw e;
                }
                LOGGER.log(Level.WARNING,
                        "[HarborClient] {0} failed (attempt {1}/{2}), retrying in {3}ms: {4}",
                        new Object[]{operationName, attempt, MAX_PULL_RETRIES, delayMs, e.getMessage()});
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
                delayMs = Math.min(delayMs * 2, 8000); // cap at 8 s
            }
        }
    }

    /** Functional interface for operations that can throw checked exceptions. */
    @FunctionalInterface
    private interface RunnableWithException {
        void run() throws Exception;
    }

    public void pushOCIArtifact(String org, String pkg, String version, String platform, Path balaFilePath) {
        try {
            if (!balaFilePath.toFile().exists()) {
                return;
            }
            String imageReference = (harborUrl + "/" + org + "/" + platform + "/" + pkg + ":" + version).toLowerCase();
            Jib.fromScratch()
                    .setFormat(ImageFormat.OCI)
                    .addLayer(Collections.singletonList(balaFilePath), AbsoluteUnixPath.get("/"))
                    .containerize(
                            Containerizer.to(RegistryImage.named(imageReference)
                                            .addCredential(username, password))
                                    .setAllowInsecureRegistries(true)
                                    .setToolName("HarborClient")

                    );
        } catch (Exception exception) {
            throw new HarborClientException("failed to push OCI artifact to Harbor", exception);
        }
    }

    public void pullMetadata(String org, String name, String version, String platform, String repoLocation) {
        try {
            withRetry(() -> doPullBala(org, name, version, repoLocation),
                    "pull bala [" + org + "/" + name + ":" + version + "]");
        } catch (Exception exception) {
            throw new HarborClientException("failed to pull bala from the repo", exception);
        }
    }

    /**
     * Core implementation of a single bala pull attempt.
     * Separated from {@link #pullMetadata} so it can be cleanly retried by {@link #withRetry}.
     */
    private void doPullBala(String org, String name, String version, String repoLocation) throws Exception {
        ImageReference imageRef = ImageReference.parse(harborUrl + "/" + org + "/" + name);
        Consumer<LogEvent> jibLogger = logEvent -> {};

        FailoverHttpClient httpClient = new FailoverHttpClient(
                true,
                true,
                jibLogger
        );

        RegistryClient registryClient = RegistryClient.factory(EventHandlers.NONE, imageRef.getRegistry(),
                    imageRef.getRepository(), httpClient)
                    .setCredential(Credential.from(username, password))
                    .newRegistryClient();

        registryClient.configureBasicAuth();

        OciManifestTemplate manifestTemplate = registryClient.pullManifest(version, OciManifestTemplate.class).getManifest();
        String rawJson = JsonTemplateMapper.toUtf8String(manifestTemplate);
        List<BuildableManifestTemplate.ContentDescriptorTemplate> layers = manifestTemplate.getLayers();

        boolean enableOutputStream = Boolean.parseBoolean(
                System.getProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM));
        byte[] blobBytes = null;
        for (BuildableManifestTemplate.ContentDescriptorTemplate layer : layers) {
            final ProgressBar[] progressBar = {null};
            Blob blob = registryClient.pullBlob(
                    layer.getDigest(),
                    size -> {
                        if (enableOutputStream) {
                            // size is -1 when the server uses chunked transfer encoding
                            // (no Content-Length header). Use -1 as initialMax to create
                            // an indeterminate / spinner-style bar in that case.
                            long totalSizeInKB = size > 0 ? (size + 1023) / 1024 : -1;
                            progressBar[0] = new ProgressBar(
                                    org + "/" + name + ":" + version + " [OCI Registry -> " + repoLocation + "]",
                                    totalSizeInKB,
                                    1000,
                                    System.out,
                                    ProgressBarStyle.ASCII,
                                    " KB",
                                    1
                            );
                        }
                    },
                    count -> {
                        // NotifyingOutputStream calls this listener with the byte count
                        // written in the *current chunk* (it resets its counter after each
                        // callback), so `count` is already a delta — not a cumulative total.
                        if (enableOutputStream && progressBar[0] != null && count > 0) {
                            long deltaKB = (count + 1023) / 1024;
                            progressBar[0].stepBy(deltaKB);
                        }
                    }
            );
            blobBytes = Blobs.writeToByteArray(blob);
            if (progressBar[0] != null) {
                progressBar[0].close();
            }
        }

        Path outputPath = Paths.get(repoLocation);
        Path balaFilePath = outputPath.resolve(org).resolve(name).resolve(version)
                .resolve(name + "-" + version + BALA_EXTENSION);
        if (balaFilePath.getParent() != null) {
            Files.createDirectories(balaFilePath.getParent());
        }
        Files.write(balaFilePath, blobBytes);
    }

    public List<String> pullMetadata(String org, String pkg, String platform) {
        try {

            ImageReference imageRef = ImageReference.parse(harborUrl + "/" + org + "/" + pkg);
            Consumer<LogEvent> jibLogger = logEvent -> {};

            FailoverHttpClient httpClient = new FailoverHttpClient(
                    true,
                    true,
                    jibLogger
            );

            RegistryClient registryClient = RegistryClient.factory(EventHandlers.NONE, imageRef.getRegistry(),
                        imageRef.getRepository(), httpClient)
                        .setCredential(Credential.from(username, password))
                        .newRegistryClient();

            registryClient.configureBasicAuth();

            OciManifestTemplate manifestTemplate = registryClient.pullManifest("latest", OciManifestTemplate.class).getManifest();
            String rawJson = JsonTemplateMapper.toUtf8String(manifestTemplate);
            List<BuildableManifestTemplate.ContentDescriptorTemplate> layers = manifestTemplate.getLayers();
            for (BuildableManifestTemplate.ContentDescriptorTemplate layer : layers) {
                com.google.cloud.tools.jib.blob.Blob blob = registryClient.pullBlob(
                        layer.getDigest(),
                        size -> {},
                        count -> {}
                );

                byte[] blobBytes = Blobs.writeToByteArray(blob);
                String text = new String(blobBytes, StandardCharsets.UTF_8);
                Gson gson = new Gson();
                List<String> versions = gson.fromJson(text, new TypeToken<List<String>>(){}.getType());
                return versions;
            }
            return Collections.emptyList();
        } catch (Exception exception) {
            throw new HarborClientException("failed to pull metadata from Harbor", exception);
        }
    }
    
}
