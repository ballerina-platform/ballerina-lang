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
 * Ballerina CodeLens Configuration.
 */
public class CodeLensConfig {
    private final Enabled all;
    private final Enabled docs;

    CodeLensConfig() {
        this.all = new Enabled(true);
        this.docs = new Enabled(true);
    }

    /**
     * Returns true if `all` is Enabled, False otherwise.
     *
     * @return True or False
     */
    public Enabled getAll() {
        return all;
    }

    /**
     * Returns true if `all` is Enabled and `docs` is Enabled, False otherwise.
     *
     * @return True or False
     */
    public Enabled getDocs() {
        return (all.isEnabled()) ? docs : all;
    }

    /**
     * Represents a boolean holder to facilitate client configs.
     */
    public static class Enabled {
        private final boolean enabled;

        public Enabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Returns True if enabled, False otherwise.
         *
         * @return True or False
         */
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Enabled)) {
                return false;
            }
            Enabled cc = (Enabled) obj;
            return cc.enabled == enabled;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + ((enabled) ? 1 : 0);
            return result;
        }
    }
}
