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
package io.ballerina.projects;

/**
 * The class {@code CompilationOptions} holds various Ballerina compilation options.
 *
 * @since 2.0.0
 */
public class CompilationOptions {
    private Boolean offlineBuild;
    private Boolean experimental;
    private Boolean observabilityIncluded;
    private Boolean dumpBir;
    private String dumpBirFile;
    private String cloud;
    private Boolean listConflictedClasses;
    private Boolean sticky;

    public CompilationOptions(Boolean offlineBuild, Boolean experimental,
                              Boolean observabilityIncluded, Boolean dumpBir, String dumpBirFile,
                              String cloud, Boolean listConflictedClasses, Boolean sticky) {
        this.offlineBuild = offlineBuild;
        this.experimental = experimental;
        this.observabilityIncluded = observabilityIncluded;
        this.dumpBir = dumpBir;
        this.dumpBirFile = dumpBirFile;
        this.cloud = cloud;
        this.listConflictedClasses = listConflictedClasses;
        this.sticky = sticky;
    }

    public boolean offlineBuild() {
        return toBooleanDefaultIfNull(this.offlineBuild);
    }

    boolean sticky() {
        return toBooleanDefaultIfNull(this.sticky);
    }

    boolean experimental() {
        return toBooleanDefaultIfNull(this.experimental);
    }

    boolean observabilityIncluded() {
        return toBooleanDefaultIfNull(this.observabilityIncluded);
    }

    public Boolean dumpBir() {
        return toBooleanDefaultIfNull(this.dumpBir);
    }

    public String getBirDumpFile() {
        return this.dumpBirFile;
    }

    public String getCloud() {
        return toStringDefaultIfNull(this.cloud);
    }

    public boolean listConflictedClasses() {
        return toBooleanDefaultIfNull(this.listConflictedClasses);
    }

    /**
     * Merge the given compilation options by favoring theirs if there are conflicts.
     *
     * @param theirOptions Compilation options to be merged
     * @return a new {@code CompilationOptions} instance that contains our options and their options
     */
    CompilationOptions acceptTheirs(CompilationOptions theirOptions) {
        if (theirOptions.offlineBuild != null) {
            this.offlineBuild = theirOptions.offlineBuild;
        }
        if (theirOptions.experimental != null) {
            this.experimental = theirOptions.experimental;
        }
        if (theirOptions.observabilityIncluded != null) {
            this.observabilityIncluded = theirOptions.observabilityIncluded;
        }
        if (theirOptions.dumpBir != null) {
            this.dumpBir = theirOptions.dumpBir;
        }
        if (theirOptions.cloud != null) {
            this.cloud = theirOptions.cloud;
        }
        this.dumpBirFile = theirOptions.dumpBirFile;
        if (theirOptions.listConflictedClasses != null) {
            this.listConflictedClasses = theirOptions.listConflictedClasses;
        }
        if (theirOptions.sticky != null) {
            this.sticky = theirOptions.sticky;
        }
        return this;
    }

    private boolean toBooleanDefaultIfNull(Boolean bool) {
        if (bool == null) {
            return false;
        }
        return bool;
    }

    private String toStringDefaultIfNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
