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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ballerina.projects.internal.balo.BaloJson;
import io.ballerina.projects.internal.balo.PackageJson;
import io.ballerina.projects.internal.balo.adaptors.JsonCollectionsAdaptor;
import io.ballerina.projects.internal.balo.adaptors.JsonStringsAdaptor;
import org.apache.commons.compress.utils.IOUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * {@code BaloWriter} writes a package to balo format.
 *
 * @since 2.0.0
 */
public class BaloWriter {
    private static final String MODULES_ROOT = "modules";
    private static final String RESOURCE_DIR_NAME = "resources";
    private static final String BLANG_SOURCE_EXT = ".bal";

    private BaloWriter() {
    }

    /**
     * Write a package to a .balo and return the created .balo path.
     *
     * @param pkg  Package to be written as a .balo.
     * @param baloPath Directory where the .balo should be created.
     */
    public static void write(Package pkg, Path baloPath) {
        // todo check if the given package is compiled properly

        // Create the archive over write if exists
        try (ZipOutputStream baloOutputStream = new ZipOutputStream(new FileOutputStream(String.valueOf(baloPath)))) {
            // Now lets put stuff in
            populateBaloArchive(baloOutputStream, pkg);
        } catch (IOException e) {
            throw new BLangCompilerException("Failed to create balo :" + e.getMessage(), e);
        } catch (BLangCompilerException be) {
            // clean up if an error occur
            try {
                Files.delete(baloPath);
            } catch (IOException e) {
                // We ignore this error and throw out the original blang compiler error to the user
            }
            throw be;
        }
    }

    private static void populateBaloArchive(ZipOutputStream baloOutputStream, Package pkg)
            throws IOException {

        addBaloJson(baloOutputStream);
        addPackageJson(baloOutputStream, pkg);
        addPackageDoc(baloOutputStream, pkg.project().sourceRoot(), pkg.packageName().toString());
        addPackageSource(baloOutputStream, pkg);
        addBIr(baloOutputStream, pkg);
        // Add platform libs only if it is not a template module
        //        if (!ballerinaToml.isTemplateModule(packageName)) {
        //            addPlatformLibs(root, packagePath, ballerinaToml);
        //        }
    }

    private static void addBaloJson(ZipOutputStream baloOutputStream) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String baloJson = gson.toJson(new BaloJson());
        try {
            putZipEntry(baloOutputStream, "balo.json",
                    new ByteArrayInputStream(baloJson.getBytes(Charset.defaultCharset())));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write 'balo.json' file: " + e.getMessage(), e);
        }
    }

    private static void addBIr(ZipOutputStream baloOutputStream, Package pkg) {
        for (ModuleId moduleId : pkg.moduleIds()) {
            Module module = pkg.module(moduleId);
            try {
                String moduleName = module.moduleName().toString();
                byte[] bir = writePackage(module.bir());
                putZipEntry(baloOutputStream, moduleName + ".bir",
                        new ByteArrayInputStream(bir));
            } catch (IOException e) {
                throw new RuntimeException("Failed to write 'balo.json' file: " + e.getMessage(), e);
            }
        }
    }

    private static byte[] writePackage(CompiledBinaryFile.BIRPackageFile packageFile) throws IOException {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        try (DataOutputStream dataOutStream = new DataOutputStream(byteArrayOS)) {
            dataOutStream.write(CompiledBinaryFile.BIRPackageFile.BIR_MAGIC);
            dataOutStream.writeInt(CompiledBinaryFile.BIRPackageFile.BIR_VERSION);

            dataOutStream.write(packageFile.pkgBirBinaryContent);
            return byteArrayOS.toByteArray();
        }
    }

    private static void addPackageJson(ZipOutputStream baloOutputStream, Package pkg) {
        //        io.ballerina.projects.model.Package pkg = ballerinaToml.getPackage();
        PackageJson packageJson = new PackageJson(pkg.packageOrg().toString(), pkg.packageName().toString(),
                pkg.packageVersion().toString());

        //        // Information extracted from Ballerina.toml
        //        packageJson.setLicenses(pkg.getLicense());
        //        packageJson.setAuthors(pkg.getAuthors());
        //        packageJson.setSourceRepository(pkg.getRepository());
        //        packageJson.setKeywords(pkg.getKeywords());
        //        packageJson.setExported(pkg.getExported());
        //
        //        // Distribution details
        //        packageJson.setBallerinaVersion(getBallerinaVersion());
        //        // TODO Need to set platform, implementation_vendor & spec
        //
        //        // Dependencies and platform libraries
        //        List<Dependency> dependencies = new ArrayList<>();
        //        List<PlatformLibrary> platformLibraries = new ArrayList<>();
        //
        //        // TODO Need to get all the dependencies (Not mentioned in the toml)
        //        Map<String, Object> tomlDependencies = ballerinaToml.getDependencies();
        //        for (String key : tomlDependencies.keySet()) {
        //            Object dependency = tomlDependencies.get(key);
        //            // if String, then Dependency
        //            if (dependency instanceof String) {
        //                String[] keyParts = key.split("/");
        //                Dependency dep = new Dependency(keyParts[0], keyParts[1], (String) dependency);
        //                dependencies.add(dep);
        //            } else { // else, PlatformLibrary
        //                // TODO Need to set platform libraries
        //            }
        //        }

        // Remove fields with empty values from `package.json`
        Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Collection.class, new JsonCollectionsAdaptor())
                .registerTypeHierarchyAdapter(String.class, new JsonStringsAdaptor()).setPrettyPrinting().create();

        try {
            putZipEntry(baloOutputStream, "package.json",
                    new ByteArrayInputStream(gson.toJson(packageJson).getBytes(Charset.defaultCharset())));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write 'package.json' file: " + e.getMessage(), e);
        }
    }

    // TODO when iterating and adding source files should create source files from Package sources

    private static void addPackageDoc(ZipOutputStream baloOutputStream, Path packageSourceDir, String pkgName)
            throws IOException {
        final String packageMdFileName = "Package.md";
        final String moduleMdFileName = "Module.md";

        Path packageMd = packageSourceDir.resolve(packageMdFileName);
        Path docsDirInBalo = Paths.get("docs");

        // If `Package.md` exists, create the docs directory & add `Package.md`
        if (packageMd.toFile().exists()) {
            Path packageMdInBalo = docsDirInBalo.resolve(packageMdFileName);
            putZipEntry(baloOutputStream, String.valueOf(packageMdInBalo),
                    new FileInputStream(String.valueOf(packageMd)));
        }

        // If `Module.md` of default module exists, create `docs/modules` directory & add `Module.md`
        Path defaultModuleMd = packageSourceDir.resolve(moduleMdFileName);
        Path modulesDirInBaloDocs = docsDirInBalo.resolve(MODULES_ROOT);

        if (defaultModuleMd.toFile().exists()) {
            Path defaultModuleMdInBaloDocs = modulesDirInBaloDocs.resolve(pkgName).resolve(moduleMdFileName);
            putZipEntry(baloOutputStream, String.valueOf(defaultModuleMdInBaloDocs),
                    new FileInputStream(String.valueOf(defaultModuleMd)));
        }

        // Add other module docs
        File modulesSourceDir = new File(String.valueOf(packageSourceDir.resolve(MODULES_ROOT)));
        File[] directoryListing = modulesSourceDir.listFiles();

        if (directoryListing != null) {
            for (File moduleDir : directoryListing) {
                if (moduleDir.isDirectory()) {
                    // Get `Module.md` path
                    Path otherModuleMd = packageSourceDir.resolve(MODULES_ROOT).resolve(moduleDir.getName())
                            .resolve(moduleMdFileName);
                    // Create `package.module` folder, if `Module.md` path exists
                    if (otherModuleMd.toFile().exists()) {
                        Path otherModuleMdInBaloDocs = modulesDirInBaloDocs.resolve(pkgName + "." + moduleDir.getName())
                                .resolve(moduleMdFileName);
                        putZipEntry(baloOutputStream, String.valueOf(otherModuleMdInBaloDocs),
                                new FileInputStream(String.valueOf(otherModuleMd)));
                    }
                }
            }
        }
    }

    private static void addPackageSource(ZipOutputStream baloOutputStream, Package pkg) throws IOException {

        // add module sources
        for (ModuleId moduleId : pkg.moduleIds()) {
            Module module = pkg.module(moduleId);

            // copy resources directory
            Path moduleRoot = pkg.project().sourceRoot();
            if (module.moduleName() != pkg.getDefaultModule().moduleName()) {
                moduleRoot = moduleRoot.resolve(MODULES_ROOT).resolve(module.moduleName().moduleNamePart());
            }
            String resourcesPathInBalo = Paths.get(MODULES_ROOT, module.moduleName().toString(), RESOURCE_DIR_NAME)
                    .toString();
            putDirectoryToZipFile(moduleRoot.resolve(RESOURCE_DIR_NAME).toString(), resourcesPathInBalo,
                    baloOutputStream);

            // only add .bal files of module
            for (DocumentId docId : module.documentIds()) {
                Document document = module.document(docId);
                if (document.name().endsWith(BLANG_SOURCE_EXT)) {
                    Path documentPath = Paths.get(MODULES_ROOT, module.moduleName().toString(), document.name());
                    char[] documentContent = document.textDocument().toCharArray();

                    putZipEntry(baloOutputStream, String.valueOf(documentPath),
                            new ByteArrayInputStream(new String(documentContent).getBytes(StandardCharsets.UTF_8)));
                }
            }
        }
    }

    private static void putZipEntry(ZipOutputStream baloOutputStream, String fileName, InputStream in)
            throws IOException {
        ZipEntry entry = new ZipEntry(fileName);
        baloOutputStream.putNextEntry(entry);

        IOUtils.copy(in, baloOutputStream);
        IOUtils.closeQuietly(in);
    }

    private static void putDirectoryToZipFile(String sourceDir, String pathInZipFile, ZipOutputStream out)
            throws IOException {
        String sourceRootDirectory = sourceDir;
        if (Paths.get(sourceDir).toFile().exists()) {
            File[] files = new File(sourceDir).listFiles();

            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        putDirectoryToZipFile(sourceDir + File.separator + file.getName(), pathInZipFile, out);
                    } else {
                        String fileNameInBalo =
                                pathInZipFile + File.separator + file.getPath().replace(sourceRootDirectory, "");
                        putZipEntry(out, fileNameInBalo,
                                new FileInputStream(sourceDir + File.separator + file.getName()));
                    }
                }
            }
        }
    }

    //    private static void addPlatformLibs(Path root, Path projectDirectory, BallerinaToml ballerinaToml)
    //            throws IOException {
    //        //If platform libs are defined add them to balo
    //        List<Library> platformLibs = ballerinaToml.getPlatform().libraries;
    //        if (platformLibs == null) {
    //            return;
    //        }
    //        Path platformLibsDir = root.resolve(ProjectConstants.LIB_DIR);
    //        Files.createDirectory(platformLibsDir);
    //
    //        for (Library lib : platformLibs) {
    //            if (lib.getModules() == null && lib.getScope() == null) {
    //                Path libPath = Paths.get(lib.getPath());
    //                Path nativeFile = projectDirectory.resolve(libPath);
    //                Path libFileName = libPath.getFileName();
    //                if (libFileName == null) {
    //                    continue;
    //                }
    //                Path targetPath = platformLibsDir.resolve(libFileName.toString());
    //                try {
    //                    Files.copy(nativeFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
    //                } catch (IOException e) {
    //                    throw new BLangCompilerException(
    //                            "Error while trying to add platform library to the BALO: " + lib.toString(), e);
    //                }
    //            }
    //        }
    //    }
}
