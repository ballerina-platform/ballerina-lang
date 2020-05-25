package org.ballerinalang.packerina;

import com.moandjiezana.toml.Toml;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.Library;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.JarResolver;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALO_PLATFORM_LIB_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.DIST_BIR_CACHE_DIR_NAME;

/**
 * Resolves jars and native libs for modules.
 *
 * @since 1.3.0
 */
public class JarResolverImpl implements JarResolver {
    private static final CompilerContext.Key<JarResolverImpl> JAR_RESOLVER_KEY = new CompilerContext.Key<>();
    private List<String> supportedPlatforms = Arrays.stream(ProgramFileConstants.SUPPORTED_PLATFORMS)
            .collect(Collectors.toList());
    private final BuildContext buildContext;
    private final Path sourceRootPath;
    private final String balHomePath;
    private final Manifest manifest;
    private boolean skipCopyLibsFromDist;

    public static JarResolverImpl getInstance(BuildContext buildContext, boolean skipCopyLibsFromDist) {
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        JarResolverImpl jarResolver = context.get(JAR_RESOLVER_KEY);
        if (jarResolver == null) {
            jarResolver = new JarResolverImpl(buildContext, skipCopyLibsFromDist);
        }
        context.put(JAR_RESOLVER_KEY, jarResolver);
        return jarResolver;
    }

    private JarResolverImpl(BuildContext buildContext, boolean skipCopyLibsFromDist) {
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        this.buildContext = buildContext;
        this.manifest = ManifestProcessor.getInstance(context).getManifest();
        this.sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
        this.balHomePath = buildContext.get(BuildContextField.HOME_REPO).toString();
        supportedPlatforms.add("any");
    }

    @Override
    public Path moduleJar(PackageID packageID, String platform) {

        Optional<Dependency> importPathDependency = buildContext.getImportPathDependency(packageID);
        // Look if it is a project module.
        if (manifest.getProject().getOrgName().equals(packageID.orgName.value) &&
                ProjectDirs.isModuleExist(sourceRootPath, packageID.name.value)) {
            // If so fetch from project balo cache
            return buildContext.getBaloFromTarget(packageID);
        } else if (importPathDependency.isPresent()) {
            return importPathDependency.get().getMetadata().getPath();
        } else if (getTomlFilePath(packageID).exists()) {
            return getJarFromDist(packageID);
        } else {
            // If not fetch from home balo cache.
            return buildContext.getBaloFromHomeCache(packageID, platform);
        }
    }

    @Override
    public List<Path> nativeLibraries(PackageID packageID) {
        Optional<Dependency> importPathDependency = buildContext.getImportPathDependency(packageID);
        List<Path> modulePlatformLibs = new ArrayList<>();
        // copy platform libs for all modules(imported modules as well)
        getPlatformLibs(packageID, modulePlatformLibs);

        if (manifest.getProject().getOrgName().equals(packageID.orgName.value) &&
                ProjectDirs.isModuleExist(sourceRootPath, packageID.name.value)) {
            return modulePlatformLibs;
        } else if (importPathDependency.isPresent()) {
            getLibsFromBalo(importPathDependency.get().getMetadata().getPath(), modulePlatformLibs);
        } else if (getTomlFilePath(packageID).exists()) {
            List<Path> dependencies = getDependenciesFromDist(packageID);
            if (dependencies != null) {
                modulePlatformLibs.addAll(dependencies);
            }
        } else {
            for (String platform : supportedPlatforms) {
                Path importJar = buildContext.getBaloFromHomeCache(packageID, platform);
                if (importJar != null && Files.exists(importJar)) {
                    getLibsFromBalo(importJar, modulePlatformLibs);
                }
            }
        }
        return modulePlatformLibs;
    }

    @Override
    public List<Path> nativeLibrariesForTests(PackageID packageID) {
        List<Path> testPlatformLibs = new ArrayList<>();
        List<Library> libraries = manifest.getPlatform().libraries;
        if (libraries != null) {
            for (Library library : libraries) {
                if ((library.getModules() == null ||
                        Arrays.asList(library.getModules()).contains(packageID.name.value)) &&
                        (library.getScope() != null && library.getScope().equalsIgnoreCase("testOnly"))) {
                    String libFilePath = library.getPath();
                    if (libFilePath == null) {
                        continue;
                    }
                    Path nativeFile = sourceRootPath.resolve(Paths.get(libFilePath));
                    testPlatformLibs.add(nativeFile);
                }
            }
        }
        return testPlatformLibs;
    }

    private void getPlatformLibs(PackageID packageID, List<Path> modulePlatformLibs) {
        List<Path> platformLibs = new ArrayList<>();
        List<Library> libraries = manifest.getPlatform().libraries;

        Optional<Dependency> importPathDependency = buildContext.getImportPathDependency(packageID);
        if (libraries != null) {
            for (Library library : libraries) {
                if ((library.getModules() == null ||
                        Arrays.asList(library.getModules()).contains(packageID.name.value) ||
                        Arrays.asList(library.getModules()).contains(packageID.orgName.value + "/" +
                                packageID.name.value)) &&
                        !(library.getScope() != null && library.getScope().equalsIgnoreCase("testOnly"))) {
                    String libFilePath = library.getPath();
                    if (libFilePath == null) {
                        continue;
                    }

                    Path nativeFile = sourceRootPath.resolve(Paths.get(libFilePath));
                    if (importPathDependency.isPresent()) {
                        platformLibs.add(nativeFile.getFileName());
                    }
                    modulePlatformLibs.add(nativeFile);
                }
            }
        }

        importPathDependency.ifPresent(dependency -> validateBaloDependencies(packageID, platformLibs,
                dependency.getMetadata().getPath()));
    }

    private void validateBaloDependencies(PackageID packageID, List<Path> platformLibs, Path importDependencyPath) {
        Manifest manifestFromBalo = RepoUtils.getManifestFromBalo(importDependencyPath);
        List<Library> baloDependencies = manifestFromBalo.getPlatform().libraries;
        List<Path> baloCompileScopeDependencies = new ArrayList<>();
        if (baloDependencies == null) {
            return;
        }

        for (Library baloTomlLib : baloDependencies) {
            if (baloTomlLib.getScope() != null && baloTomlLib.getScope().equalsIgnoreCase("provided")) {
                baloCompileScopeDependencies.add(Paths.get(baloTomlLib.getPath()).getFileName());
            }
        }

        for (Path baloTomlLib : baloCompileScopeDependencies) {
            if (!platformLibs.contains(baloTomlLib)) {
                buildContext.out().println("warning: " + packageID + " is missing a native library dependency - " +
                        baloTomlLib);
            }
        }
    }

    private void getLibsFromBalo(Path baloFilePath, List<Path> moduleDependencySet) {

        String fileName = baloFilePath.getFileName().toString();
        Path baloFileUnzipDirectory = Paths.get(baloFilePath.getParent().toString(),
                fileName.substring(0, fileName.lastIndexOf(".")));
        File destFile = baloFileUnzipDirectory.toFile();

        // Read from .balo file if directory not exist.
        if (!destFile.mkdir()) {
            // Read from already unzipped balo directory.
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(destFile.toString()))) {
                for (Path path : stream) {
                    moduleDependencySet.add(path);
                }
            } catch (IOException e) {
                throw createLauncherException("unable to copy native jar: " + e.getMessage());
            }
            return;
        }
        try (JarFile jar = new JarFile(baloFilePath.toFile())) {
            Enumeration<JarEntry> enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry file = enumEntries.nextElement();
                String entryName = file.getName();
                if (!entryName.endsWith(BLANG_COMPILED_JAR_EXT) || !entryName.contains(BALO_PLATFORM_LIB_DIR_NAME)) {
                    continue;
                }
                File f = Paths.get(baloFileUnzipDirectory.toString(),
                        entryName.split(BALO_PLATFORM_LIB_DIR_NAME)[1]).toFile();
                if (!f.exists()) { // if file already copied or its a directory, ignore
                    // get the input stream
                    try (InputStream is = jar.getInputStream(file)) {
                        Files.copy(is, f.toPath());
                    }
                }
                moduleDependencySet.add(f.toPath());
            }
        } catch (IOException e) {
            throw createLauncherException("unable to copy native jar: " + e.getMessage());
        }
    }

    private List<Path> getDependenciesFromDist(PackageID packageID) {
        // Get the jar paths
        List<Path> libPaths = new ArrayList<>();
        File tomlFile = getTomlFilePath(packageID);

        if (skipCopyLibsFromDist) {
            return null;
        }

        Toml tomlConfig = new Toml().read(tomlFile);
        Toml platform = tomlConfig.getTable("platform");
        if (platform == null) {
            return null;
        }

        List<Object> libraries = platform.getList("libraries");
        if (libraries == null) {
            return null;
        }

        for (Object lib : libraries) {
            Path fileName = Paths.get(((HashMap) lib).get("path").toString()).getFileName();
            libPaths.add(Paths.get(balHomePath, "bre", "lib", fileName.toString()));
        }
        return libPaths;
    }

    private File getTomlFilePath(PackageID packageID) {
        String version = BLANG_PKG_DEFAULT_VERSION;
        if (!packageID.version.value.equals("")) {
            version = packageID.version.value;
        }

        return Paths.get(balHomePath, DIST_BIR_CACHE_DIR_NAME, packageID.orgName.value,
                packageID.name.value, version, "Ballerina.toml").toFile();
    }

    private Path getJarFromDist(PackageID packageID) {
        List<Path> dependencies = getDependenciesFromDist(packageID);
        Path jarPath = null;
        if (dependencies == null) {
            return null;
        }

        for (Path dependency: dependencies) {
            if (dependency.getFileName().toString().equals(packageID.orgName.value + "-" + packageID.name.value + "-" +
                    RepoUtils.getBallerinaVersion() + ".jar")) {
                jarPath = dependency;
                break;
            }
        }
        return jarPath;
    }
}
