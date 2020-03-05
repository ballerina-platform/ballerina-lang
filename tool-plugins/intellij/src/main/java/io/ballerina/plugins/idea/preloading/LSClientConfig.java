/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package io.ballerina.plugins.idea.preloading;

/**
 * Ballerina Client Configuration.
 *
 * @since 1.2.0
 */
public class LSClientConfig {
    private String home = "";
    private boolean allowExperimental;
    private boolean debugLog;
    private boolean traceLog;
    private GoToDefinitionConfig goToDefinition;

    LSClientConfig() {
        this.home = "";
        this.allowExperimental = false;
        this.debugLog = false;
        this.traceLog = false;
        this.goToDefinition = new GoToDefinitionConfig(false);
    }

    public void setAllowExperimental(boolean allowExperimental) {
        this.allowExperimental = allowExperimental;
    }

    public void setDebugLog(boolean debugLog) {
        this.debugLog = debugLog;
    }

    public void setTraceLog(boolean traceLog) {
        this.traceLog = traceLog;
    }

    public void setGoToDefStdLibs(boolean goToDefStdLib) {
        this.goToDefinition = new GoToDefinitionConfig(goToDefStdLib);
    }
}
