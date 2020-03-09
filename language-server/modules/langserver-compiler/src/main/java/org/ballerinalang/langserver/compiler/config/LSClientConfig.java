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
package org.ballerinalang.langserver.compiler.config;

/**
 * Ballerina Client Configuration.
 */
public class LSClientConfig {
    private final String home;
    private final boolean allowExperimental;
    private final boolean debugLog;
    private final CodeLensConfig codeLens;
    private final boolean traceLog;
    private final GoToDefinitionConfig goToDefinition;

    private LSClientConfig() {
        this.home = "";

        // NOTE: Added reading environmental variables to support IntelliJ plugin
        String balDebugLog = System.getenv("BAL_DEBUG_LOG");
        String balTraceLog = System.getenv("BAL_TRACE_LOG");
        String balExperimental = System.getenv("BAL_EXPERIMENTAL");
        String balDefStdLibs = System.getenv("BAL_DEF_STD_LIBS");

        this.allowExperimental = (balExperimental != null) && Boolean.getBoolean(balExperimental);
        this.debugLog = (balDebugLog != null) && Boolean.getBoolean(balDebugLog);
        this.traceLog = (balTraceLog != null) && Boolean.getBoolean(balTraceLog);
        this.codeLens = new CodeLensConfig();
        this.goToDefinition = (balDefStdLibs != null) ? new GoToDefinitionConfig(Boolean.getBoolean(balDefStdLibs)) :
                new GoToDefinitionConfig(true);
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
     * Returns Goto Definition Config.
     *
     * @return {@link GoToDefinitionConfig}
     */
    public GoToDefinitionConfig getGoToDefinition() {
        return goToDefinition;
    }
}
