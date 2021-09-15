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
package io.ballerina.projects.environment;

/**
 * Holds the options used by the package resolution components.
 *
 * @since 2.0.0
 */
public class ResolutionOptions {
    private final boolean offline;
    private final boolean sticky;
    private final boolean dumpGraph;
    private final boolean dumpRawGraph;

    private ResolutionOptions(boolean offline, boolean sticky, boolean dumpGraph, boolean dumpRawGraph) {
        this.offline = offline;
        this.sticky = sticky;
        this.dumpGraph = dumpGraph;
        this.dumpRawGraph = dumpRawGraph;
    }

    /**
     * Offline mode indicates whether the resolution system makes network calls to Ballerina central API or not.
     * <p>
     * A `false` value indicates that the system will make connections to Ballerina central.
     * <p>
     * The default value is 'false'.
     *
     * @return true if offline mode is enabled, otherwise false
     */
    public boolean offline() {
        return offline;
    }

    /**
     * If the sticky mode is enabled, the resolution system attempts use the dependency versions
     * recorded in Ballerina.toml as much as possible.
     * <p>
     * The default value is 'true'.
     *
     * @return true if sticky model is enabled, otherwise false
     */
    public boolean sticky() {
        return sticky;
    }

    public boolean dumpGraph() {
        return dumpGraph;
    }

    public boolean dumpRawGraph() {
        return dumpRawGraph;
    }

    public static ResolutionOptionBuilder builder() {
        return new ResolutionOptionBuilder();
    }

    /**
     * A builder for the {@code ResolutionOptions}.
     *
     * @since 2.0.0
     */
    public static class ResolutionOptionBuilder {
        private boolean offline = false;
        private boolean sticky = true;
        private boolean dumpGraph = false;
        private boolean dumpRawGraph = false;

        public ResolutionOptionBuilder setOffline(boolean value) {
            offline = value;
            return this;
        }

        public ResolutionOptionBuilder setSticky(boolean value) {
            sticky = value;
            return this;
        }

        public ResolutionOptionBuilder setDumpGraph(boolean value) {
            dumpGraph = value;
            return this;
        }

        public ResolutionOptionBuilder setDumpRawGraph(boolean value) {
            dumpRawGraph = value;
            return this;
        }

        public ResolutionOptions build() {
            return new ResolutionOptions(offline, sticky, dumpGraph, dumpRawGraph);
        }
    }
}
