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

import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ProjectEnvironmentContext;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.compiler.CompiledJarFile;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static io.ballerina.projects.util.FileUtils.getFileNameWithoutExtension;

/**
 * This class represents the Ballerina compiler backend that produces executables that runs on the JVM.
 *
 * @since 2.0.0
 */
// TODO move this class to a separate Java package. e.g. io.ballerina.projects.platform.jballerina
//    todo that, we would have to move PackageContext class into an internal package.
public class JBallerinaBackend extends CompilerBackend {

    private final PackageCompilation pkgCompilation;
    private final PackageContext packageContext;
    private final PackageResolver packageResolver;

    public static JBallerinaBackend from(PackageCompilation packageCompilation, JdkVersion jdkVersion) {
        return new JBallerinaBackend(packageCompilation, jdkVersion);
    }

    private JBallerinaBackend(PackageCompilation packageCompilation, JdkVersion jdkVersion) {
        this.pkgCompilation = packageCompilation;
        this.packageContext = packageCompilation.packageContext();

        ProjectEnvironmentContext projectEnvContext = this.packageContext.project().environmentContext();
        this.packageResolver = projectEnvContext.getService(PackageResolver.class);
    }

    public EmitResult emit(OutputType outputType, Path filePath) {
        if (pkgCompilation.hasDiagnostics()) {
            return new EmitResult(false, pkgCompilation.diagnostics());
        }

        switch (outputType) {
            case EXEC:
                emitExecutable(filePath);
                break;
            case JAR:
                emitJar(filePath);
                break;
            case BIR:
                emitBirs(filePath);
                break;
            case BALO:
                emitBalo(filePath);
                break;
            default:
                throw new RuntimeException("Unexpected output type: " + outputType);
        }
        // TODO handle the EmitResult properly
        return new EmitResult(true, pkgCompilation.diagnostics());
    }

    private void emitBalo(Path filePath) {
        BaloWriter.write(packageResolver.getPackage(packageContext.packageId()), filePath);
    }

    private void emitBirs(Path filePath) {
        for (ModuleId moduleId : packageContext.moduleIds()) {
            BLangPackage bLangPackage = packageContext.moduleContext(moduleId).bLangPackage();
            String birName;
            if (packageContext.moduleContext(moduleId).moduleName().isDefaultModuleName()) {
                birName = packageContext.moduleContext(moduleId).moduleName().packageName().toString();
            } else {
                birName = packageContext.moduleContext(moduleId).moduleName().moduleNamePart();
            }
            BirWriter.write(bLangPackage, filePath.resolve(birName + ProjectConstants.BLANG_COMPILED_PKG_BIR_EXT));
        }
    }

    private void emitJar(Path filePath) {
        for (ModuleId moduleId : packageContext.moduleIds()) {
            ModuleContext moduleContext = packageContext.moduleContext(moduleId);
            CompiledJarFile compiledJarFile = moduleContext.compiledJarEntries();
            String jarName;
            String packageName; // this is used to log the error message
            if (packageContext.packageDescriptor().org().anonymous()) {
                DocumentId documentId = moduleContext.srcDocumentIds().iterator().next();
                String documentName = moduleContext.documentContext(documentId).name();
                jarName = getFileNameWithoutExtension(documentName);
                packageName = documentName;
            } else {
                ModuleName moduleName = moduleContext.moduleName();
                if (moduleName.isDefaultModuleName()) {
                    jarName = moduleName.packageName().toString();
                } else {
                    jarName = moduleName.moduleNamePart();
                }
                packageName = this.packageContext.packageName().toString();
            }
            try {
                JarWriter.write(compiledJarFile, filePath.resolve(jarName + ProjectConstants.BLANG_COMPILED_JAR_EXT));
            } catch (IOException e) {
                throw new RuntimeException(
                        "error while creating the jar file for module: " + jarName + " in package: " + packageName, e);
            }
        }
    }

    private void emitExecutable(Path executableFilePath) {
        if (!this.packageContext.defaultModuleContext().entryPointExists()) {
            // TODO Improve error handling
            throw new RuntimeException("no entrypoint found in package: " + this.packageContext.packageName());
        }

        // TODO We need to generate a root package
        CompiledJarFile entryModuleJarEntries = this.packageContext.compiledJarEntries();
        Manifest manifest = getManifest(entryModuleJarEntries);

        List<PackageId> sortedPackageIds = pkgCompilation.packageDependencyGraph().toTopologicallySortedList();
        List<CompiledJarFile> compiledPackageJarList = sortedPackageIds
                .stream()
                .map(packageResolver::getPackage)
                .map(pkg -> pkg.packageContext().compiledJarEntries())
                .collect(Collectors.toList());

        try {
            ProjectUtils.assembleExecutableJar(manifest, compiledPackageJarList, executableFilePath);
        } catch (IOException e) {
            throw new RuntimeException("error while creating the executable jar file for package: " +
                    this.packageContext.packageName(), e);
        }
    }

    private Manifest getManifest(CompiledJarFile entryModuleJarEntries) {
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        String mainClass = entryModuleJarEntries.getMainClassName().orElseThrow(
                () -> new RuntimeException("main class not found in:" + this.packageContext.packageName()));
        mainAttributes.put(Attributes.Name.MAIN_CLASS, mainClass);
        return manifest;
    }

    /**
     * Enum to represent output types.
     */
    public enum OutputType {

        BIR("bir"),
        EXEC("exec"),
        BALO("balo"),
        JAR("jar");

        private String value;

        OutputType(String value) {
            this.value = value;
        }
    }
}
