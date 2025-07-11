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
    Boolean withCodeModifiers;
    Boolean configSchemaGen;
    Boolean exportOpenAPI;
    Boolean exportComponentModel;
    Boolean enableCache;
    Boolean disableSyntaxTree;
    Boolean remoteManagement;
    Boolean optimizeDependencyCompilation;
    String lockingMode;

    CompilationOptions(Boolean offlineBuild, Boolean experimental,
                       Boolean observabilityIncluded, Boolean dumpBir, Boolean dumpBirFile,
                       String cloud, Boolean listConflictedClasses, Boolean sticky,
                       Boolean dumpGraph, Boolean dumpRawGraphs, Boolean withCodeGenerators,
                       Boolean withCodeModifiers, Boolean configSchemaGen, Boolean exportOpenAPI,
                       Boolean exportComponentModel, Boolean enableCache, Boolean disableSyntaxTree,
                       Boolean remoteManagement, Boolean optimizeDependencyCompilation, String lockingMode) {
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
        this.withCodeModifiers = withCodeModifiers;
        this.configSchemaGen = configSchemaGen;
        this.exportOpenAPI = exportOpenAPI;
        this.exportComponentModel = exportComponentModel;
        this.enableCache = enableCache;
        this.disableSyntaxTree = disableSyntaxTree;
        this.remoteManagement = remoteManagement;
        this.optimizeDependencyCompilation = optimizeDependencyCompilation;
        this.lockingMode = lockingMode;
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

    public boolean withCodeModifiers() {
        return toBooleanDefaultIfNull(this.withCodeModifiers);
    }

    public Boolean configSchemaGen() {
        return toBooleanDefaultIfNull(this.configSchemaGen);
    }

    public boolean exportOpenAPI() {
        return toBooleanDefaultIfNull(this.exportOpenAPI);
    }

    public boolean exportComponentModel() {
        return toBooleanDefaultIfNull(this.exportComponentModel);
    }

    public boolean enableCache() {
        return toBooleanDefaultIfNull(this.enableCache);
    }

    boolean remoteManagement() {
        return toBooleanDefaultIfNull(this.remoteManagement);
    }

    boolean optimizeDependencyCompilation() {
        return toBooleanDefaultIfNull(this.optimizeDependencyCompilation);
    }

    public String lockingMode() {
        return toStringDefaultIfNull(this.lockingMode);
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
        if (theirOptions.withCodeModifiers != null) {
            compilationOptionsBuilder.withCodeModifiers(theirOptions.withCodeModifiers);
        } else {
            compilationOptionsBuilder.withCodeModifiers(this.withCodeModifiers);
        }
        if (theirOptions.configSchemaGen != null) {
            compilationOptionsBuilder.setConfigSchemaGen(theirOptions.configSchemaGen);
        } else {
            compilationOptionsBuilder.setConfigSchemaGen(this.configSchemaGen);
        }
        if (theirOptions.exportOpenAPI != null) {
            compilationOptionsBuilder.setExportOpenAPI(theirOptions.exportOpenAPI);
        } else {
            compilationOptionsBuilder.setExportOpenAPI(this.exportOpenAPI);
        }
        if (theirOptions.exportComponentModel != null) {
            compilationOptionsBuilder.setExportComponentModel(theirOptions.exportComponentModel);
        } else {
            compilationOptionsBuilder.setExportComponentModel(this.exportComponentModel);
        }
        if (theirOptions.enableCache != null) {
            compilationOptionsBuilder.setEnableCache(theirOptions.enableCache);
        } else {
            compilationOptionsBuilder.setEnableCache(this.enableCache);
        }
        if (theirOptions.remoteManagement != null) {
            compilationOptionsBuilder.setRemoteManagement(theirOptions.remoteManagement);
        } else {
            compilationOptionsBuilder.setRemoteManagement(this.remoteManagement);
        }
        if (theirOptions.optimizeDependencyCompilation != null) {
            compilationOptionsBuilder.setOptimizeDependencyCompilation(theirOptions.optimizeDependencyCompilation);
        } else {
            compilationOptionsBuilder.setOptimizeDependencyCompilation(this.optimizeDependencyCompilation);
        }
        if (theirOptions.lockingMode != null) {
            compilationOptionsBuilder.setLockingMode(theirOptions.lockingMode);
        } else {
            compilationOptionsBuilder.setLockingMode(this.lockingMode);
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

    private String toStringDefaultIfNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    public boolean disableSyntaxTree() {
        return toBooleanDefaultIfNull(this.disableSyntaxTree);
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
        private Boolean withCodeModifiers;
        private Boolean configSchemaGen;
        private Boolean exportOpenAPI;
        private Boolean exportComponentModel;
        private Boolean enableCache;
        private Boolean disableSyntaxTree;
        private Boolean remoteManagement;
        private Boolean optimizeDependencyCompilation;
        // TODO: remove this after fixing https://github.com/ballerina-platform/ballerina-library/issues/7755
        private String lockingMode;

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

        CompilationOptionsBuilder disableSyntaxTree(Boolean value) {
            disableSyntaxTree = value;
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

        CompilationOptionsBuilder withCodeModifiers(Boolean value) {
            withCodeModifiers = value;
            return this;
        }

        CompilationOptionsBuilder setExportOpenAPI(Boolean value) {
            exportOpenAPI = value;
            return this;
        }

        CompilationOptionsBuilder setExportComponentModel(Boolean value) {
            exportComponentModel = value;
            return this;
        }

        public CompilationOptionsBuilder setEnableCache(Boolean value) {
            enableCache = value;
            return this;
        }

        public CompilationOptionsBuilder setRemoteManagement(Boolean value) {
            remoteManagement = value;
            return this;
        }

        public CompilationOptionsBuilder setOptimizeDependencyCompilation(Boolean value) {
            optimizeDependencyCompilation = value;
            return this;
        }

        public CompilationOptionsBuilder setLockingMode(String value) {
            lockingMode = value;
            return this;
        }

        public CompilationOptions build() {
            return new CompilationOptions(offline, experimental, observabilityIncluded, dumpBir,
                    dumpBirFile, cloud, listConflictedClasses, sticky, dumpGraph, dumpRawGraph,
                    withCodeGenerators, withCodeModifiers, configSchemaGen, exportOpenAPI,
                    exportComponentModel, enableCache, disableSyntaxTree, remoteManagement,
                    optimizeDependencyCompilation, lockingMode);
        }
    }
}
