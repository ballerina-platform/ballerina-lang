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
    Boolean offlineBuild;
    Boolean experimental;
    Boolean observabilityIncluded;
    Boolean dumpBir;
    Boolean dumpBirFile;
    String cloud;
    Boolean listConflictedClasses;
    Boolean sticky;
    Boolean dumpGraph;
    Boolean dumpRawGraphs;
    private Boolean semtype;

    CompilationOptions(Boolean offlineBuild, Boolean experimental,
                       Boolean observabilityIncluded, Boolean dumpBir, Boolean dumpBirFile,
                       String cloud, Boolean listConflictedClasses, Boolean sticky,
                       Boolean dumpGraph, Boolean dumpRawGraphs,
                       Boolean semtype) {
        this.offlineBuild = offlineBuild;
        this.experimental = experimental;
        this.observabilityIncluded = observabilityIncluded;
        this.dumpBir = dumpBir;
        this.dumpBirFile = dumpBirFile;
        this.cloud = cloud;
        this.listConflictedClasses = listConflictedClasses;
        this.sticky = sticky;
        this.dumpGraph = dumpGraph;
        this.dumpRawGraphs = dumpRawGraphs;
        this.semtype = semtype;
    }

    public boolean offlineBuild() {
        return toBooleanDefaultIfNull(this.offlineBuild);
    }

    boolean sticky() {
        return toBooleanTrueIfNull(this.sticky);
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

    public Boolean dumpBirFile() {
        return toBooleanDefaultIfNull(this.dumpBirFile);
    }

    public Boolean dumpGraph() {
        return toBooleanDefaultIfNull(this.dumpGraph);
    }

    public Boolean dumpRawGraphs() {
        return toBooleanDefaultIfNull(this.dumpRawGraphs);
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
        CompilationOptionsBuilder compilationOptionsBuilder = new CompilationOptionsBuilder();
        if (theirOptions.offlineBuild != null) {
            compilationOptionsBuilder.offline(theirOptions.offlineBuild);
        } else {
            compilationOptionsBuilder.offline(this.offlineBuild);
        }
        if (theirOptions.experimental != null) {
            compilationOptionsBuilder.experimental(theirOptions.experimental);
        } else {
            compilationOptionsBuilder.experimental(this.experimental);
        }
        if (theirOptions.observabilityIncluded != null) {
            compilationOptionsBuilder.observabilityIncluded(theirOptions.observabilityIncluded);
        } else {
            compilationOptionsBuilder.observabilityIncluded(this.observabilityIncluded);
        }
        if (theirOptions.dumpBir != null) {
            compilationOptionsBuilder.dumpBir(theirOptions.dumpBir);
        } else {
            compilationOptionsBuilder.dumpBir(this.dumpBir);
        }
        if (theirOptions.dumpBirFile != null) {
            compilationOptionsBuilder.dumpBirFile(theirOptions.dumpBirFile);
        } else {
            compilationOptionsBuilder.dumpBirFile(this.dumpBirFile);
        }
        if (theirOptions.dumpGraph != null) {
            compilationOptionsBuilder.dumpGraph(theirOptions.dumpGraph);
        } else {
            compilationOptionsBuilder.dumpGraph(this.dumpGraph);
        }
        if (theirOptions.dumpRawGraphs != null) {
            compilationOptionsBuilder.dumpRawGraphs(theirOptions.dumpRawGraphs);
        } else {
            compilationOptionsBuilder.dumpRawGraphs(this.dumpRawGraphs);
        }
        if (theirOptions.cloud != null) {
            compilationOptionsBuilder.cloud(theirOptions.cloud);
        } else {
            compilationOptionsBuilder.cloud(this.cloud);
        }
        if (theirOptions.listConflictedClasses != null) {
            compilationOptionsBuilder.listConflictedClasses(theirOptions.listConflictedClasses);
        } else {
            compilationOptionsBuilder.listConflictedClasses(this.listConflictedClasses);
        }
        if (theirOptions.sticky != null) {
            compilationOptionsBuilder.sticky(theirOptions.sticky);
        } else {
            compilationOptionsBuilder.sticky(this.sticky);
        }
        if (theirOptions.semtype != null) {
            compilationOptionsBuilder.semtype(theirOptions.semtype);
        } else {
            compilationOptionsBuilder.semtype(this.semtype);
        }
        return compilationOptionsBuilder.build();
    }

    private boolean toBooleanDefaultIfNull(Boolean bool) {
        if (bool == null) {
            return false;
        }
        return bool;
    }

    private boolean toBooleanTrueIfNull(Boolean bool) {
        if (bool == null) {
            return true;
        }
        return bool;
    }

    private String toStringDefaultIfNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    public boolean semtype() {
        return toBooleanDefaultIfNull(this.semtype);
    }
}
