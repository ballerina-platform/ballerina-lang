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

import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * Represents the various compilation stages of a Ballerina module.
 *
 * @since 2.0.0
 */
enum ModuleCompilationState {
    LOADED_FROM_SOURCES {
        @Override
        void parse(ModuleContext moduleContext) {
            ModuleContext.parseInternal(moduleContext);
            moduleContext.setCompilationState(PARSED);
        }

        @Override
        void resolveDependencies(ModuleContext moduleContext) {
            parse(moduleContext);
            ModuleContext.resolveDependenciesInternal(moduleContext);
            moduleContext.setCompilationState(DEPENDENCIES_RESOLVED_FROM_SOURCES);
        }

        @Override
        void compile(ModuleContext moduleContext, CompilerContext compilerContext) {
            resolveDependencies(moduleContext);
            ModuleContext.compileInternal(moduleContext, compilerContext);
            moduleContext.setCompilationState(COMPILED);
        }

        @Override
        void generatePlatformSpecificCode(ModuleContext moduleContext,
                                          CompilerContext compilerContext,
                                          CompilerBackend compilerBackend) {
            compile(moduleContext, compilerContext);
            ModuleContext.generateCodeInternal(moduleContext, compilerBackend, compilerContext);
            moduleContext.setCompilationState(PLATFORM_LIBRARY_GENERATED);
        }
    },
    PARSED {
        @Override
        void parse(ModuleContext moduleContext) {
            // TODO how about illegal state transition
            // Do nothing
        }

        @Override
        void resolveDependencies(ModuleContext moduleContext) {
            ModuleContext.resolveDependenciesInternal(moduleContext);
            moduleContext.setCompilationState(DEPENDENCIES_RESOLVED_FROM_SOURCES);
        }

        @Override
        void compile(ModuleContext moduleContext, CompilerContext compilerContext) {
            resolveDependencies(moduleContext);
            ModuleContext.compileInternal(moduleContext, compilerContext);
            moduleContext.setCompilationState(COMPILED);
        }

        @Override
        void generatePlatformSpecificCode(ModuleContext moduleContext,
                                          CompilerContext compilerContext,
                                          CompilerBackend compilerBackend) {
            compile(moduleContext, compilerContext);
            ModuleContext.generateCodeInternal(moduleContext, compilerBackend, compilerContext);
            moduleContext.setCompilationState(PLATFORM_LIBRARY_GENERATED);
        }
    },
    DEPENDENCIES_RESOLVED_FROM_SOURCES {
        @Override
        void parse(ModuleContext moduleContext) {
            // Do nothing
        }

        @Override
        void resolveDependencies(ModuleContext moduleContext) {
            // Do nothing
        }

        @Override
        void compile(ModuleContext moduleContext, CompilerContext compilerContext) {
            ModuleContext.compileInternal(moduleContext, compilerContext);
            moduleContext.setCompilationState(COMPILED);
        }

        @Override
        void generatePlatformSpecificCode(ModuleContext moduleContext,
                                          CompilerContext compilerContext,
                                          CompilerBackend compilerBackend) {
            compile(moduleContext, compilerContext);
            ModuleContext.generateCodeInternal(moduleContext, compilerBackend, compilerContext);
            moduleContext.setCompilationState(PLATFORM_LIBRARY_GENERATED);
        }
    },
    COMPILED {
        @Override
        void parse(ModuleContext moduleContext) {
            // Do nothing
        }

        @Override
        void resolveDependencies(ModuleContext moduleContext) {
            // Do nothing
        }

        @Override
        void compile(ModuleContext moduleContext, CompilerContext compilerContext) {
        }

        @Override
        void generatePlatformSpecificCode(ModuleContext moduleContext,
                                          CompilerContext compilerContext,
                                          CompilerBackend compilerBackend) {
            ModuleContext.generateCodeInternal(moduleContext, compilerBackend, compilerContext);
            moduleContext.setCompilationState(PLATFORM_LIBRARY_GENERATED);
        }
    },
    PLATFORM_LIBRARY_GENERATED {
        @Override
        void parse(ModuleContext moduleContext) {
            // Do nothing
        }

        @Override
        void resolveDependencies(ModuleContext moduleContext) {
            // Do nothing
        }

        @Override
        void compile(ModuleContext moduleContext, CompilerContext compilerContext) {
            // Do nothing
        }

        @Override
        void generatePlatformSpecificCode(ModuleContext moduleContext,
                                          CompilerContext compilerContext,
                                          CompilerBackend compilerBackend) {
            // Do nothing
        }
    },

    LOADED_FROM_CACHE {
        @Override
        void parse(ModuleContext moduleContext) {
            ModuleContext.loadBirBytesInternal(moduleContext);
            moduleContext.setCompilationState(BIR_LOADED);
        }

        @Override
        void resolveDependencies(ModuleContext moduleContext) {
            parse(moduleContext);
            // TODO We need to use the dependencies declared in the package descriptor
            ModuleContext.resolveDependenciesFromBALOInternal(moduleContext);
            moduleContext.setCompilationState(DEPENDENCIES_RESOLVED_FROM_BALO);
        }

        @Override
        void compile(ModuleContext moduleContext, CompilerContext compilerContext) {
            resolveDependencies(moduleContext);
            ModuleContext.loadPackageSymbolInternal(moduleContext, compilerContext);
            moduleContext.setCompilationState(MODULE_SYMBOL_LOADED);
        }

        @Override
        void generatePlatformSpecificCode(ModuleContext moduleContext,
                                          CompilerContext compilerContext,
                                          CompilerBackend compilerBackend) {
            compile(moduleContext, compilerContext);
            ModuleContext.loadPlatformSpecificCodeInternal(moduleContext, compilerBackend);
            moduleContext.setCompilationState(PLATFORM_LIBRARY_LOADED);
        }
    },
    BIR_LOADED {
        @Override
        void parse(ModuleContext moduleContext) {
            // Do nothing
        }

        @Override
        void resolveDependencies(ModuleContext moduleContext) {
            ModuleContext.resolveDependenciesFromBALOInternal(moduleContext);
            moduleContext.setCompilationState(DEPENDENCIES_RESOLVED_FROM_BALO);
        }

        @Override
        void compile(ModuleContext moduleContext, CompilerContext compilerContext) {
            resolveDependencies(moduleContext);
            ModuleContext.loadPackageSymbolInternal(moduleContext, compilerContext);
            moduleContext.setCompilationState(MODULE_SYMBOL_LOADED);
        }

        @Override
        void generatePlatformSpecificCode(ModuleContext moduleContext,
                                          CompilerContext compilerContext,
                                          CompilerBackend compilerBackend) {
            compile(moduleContext, compilerContext);
            ModuleContext.loadPlatformSpecificCodeInternal(moduleContext, compilerBackend);
            moduleContext.setCompilationState(PLATFORM_LIBRARY_LOADED);
        }
    },
    DEPENDENCIES_RESOLVED_FROM_BALO {
        @Override
        void parse(ModuleContext moduleContext) {
            // Do nothing
        }

        @Override
        void resolveDependencies(ModuleContext moduleContext) {
            // Do nothing
        }

        @Override
        void compile(ModuleContext moduleContext, CompilerContext compilerContext) {
            ModuleContext.loadPackageSymbolInternal(moduleContext, compilerContext);
            moduleContext.setCompilationState(MODULE_SYMBOL_LOADED);
        }

        @Override
        void generatePlatformSpecificCode(ModuleContext moduleContext,
                                          CompilerContext compilerContext,
                                          CompilerBackend compilerBackend) {
            compile(moduleContext, compilerContext);
            ModuleContext.loadPlatformSpecificCodeInternal(moduleContext, compilerBackend);
            moduleContext.setCompilationState(PLATFORM_LIBRARY_LOADED);
        }
    },
    MODULE_SYMBOL_LOADED {
        @Override
        void parse(ModuleContext moduleContext) {
            // Do nothing
        }

        @Override
        void resolveDependencies(ModuleContext moduleContext) {
            // Do nothing
        }

        @Override
        void compile(ModuleContext moduleContext, CompilerContext compilerContext) {
            // Do nothing
        }

        @Override
        void generatePlatformSpecificCode(ModuleContext moduleContext,
                                          CompilerContext compilerContext,
                                          CompilerBackend compilerBackend) {
            ModuleContext.loadPlatformSpecificCodeInternal(moduleContext, compilerBackend);
            moduleContext.setCompilationState(PLATFORM_LIBRARY_LOADED);
        }
    },
    PLATFORM_LIBRARY_LOADED {
        @Override
        void parse(ModuleContext moduleContext) {
            // Do nothing
        }

        @Override
        void resolveDependencies(ModuleContext moduleContext) {
            // Do nothing
        }

        @Override
        void compile(ModuleContext moduleContext, CompilerContext compilerContext) {
            // Do nothing
        }

        @Override
        void generatePlatformSpecificCode(ModuleContext moduleContext,
                                          CompilerContext compilerContext,
                                          CompilerBackend compilerBackend) {
            // Do nothing
        }
    };

    abstract void parse(ModuleContext moduleContext);

    abstract void resolveDependencies(ModuleContext moduleContext);

    abstract void compile(ModuleContext moduleContext, CompilerContext compilerContext);

    abstract void generatePlatformSpecificCode(ModuleContext moduleContext,
                                               CompilerContext compilerContext,
                                               CompilerBackend compilerBackend);
}
