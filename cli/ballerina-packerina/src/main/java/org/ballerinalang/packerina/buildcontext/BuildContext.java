/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.buildcontext;

import io.ballerina.runtime.util.BLangConstants;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.sourcecontext.MultiModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType;
import org.ballerinalang.packerina.model.DependencyJar;
import org.ballerinalang.packerina.utils.EmptyPrintStream;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Constants;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BIR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.DIST_BIR_CACHE_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.TARGET_DIR_NAME;
import static org.wso2.ballerinalang.util.RepoUtils.BALLERINA_INSTALL_DIR_PROP;


/**
 * Context to be passed to tasks when they get executed.
 */
public class BuildContext extends HashMap<BuildContextField, Object> {
    private static final long serialVersionUID = 6363519534259706585L;
    private transient Path executableDir;
    private transient Path targetJarCacheDir;
    private transient Path targetBirCacheDir;
    private transient Path targetTestJsonCacheDir;
    private transient Path baloCacheDir;
    private SourceType srcType;
    private transient PrintStream out;
    private transient PrintStream err;
    public transient Map<PackageID, DependencyJar> missedJarMap = new HashMap<>();
    
    /**
     * Create a build context with context fields.
     *
     * @param sourceRootPath  The root of the source files. If its a project then its project root. If its a single bal
     *                        file then is it the parent directory of the bal file.
     * @param targetPath      The location of the target(build artifacts output path).
     * @param source          The name of the source file or the name of the module. Pass null to build all modules.
     * @param compilerContext The compiler context for compiling.
     */
    public BuildContext(Path sourceRootPath, Path targetPath, Path source, CompilerContext compilerContext) {
        try {
            out = new EmptyPrintStream();
            err = new EmptyPrintStream();
            if (Files.exists(sourceRootPath)) {
                // set home repo
                this.put(BuildContextField.HOME_REPO, Paths.get(System.getProperty(BALLERINA_HOME)));
                // set source root
                this.put(BuildContextField.SOURCE_ROOT, sourceRootPath);
                
                // set target dir
                this.put(BuildContextField.TARGET_DIR, targetPath);
                
                // set compiler context
                this.put(BuildContextField.COMPILER_CONTEXT, compilerContext);
                
                // set source context
                this.setSource(source);
                
                // save '<target>/balo' dir for balo files
                this.baloCacheDir = targetPath.resolve(ProjectDirConstants.TARGET_BALO_DIRECTORY);
                
                // save '<target>/cache/bir_cache' dir for bir files
                this.targetBirCacheDir = targetPath
                        .resolve(ProjectDirConstants.CACHES_DIR_NAME)
                        .resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME);
                
                this.put(BuildContextField.BIR_CACHE_DIR, this.targetBirCacheDir);
                
                // save '<target>/cache/jar_cache' dir for jar files
                this.targetJarCacheDir = targetPath
                        .resolve(ProjectDirConstants.CACHES_DIR_NAME)
                        .resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);

                // save '<target>/cache/json_cache' dir for jar files
                this.targetTestJsonCacheDir = targetPath
                        .resolve(ProjectDirConstants.CACHES_DIR_NAME)
                        .resolve(ProjectDirConstants.JSON_CACHE_DIR_NAME);
                
                // save '<target>/bin' dir for executables
                this.executableDir = targetPath.resolve(ProjectDirConstants.BIN_DIR_NAME);
            } else {
                throw new BLangCompilerException("location of the source root does not exists: " + sourceRootPath);
            }
        } catch (UnsupportedEncodingException e) {
            // ignore this error. this error is thrown when creating an empty PrintStream. but it cannot happen.
        }
    }
    
    /**
     * Create a build context with context fields.
     *
     * @param sourceRootPath The root of the source files. If its a project then its project root. If its a single bal
     *                       file then is it the parent directory of the bal file.
     * @param targetPath     The location of the target(build artifacts output path).
     * @param source         The name of the source file or the name of the module. Pass null to build all modules.
     */
    public BuildContext(Path sourceRootPath, Path targetPath, Path source) {
        this(sourceRootPath, targetPath, source, null);
    }
    
    /**
     * Create a build context with context fields.
     *
     * @param sourceRootPath The root of the source files. If its a project then its project root. If its a single bal
     *                       file then is it the parent directory of the bal file.
     * @param source         The name of the source file or the name of the module.
     */
    public BuildContext(Path sourceRootPath, Path source) {
        this(sourceRootPath, sourceRootPath.resolve(TARGET_DIR_NAME), source);
    }
    
    /**
     * Create a build context with context fields.
     *
     * @param sourceRootPath The root of the source files. If its a project then its project root. If its a single bal
     *                       file then is it the parent directory of the bal file.
     */
    public BuildContext(Path sourceRootPath) {
        this(sourceRootPath, null);
    }
    
    /**
     * Set output stream.
     *
     * @param out The output stream.
     */
    public void setOut(PrintStream out) {
        this.out = out;
    }
    
    /**
     * Get the output stream.
     *
     * @return The output stream.
     */
    public PrintStream out() {
        return this.out;
    }
    
    /**
     * Set the error stream.
     *
     * @param err The error stream.
     */
    public void setErr(PrintStream err) {
        this.err = err;
    }
    
    /**
     * Get the error stream.
     *
     * @return The error stream.
     */
    public PrintStream err() {
        return this.err;
    }
    
    /**
     * Get value from the build context.
     *
     * @param key The key of the value.
     * @param <T> The return type.
     * @return The casted value.
     */
    public <T> T get(BuildContextField key) {
        return (T) super.get(key);
    }
    
    /**
     * Sets the type of the source and it's context.
     *
     * @param source The path of the source.
     *               Pass absolute path of the source to build a single file or a module.
     *               Pass null build all modules.
     */
    public void setSource(Path source) {
        if (source == null) {
            this.put(BuildContextField.SOURCE_CONTEXT, new MultiModuleContext());
            this.srcType = SourceType.ALL_MODULES;
        } else {
            Path sourceRootPath = this.get(BuildContextField.SOURCE_ROOT);
            Path absoluteSourcePath = RepoUtils.isBallerinaProject(sourceRootPath) ?
                                      sourceRootPath.toAbsolutePath().resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                                              .resolve(source) :
                                      sourceRootPath.toAbsolutePath().resolve(source);
            
            if (Files.isRegularFile(absoluteSourcePath) &&
                source.toString().endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
                
                this.put(BuildContextField.SOURCE_CONTEXT, new SingleFileContext(source));
                this.srcType = SourceType.SINGLE_BAL_FILE;
            } else if (Files.isDirectory(absoluteSourcePath) &&
                       !source.toString().endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
                
                // this 'if' is to avoid spotbugs
                Path moduleNameAsPath = source.getFileName();
                if (null != moduleNameAsPath) {
                    String moduleName = moduleNameAsPath.toString();
                    this.put(BuildContextField.SOURCE_CONTEXT, new SingleModuleContext(moduleName));
                    this.srcType = SourceType.SINGLE_MODULE;
                }
            } else {
                throw new BLangCompilerException("invalid source type found: '" + source + "' at: " + sourceRootPath);
            }
        }
        
        CompilerContext compilerContext = this.get(BuildContextField.COMPILER_CONTEXT);
        if (compilerContext != null) {
            // set source type as string in compiler context
            CompilerOptions options = CompilerOptions.getInstance(compilerContext);
            options.put(CompilerOptionName.SOURCE_TYPE, this.srcType.toString());
        }
    }
    
    public SourceType getSourceType() {
        return this.srcType;
    }
    
    /**
     * Update the path of executables directory.
     *
     * @param executablesDir Directory of the executables.
     */
    public void updateExecutableDir(Path executablesDir) {
        this.executableDir = executablesDir;
    }
    
    public Path getSystemRepoBirCache() {
        return Paths.get(System.getProperty(BALLERINA_INSTALL_DIR_PROP)).resolve(DIST_BIR_CACHE_DIR_NAME);
    }
    
    public Path getHomeRepoDir() {
        return RepoUtils.createAndGetHomeReposPath();
    }
    
    public Path getBirCacheFromHome() {
        return RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME + "-" +
                                                             RepoUtils.getBallerinaVersion());
    }
    
    public Path getJarCacheFromHome() {
        return RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME + "-" +
                                                             RepoUtils.getBallerinaVersion());
    }

    public Path getBaloCacheFromHome() {
        return RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME);
    }
    
    public List<BLangPackage> getModules() {
        List<BLangPackage> modules = new LinkedList<>();
        switch (this.getSourceType()) {
            case SINGLE_BAL_FILE:
                SingleFileContext singleFileContext = this.get(BuildContextField.SOURCE_CONTEXT);
                modules.add(singleFileContext.getModule());
                break;
            case SINGLE_MODULE:
                SingleModuleContext singleModuleContext = this.get(BuildContextField.SOURCE_CONTEXT);
                modules.add(singleModuleContext.getModule());
                break;
            case ALL_MODULES:
                MultiModuleContext multiModuleContext = this.get(BuildContextField.SOURCE_CONTEXT);
                modules = multiModuleContext.getModules();
                break;
            default:
                throw new BLangCompilerException("unknown source type found: " + this.getSourceType());
        }
        return modules;
    }
    
    public Path getBirPathFromHomeCache(PackageID moduleID) {
        try {
            Path moduleBirCacheDir = Files.createDirectories(getBirCacheFromHome()
                    .resolve(moduleID.orgName.value)
                    .resolve(moduleID.name.value)
                    .resolve(moduleID.version.value));
            return moduleBirCacheDir.resolve(moduleID.name.value + BLANG_COMPILED_PKG_BIR_EXT);
        } catch (IOException e) {
            throw new BLangCompilerException("error resolving bir_cache dir for module: " + moduleID);
        }
    }
    
    public Path getJarPathFromHomeCache(PackageID moduleID) {
        try {
            Path moduleJarCacheDir = Files.createDirectories(getJarCacheFromHome()
                    .resolve(moduleID.orgName.value)
                    .resolve(moduleID.name.value)
                    .resolve(moduleID.version.value));
            return moduleJarCacheDir.resolve(moduleID.name.value + BLANG_COMPILED_JAR_EXT);
        } catch (IOException e) {
            throw new BLangCompilerException("error resolving bir_cache dir for module: " + moduleID);
        }
    }

    public Path getBaloFromHomeCache(PackageID moduleID, String platform) {
        try {
            Path moduleBaloCacheDir = Files.createDirectories(getBaloCacheFromHome()
                    .resolve(moduleID.orgName.value)
                    .resolve(moduleID.name.value)
                    .resolve(moduleID.version.value));
    
            String baloFileName = moduleID.name.value + "-"
                                  + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                  + platform + "-"
                                  + moduleID.version.value
                                  + BLANG_COMPILED_PKG_BINARY_EXT;
            Path baloPath = moduleBaloCacheDir.resolve(baloFileName);
            // check if file exists
            // if not try to load other spec versions
            if (!Files.exists(baloPath) && Files.exists(moduleBaloCacheDir)) {
                Stream<Path> list = Files.list(moduleBaloCacheDir);
                PathMatcher pathMatcher = moduleBaloCacheDir.getFileSystem()
                        .getPathMatcher("glob:**/" + moduleID.name.value + "-*-" +
                                platform + "-" + moduleID.version.value + ".balo");
                for (Path file : (Iterable<Path>) list::iterator) {
                    if (pathMatcher.matches(file)) {
                        return file;
                    }
                }
            }
            return baloPath;
        } catch (IOException e) {
            throw new BLangCompilerException("error resolving balo_cache dir for module: " + moduleID);
        }
    }
    
    public Path getBaloFromTarget(PackageID moduleID) {
        try {
            Files.createDirectories(baloCacheDir);
            switch (this.getSourceType()) {
                case SINGLE_BAL_FILE:
                    throw new BLangCompilerException("balo file for single ballerina files are not supported");
                case SINGLE_MODULE:
                case ALL_MODULES:
                    CompilerContext context = this.get(BuildContextField.COMPILER_CONTEXT);
                    Manifest manifest = ManifestProcessor.getInstance(context).getManifest();
                    
                    // Get the version of the project.
                    String versionNo = manifest.getProject().getVersion();
                    // Identify the platform version
                    String platform = manifest.getTargetPlatform(moduleID.name.value);
                    // {module}-{lang spec version}-{platform}-{version}.balo
                    //+ "2019R2" + ProjectDirConstants.FILE_NAME_DELIMITER
                    String baloFileName = moduleID.name.value + "-"
                                          + ProgramFileConstants.IMPLEMENTATION_VERSION + "-"
                                          + platform + "-"
                                          + versionNo
                                          + BLANG_COMPILED_PKG_BINARY_EXT;
                    
                    return baloCacheDir.resolve(baloFileName);
                default:
                    throw new BLangCompilerException("unable to resolve balo location for build source");
            }
        } catch (IOException e) {
            throw new BLangCompilerException("error creating bir_cache dir for module(s): " + baloCacheDir);
        }
    }
    
    public Path getBirPathFromTargetCache(PackageID moduleID) {
        try {
            Files.createDirectories(targetBirCacheDir);
            switch (this.getSourceType()) {
                case SINGLE_BAL_FILE:
                    SingleFileContext singleFileContext = this.get(BuildContextField.SOURCE_CONTEXT);
                    String birFileName = singleFileContext.getBalFileNameWithoutExtension() +
                                         BLANG_COMPILED_PKG_BIR_EXT;
                    return targetBirCacheDir.resolve(birFileName);
                case SINGLE_MODULE:
                case ALL_MODULES:
                    Path moduleBirCacheDir = Files.createDirectories(targetBirCacheDir
                            .resolve(moduleID.orgName.value)
                            .resolve(moduleID.name.value)
                            .resolve(moduleID.version.value));
                    return moduleBirCacheDir.resolve(moduleID.name.value + BLANG_COMPILED_PKG_BIR_EXT);
                default:
                    throw new BLangCompilerException("unknown source type found: " + this.getSourceType());
            }
        } catch (IOException e) {
            throw new BLangCompilerException("error creating bir_cache dir for module(s): " + targetBirCacheDir);
        }
    }

    public Path getTestBirPathFromTargetCache(PackageID moduleID) {
        try {
            Files.createDirectories(targetBirCacheDir);
            switch (this.getSourceType()) {
                case SINGLE_BAL_FILE:
                    SingleFileContext singleFileContext = this.get(BuildContextField.SOURCE_CONTEXT);
                    String birFileName = singleFileContext.getBalFileNameWithoutExtension() + "-testable" +
                    BLANG_COMPILED_PKG_BIR_EXT;
                    return targetBirCacheDir.resolve(birFileName);
                case SINGLE_MODULE:
                case ALL_MODULES:
                    Path moduleBirCacheDir = Files.createDirectories(targetBirCacheDir
                            .resolve(moduleID.orgName.value)
                            .resolve(moduleID.name.value)
                            .resolve(moduleID.version.value));
                    return moduleBirCacheDir.resolve(moduleID.name.value + "-testable" + BLANG_COMPILED_PKG_BIR_EXT);
                default:
                    throw new BLangCompilerException("unknown source type found: " + this.getSourceType());
            }
        } catch (IOException e) {
            throw new BLangCompilerException("error creating bir_cache dir for module(s): " + targetBirCacheDir);
        }
    }
    
    public Path getJarPathFromTargetCache(PackageID moduleID) {
        try {
            Files.createDirectories(targetJarCacheDir);
            switch (this.getSourceType()) {
                case SINGLE_BAL_FILE:
                    SingleFileContext singleFileContext = this.get(BuildContextField.SOURCE_CONTEXT);
                    String birFileName = singleFileContext.getBalFileNameWithoutExtension() +
                                         BLANG_COMPILED_JAR_EXT;
                    return targetJarCacheDir.resolve(birFileName);
                case SINGLE_MODULE:
                case ALL_MODULES:
                    Path moduleBirCacheDir = Files.createDirectories(targetJarCacheDir
                            .resolve(moduleID.orgName.value)
                            .resolve(moduleID.name.value)
                            .resolve(moduleID.version.value));
                    return moduleBirCacheDir.resolve(moduleID.orgName.value + "-" + 
                                                             moduleID.name.value + "-" +
                                                             moduleID.version.value + BLANG_COMPILED_JAR_EXT);
                default:
                    throw new BLangCompilerException("unknown source type found: " + this.getSourceType());
            }
            
        } catch (IOException e) {
            throw new BLangCompilerException("error creating bir_cache dir for module(s): " + targetJarCacheDir);
        }
    }

    public Path getTestJarPathFromTargetCache(PackageID moduleID) {
        try {
            Files.createDirectories(targetJarCacheDir);
            switch (this.getSourceType()) {
                case SINGLE_BAL_FILE:
                    SingleFileContext singleFileContext = this.get(BuildContextField.SOURCE_CONTEXT);
                    String birFileName = singleFileContext.getBalFileNameWithoutExtension() + "-testable"  +
                            BLANG_COMPILED_JAR_EXT;
                    return targetJarCacheDir.resolve(birFileName);
                case SINGLE_MODULE:
                case ALL_MODULES:
                    Path moduleBirCacheDir = Files.createDirectories(targetJarCacheDir
                            .resolve(moduleID.orgName.value)
                            .resolve(moduleID.name.value)
                            .resolve(moduleID.version.value));
                    return moduleBirCacheDir.resolve(moduleID.orgName.value + "-" +
                            moduleID.name.value + "-" +
                            moduleID.version.value + "-testable" + BLANG_COMPILED_JAR_EXT);
                default:
                    throw new BLangCompilerException("unknown source type found: " + this.getSourceType());
            }

        } catch (IOException e) {
            throw new BLangCompilerException("error creating bir_cache dir for module(s): " + targetJarCacheDir);
        }
    }
    
    public Path getExecutablePathFromTarget(PackageID moduleID) {
        try {
            Files.createDirectories(this.executableDir);
            switch (this.getSourceType()) {
                case SINGLE_BAL_FILE:
                    SingleFileContext singleFileContext = this.get(BuildContextField.SOURCE_CONTEXT);
                    if (null == singleFileContext.getExecutableFilePath()) {
                        String executableFileName = singleFileContext.getBalFileNameWithoutExtension()
                                + BLANG_COMPILED_JAR_EXT;
                        return this.executableDir.resolve(executableFileName);
                    } else {
                        return singleFileContext.getExecutableFilePath();
                    }
                case SINGLE_MODULE:
                case ALL_MODULES:
                    return this.executableDir.resolve(moduleID.name.value + BLANG_COMPILED_JAR_EXT);
                
                default:
                    throw new BLangCompilerException("unable to resolve executable(s) location for build source");
            }
            
        } catch (IOException e) {
            throw new BLangCompilerException("error creating bir_cache dir for module(s): " + this.executableDir);
        }
    }

    public Path getTestJsonPathTargetCache(PackageID moduleID) {
        try {
            Files.createDirectories(targetTestJsonCacheDir);
            switch (this.getSourceType()) {
                case SINGLE_MODULE:
                case ALL_MODULES:
                    return Files.createDirectories(targetTestJsonCacheDir.resolve(moduleID.orgName.value)
                                                           .resolve(moduleID.name.value)
                                                           .resolve(moduleID.version.value));
                default:
                    return targetTestJsonCacheDir;
            }

        } catch (IOException e) {
            throw new BLangCompilerException("error creating test_json_cache " +
                                                     "dir for module(s): " + targetTestJsonCacheDir);
        }
    }
    
    /**
     * Check if the a given dependency is from provided by path to the balo.
     *
     * @param moduleID The modules ID.
     * @return True if given by path, else false.
     */
    public Optional<Dependency> getImportPathDependency(PackageID moduleID) {
        CompilerContext context = this.get(BuildContextField.COMPILER_CONTEXT);
        if (null == context) {
            return Optional.empty();
        }
    
        Manifest manifest = ManifestProcessor.getInstance(context).getManifest();
        return manifest.getDependencies().stream()
                .filter(dep -> dep.getOrgName().equals(moduleID.orgName.value) &&
                               dep.getModuleName().equals(moduleID.name.value) &&
                                null != dep.getMetadata().getPath())
                .findFirst();
    }
    
    /**
     * Get the path to the lock file.
     *
     * @return The path.
     */
    public Path getLockFilePath() {
        Path sourceRootPath = this.get(BuildContextField.SOURCE_ROOT);
        return sourceRootPath.resolve(ProjectDirConstants.LOCK_FILE_NAME);
    }

    public boolean skipTests() {
        CompilerContext context = this.get(BuildContextField.COMPILER_CONTEXT);
        if (context == null) {
            return false;
        }
        CompilerOptions compilerOptions = CompilerOptions.getInstance(context);
        String skipTestsArg = compilerOptions.get(CompilerOptionName.SKIP_TESTS);
        return (skipTestsArg != null && !skipTestsArg.equals(Constants.SKIP_TESTS));
    }
}
