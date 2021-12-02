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
    Boolean withCodeGenerators;
    Boolean configSchemaGen;
    Boolean exportOpenapi;

    CompilationOptions(Boolean offlineBuild, Boolean experimental,
                       Boolean observabilityIncluded, Boolean dumpBir, Boolean dumpBirFile,
                       String cloud, Boolean listConflictedClasses, Boolean sticky,
                       Boolean dumpGraph, Boolean dumpRawGraphs, Boolean withCodeGenerators, Boolean configSchemaGen,
                       Boolean exportOpenapi) {
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
        this.withCodeGenerators = withCodeGenerators;
        this.configSchemaGen = configSchemaGen;
        this.exportOpenapi = exportOpenapi;
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

    public boolean withCodeGenerators() {
        return toBooleanDefaultIfNull(this.withCodeGenerators);
    }

    public Boolean configSchemaGen() {
        return toBooleanDefaultIfNull(this.configSchemaGen);
    }

    public boolean exportOpenapi() { return toBooleanDefaultIfNull(this.exportOpenapi); }

    /**
     * Merge the given compilation options by favoring theirs if there are conflicts.
     *
     * @param theirOptions Compilation options to be merged
     * @return a new {@code CompilationOptions} instance that contains our options and their options
     */
    CompilationOptions acceptTheirs(CompilationOptions theirOptions) {
        CompilationOptionsBuilder compilationOptionsBuilder = new CompilationOptionsBuilder();
        if (theirOptions.offlineBuild != null) {
            compilationOptionsBuilder.setOffline(theirOptions.offlineBuild);
        } else {
            compilationOptionsBuilder.setOffline(this.offlineBuild);
        }
        if (theirOptions.experimental != null) {
            compilationOptionsBuilder.setExperimental(theirOptions.experimental);
        } else {
            compilationOptionsBuilder.setExperimental(this.experimental);
        }
        if (theirOptions.observabilityIncluded != null) {
            compilationOptionsBuilder.setObservabilityIncluded(theirOptions.observabilityIncluded);
        } else {
            compilationOptionsBuilder.setObservabilityIncluded(this.observabilityIncluded);
        }
        if (theirOptions.dumpBir != null) {
            compilationOptionsBuilder.setDumpBir(theirOptions.dumpBir);
        } else {
            compilationOptionsBuilder.setDumpBir(this.dumpBir);
        }
        if (theirOptions.dumpBirFile != null) {
            compilationOptionsBuilder.setDumpBirFile(theirOptions.dumpBirFile);
        } else {
            compilationOptionsBuilder.setDumpBirFile(this.dumpBirFile);
        }
        if (theirOptions.dumpGraph != null) {
            compilationOptionsBuilder.setDumpGraph(theirOptions.dumpGraph);
        } else {
            compilationOptionsBuilder.setDumpGraph(this.dumpGraph);
        }
        if (theirOptions.dumpRawGraphs != null) {
            compilationOptionsBuilder.setDumpRawGraphs(theirOptions.dumpRawGraphs);
        } else {
            compilationOptionsBuilder.setDumpRawGraphs(this.dumpRawGraphs);
        }
        if (theirOptions.cloud != null) {
            compilationOptionsBuilder.setCloud(theirOptions.cloud);
        } else {
            compilationOptionsBuilder.setCloud(this.cloud);
        }
        if (theirOptions.listConflictedClasses != null) {
            compilationOptionsBuilder.setListConflictedClasses(theirOptions.listConflictedClasses);
        } else {
            compilationOptionsBuilder.setListConflictedClasses(this.listConflictedClasses);
        }
        if (theirOptions.sticky != null) {
            compilationOptionsBuilder.setSticky(theirOptions.sticky);
        } else {
            compilationOptionsBuilder.setSticky(this.sticky);
        }
        if (theirOptions.withCodeGenerators != null) {
            compilationOptionsBuilder.withCodeGenerators(theirOptions.withCodeGenerators);
        } else {
            compilationOptionsBuilder.withCodeGenerators(this.withCodeGenerators);
        }
        if (theirOptions.configSchemaGen != null) {
            compilationOptionsBuilder.setConfigSchemaGen(theirOptions.configSchemaGen);
        } else {
            compilationOptionsBuilder.setConfigSchemaGen(this.configSchemaGen);
        }
        if (theirOptions.exportOpenapi != null) {
            compilationOptionsBuilder.setExportOpenapi(theirOptions.exportOpenapi);
        } else {
            compilationOptionsBuilder.setExportOpenapi(this.exportOpenapi);
        }
        return compilationOptionsBuilder.build();
    }

    public static CompilationOptionsBuilder builder() {
        return new CompilationOptionsBuilder();
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

    /**
     * A builder for the {@code CompilationOptions}.
     *
     * @since 2.0.0
     */
    public static class CompilationOptionsBuilder {
        private Boolean offline;
        private Boolean experimental;
        private Boolean observabilityIncluded;
        private Boolean dumpBir;
        private Boolean dumpBirFile;
        private String cloud;
        private Boolean listConflictedClasses;
        private Boolean sticky;
        private Boolean dumpGraph;
        private Boolean dumpRawGraph;
        private Boolean withCodeGenerators;
        private Boolean configSchemaGen;
        private Boolean exportOpenapi;

        public CompilationOptionsBuilder setOffline(Boolean value) {
            offline = value;
            return this;
        }

        public CompilationOptionsBuilder setSticky(Boolean value) {
            sticky = value;
            return this;
        }

        CompilationOptionsBuilder setExperimental(Boolean value) {
            experimental = value;
            return this;
        }

        CompilationOptionsBuilder setObservabilityIncluded(Boolean value) {
            observabilityIncluded = value;
            return this;
        }

        CompilationOptionsBuilder setDumpBir(Boolean value) {
            dumpBir = value;
            return this;
        }

        CompilationOptionsBuilder setCloud(String value) {
            cloud = value;
            return this;
        }

        CompilationOptionsBuilder setDumpBirFile(Boolean value) {
            dumpBirFile = value;
            return this;
        }

        CompilationOptionsBuilder setDumpGraph(Boolean value) {
            dumpGraph = value;
            return this;
        }

        CompilationOptionsBuilder setDumpRawGraphs(Boolean value) {
            dumpRawGraph = value;
            return this;
        }

        public CompilationOptionsBuilder setConfigSchemaGen(Boolean value) {
            configSchemaGen = value;
            return this;
        }

        CompilationOptionsBuilder setListConflictedClasses(Boolean value) {
            listConflictedClasses = value;
            return this;
        }

        CompilationOptionsBuilder withCodeGenerators(Boolean value) {
            withCodeGenerators = value;
            return this;
        }

        CompilationOptionsBuilder setExportOpenapi (Boolean value) {
            exportOpenapi = value;
            return this;
        }

        public CompilationOptions build() {
            return new CompilationOptions(offline, experimental, observabilityIncluded, dumpBir,
                    dumpBirFile, cloud, listConflictedClasses, sticky,
                    dumpGraph, dumpRawGraph, withCodeGenerators, configSchemaGen, exportOpenapi);
        }
    }
}
