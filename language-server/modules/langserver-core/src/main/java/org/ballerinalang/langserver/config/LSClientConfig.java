/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.config;

/**
 * Ballerina Client Configuration.
 */
public class LSClientConfig {
    private final String home;
    private final boolean allowExperimental;
    private final boolean debugLog;
    private final CodeLensConfig codeLens;
    private final boolean traceLog;
    private final boolean enableFileWatcher;
    private final boolean enableTelemetry;
    private final boolean enableSemanticHighlighting;

    protected LSClientConfig() {
        this.home = "";

        // NOTE: Added reading environmental variables to support IntelliJ plugin
        String balDebugLog = System.getenv("BAL_DEBUG_LOG");
        String balTraceLog = System.getenv("BAL_TRACE_LOG");
        String balExperimental = System.getenv("BAL_EXPERIMENTAL");
        String balFileWatcher = System.getenv("BAL_FILE_WATCHER");
        String balTelemetry = System.getenv("BAL_TELEMETRY");

        this.allowExperimental = Boolean.parseBoolean(balExperimental);
        this.debugLog = Boolean.parseBoolean(balDebugLog);
        this.traceLog = Boolean.parseBoolean(balTraceLog);
        this.codeLens = new CodeLensConfig();
        this.enableFileWatcher = balFileWatcher == null || Boolean.parseBoolean(balFileWatcher);
        this.enableTelemetry = balTelemetry == null || Boolean.parseBoolean(balTelemetry);
        this.enableSemanticHighlighting = true;
    }

    /**
     * Returns default ballerina client configuration.
     *
     * @return {@link LSClientConfig}
     */
    public static LSClientConfig getDefault() {
        return new LSClientConfig();
    }

    /**
     * Returns home.
     *
     * @return home
     */
    public String getHome() {
        return home;
    }

    /**
     * Returns True if allow experimental enabled, False otherwise.
     *
     * @return True if enabled, False otherwise
     */
    public boolean isAllowExperimental() {
        return allowExperimental;
    }

    /**
     * Returns True if allow debug log enabled, False otherwise.
     *
     * @return True if enabled, False otherwise
     */
    public boolean isDebugLogEnabled() {
        return debugLog;
    }

    /**
     * Returns Code Lens Configs.
     *
     * @return {@link CodeLensConfig}
     */
    public CodeLensConfig getCodeLens() {
        return codeLens;
    }

    /**
     * Returns True if trace log enabled, False otherwise.
     *
     * @return True if enabled, False otherwise
     */
    public boolean isTraceLogEnabled() {
        return traceLog;
    }
    
    /**
     * Returns True if file watcher enabled, False otherwise.
     *
     * @return True if enabled, False otherwise
     */
    public boolean isEnableFileWatcher() {
        return enableFileWatcher;
    }

    /**
     * Returns True if ballerina telemetry enabled, False otherwise.
     *
     * @return True if enabled, False otherwise
     */
    public boolean isEnableTelemetry() {
        return enableTelemetry;
    }

    /**
     * Returns True if ballerina semantic highlighting is enabled, False otherwise.
     *
     * @return True if enabled, False otherwise
     */
    public boolean isEnableSemanticHighlighting() {
        return enableSemanticHighlighting;
    }
}
