/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.workspace;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.compiler.workspace.repository.WorkspacePackageRepository;
import org.ballerinalang.repository.PackageRepository;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Test WorkspacePackageRepository.
 */
public class WorkspacePackageRepositoryTest {

    private WorkspaceDocumentManager documentManager;
    private String sourceRoot;
    private String pkg;
    private PackageRepository packageRepository;

    @BeforeClass
    public void setup() {
        documentManager = WorkspaceDocumentManagerImpl.getInstance();
        sourceRoot = Paths.get("src/test/resources/workspace").toAbsolutePath().toString();
        pkg = "org.pkg1";
        packageRepository = new WorkspacePackageRepository(sourceRoot, documentManager);
    }

    //@Test
    public void testCompilePackageWithDirtyContent() {
        Compiler compiler = Compiler.getInstance(prepareCompilerContext());
        BLangPackage packageNode = compiler.compile(pkg);
        Assert.assertEquals(packageNode.getFunctions().size(), 1,
                "Package should contain one function which is in persisted file.");
        Assert.assertEquals(packageNode.getFunctions().get(0).getName().getValue(), "sayHello",
                "Name of the function should be equal to sayHello.");

        // open the file in document manager and set content without the function
        final Path filePath = Paths.get(sourceRoot, "org.pkg1", "file1.bal");
        documentManager.openFile(filePath, "package org.pkg1;");
        compiler = Compiler.getInstance(prepareCompilerContext());
        compiler.compile(pkg);
        Assert.assertEquals(packageNode.getFunctions().size(), 0,
                "Package should now contain no functions as we removed it in file1.bal dirty content.");

        // now update the file and add two public functions
        documentManager.updateFile(filePath, "package org.pkg1; public function f1(){} " +
                "public function f2(){}");
        compiler = Compiler.getInstance(prepareCompilerContext());
        compiler.compile(pkg);
        Assert.assertEquals(packageNode.getFunctions().size(), 2,
                "Package should now contain two functions.");
        Assert.assertEquals(packageNode.getFunctions().get(0).getName().getValue(), "f1",
                "Name of the first function should be equal to f1.");
        Assert.assertEquals(packageNode.getFunctions().get(1).getName().getValue(), "f2",
                "Name of the first function should be equal to f2.");

        // now close file without saving new content to disk
        documentManager.closeFile(filePath);
        compiler = Compiler.getInstance(prepareCompilerContext());
        compiler.compile(pkg);
        Assert.assertEquals(packageNode.getFunctions().size(), 1,
                "Package should now contain a single function which is in the file on disk.");
        Assert.assertEquals(packageNode.getFunctions().get(0).getName().getValue(), "sayHello",
                "Name of the function should be equal to sayHello.");
    }

    protected CompilerContext prepareCompilerContext() {
        CompilerContext context = new CompilerContext();
        context.put(PackageRepository.class, packageRepository);
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        options.put(COMPILER_PHASE, CompilerPhase.CODE_ANALYZE.toString());
        return context;
    }
}
