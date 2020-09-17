/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.langserver.compiler.workspace.repository;

import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.LSPackageCache;
import org.ballerinalang.langserver.compiler.common.LSDocumentIdentifierImpl;
import org.ballerinalang.langserver.compiler.workspace.ExtendedWorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for WorkspacePackageRepository.
 *
 * @since 0.982.0
 */
public class WorkspacePackageRepositoryTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    @Test
    public void testLookupPackageSourceForSinglePkg() {
        Path filePath = RES_DIR.resolve("source").resolve("singlepackage").resolve("io-sample.bal");
        Pair pair = compileAndGetWorkspacePackageRepo(filePath);
        WorkspacePackageRepository workspacePackageRepository = pair.getWorkspacePackageRepository();

        //Prepare package id
        String sourceFileName = filePath.getFileName().toString();
        PackageID packageId = new PackageID(sourceFileName);

        PackageSource packageSource = workspacePackageRepository.lookupPackageSource(packageId);
        Assert.assertEquals(packageId, packageSource.getPackageId());
        boolean found = packageSource.getPackageSourceEntries().stream().anyMatch(
                compilerInput -> compilerInput.getEntryName().equals(sourceFileName)
        );
        Assert.assertTrue(found, "PackageSource should contain '" + sourceFileName + "'");
    }

    @Test
    public void testLookupPackageSourceForSinglePkgNegative() {
        Path filePath = RES_DIR.resolve("source").resolve("singlepackage").resolve("io-sample.bal");
        Pair pair = compileAndGetWorkspacePackageRepo(filePath);
        WorkspacePackageRepository workspacePackageRepository = pair.getWorkspacePackageRepository();

        //Prepare package id
        String fileName = "non-existent.bal";
        PackageID invalidPackageId = new PackageID(fileName);

        PackageSource packageSource = workspacePackageRepository.lookupPackageSource(invalidPackageId);
        boolean found = packageSource.getPackageSourceEntries().stream().anyMatch(
                compilerInput -> compilerInput.getEntryName().equals(fileName)
        );
        Assert.assertFalse(found, "PackageSource should not contain '" + fileName + "'");
    }

    @Test
    public void testLookupPackageSourceForMultiPkgs() {
        Path filePath = RES_DIR.resolve("source").resolve("multipackages").resolve("sample").resolve("main.bal");
        Pair pair = compileAndGetWorkspacePackageRepo(filePath);
        Names names = pair.getNames();
        WorkspacePackageRepository workspacePackageRepository = pair.getWorkspacePackageRepository();

        //Prepare package ids
        Name orgName = names.fromString("demo");
        Name print = names.fromString("print");
        Name sample = names.fromString("sample");
        Name version = names.fromString("0.0.1");
        PackageID printPackageId = new PackageID(orgName, print, version);
        PackageID samplePackageId = new PackageID(orgName, sample, version);

        PackageSource packageSource = workspacePackageRepository.lookupPackageSource(printPackageId, "io-printer.bal");
        Assert.assertNotNull(packageSource);
        Assert.assertEquals(packageSource.getPackageId(), printPackageId);
        Assert.assertNotNull(packageSource.getPackageSourceEntries());
        packageSource = workspacePackageRepository.lookupPackageSource(samplePackageId, "main.bal");
        Assert.assertNotNull(packageSource);
        Assert.assertEquals(packageSource.getPackageId(), samplePackageId);
        Assert.assertNotNull(packageSource.getPackageSourceEntries());
    }

    @Test
    public void testLookupPackageSourceForMultiPkgsNegative() {
        Path filePath = RES_DIR.resolve("source").resolve("multipackages").resolve("sample").resolve("main.bal");
        Pair pair = compileAndGetWorkspacePackageRepo(filePath);
        Names names = pair.getNames();
        WorkspacePackageRepository workspacePackageRepository = pair.getWorkspacePackageRepository();

        //Prepare package ids
        Name orgName = names.fromString("demo");
        Name print = names.fromString("print");
        Name version = names.fromString("0.0.1");
        PackageID printPackageId = new PackageID(orgName, print, version);
        PackageID invalidPackageId = new PackageID(orgName, names.fromString("invalid"), version);

        PackageSource packageSource = workspacePackageRepository.lookupPackageSource(printPackageId,
                                                                                     "non-existent.bal");
        Assert.assertNull(packageSource);
        PackageSource packageSource1 = workspacePackageRepository.lookupPackageSource(invalidPackageId,
                                                                                      "io-printer.bal");
        Assert.assertNull(packageSource1);
    }

    private Pair compileAndGetWorkspacePackageRepo(Path filePath) {
        LSDocumentIdentifier document = new LSDocumentIdentifierImpl(filePath.toUri().toString());
        WorkspaceDocumentManagerImpl documentManager = ExtendedWorkspaceDocumentManagerImpl.getInstance();
        String sourceRoot = document.getProjectRoot();
        WorkspacePackageRepository workspacePackageRepository = new WorkspacePackageRepository(sourceRoot,
                                                                                               documentManager);

        //Compile file
        String packageName = getPackageName(sourceRoot, filePath);
        CompilerContext context = getCompilerContext(filePath, sourceRoot, packageName, workspacePackageRepository);
        compileFile(packageName, context);

        return new Pair(workspacePackageRepository, Names.getInstance(context));
    }

    private String getPackageName(String sourceRoot, Path filePath) {
        LSDocumentIdentifier lsDocument = new LSDocumentIdentifierImpl(sourceRoot);
        String packageName = lsDocument.getOwnerModule();
        if ("".equals(packageName)) {
            Path path = filePath.getFileName();
            if (path != null) {
                packageName = path.toString();
            }
        }
        return packageName;
    }

    private CompilerContext getCompilerContext(Path filePath, String sourceRoot, String packageName,
                                               PackageRepository packageRepository) {
        LSDocumentIdentifier sourceDocument = new LSDocumentIdentifierImpl(filePath, sourceRoot);
        WorkspaceDocumentManagerImpl documentManager = ExtendedWorkspaceDocumentManagerImpl.getInstance();
        PackageID packageID = new PackageID(Names.ANON_ORG, new Name(packageName), Names.DEFAULT_VERSION);
        return LSCompilerUtil.prepareCompilerContext(packageID, packageRepository, sourceDocument, documentManager,
                false);
    }

    private BLangPackage compileFile(String packageName, CompilerContext context) {
        BLangPackage bLangPackage = null;
        BLangDiagnosticLog.getInstance(context).resetErrorCount();
        Compiler compiler = Compiler.getInstance(context);
        bLangPackage = compiler.compile(packageName);
        LSPackageCache.getInstance(context).invalidate(bLangPackage.packageID);

        Assert.assertNotNull(bLangPackage);
        return bLangPackage;
    }

    private static class Pair {
        private final WorkspacePackageRepository workspacePackageRepository;
        private final Names names;

        Pair(WorkspacePackageRepository workspacePackageRepository, Names names) {
            this.workspacePackageRepository = workspacePackageRepository;
            this.names = names;
        }

        WorkspacePackageRepository getWorkspacePackageRepository() {
            return workspacePackageRepository;
        }

        public Names getNames() {
            return names;
        }
    }
}
