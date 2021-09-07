/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.BallerinaFunctionSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.Project;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.tools.text.LinePosition;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.util.Name;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Contains cases to test retrieving file paths from dependencies.
 *
 * @since 2.0.0
 */
public class DependencyFilePathsTest extends BaseTest {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources").toAbsolutePath();

    @Test
    public void testGetDependencyFilePathFromBuildProject() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_resolution_tests").resolve("package_a");
        Project project = TestUtils.loadProject(projectDirPath);
        Package currentPackage = project.currentPackage();
        PackageCompilation compilation = currentPackage.getCompilation();

        Module rootModule = currentPackage.module(
                ModuleName.from(PackageName.from("package_a"), "mod_a2"));
        SemanticModel semanticModel = compilation.getSemanticModel(rootModule.moduleId());

        // get mod2.bal in pacakge_a.mod_a2 module
        DocumentId documentId = rootModule.documentIds().stream().findFirst().get();
        Document document = rootModule.document(documentId);

        // get info from position of mod_b1:func1();
        Name moduleName = ((BallerinaFunctionSymbol) semanticModel
                .symbol(document, LinePosition.from(3, 12)).get()).getInternalSymbol().pkgID.name;
        Name orgName = ((BallerinaFunctionSymbol) semanticModel
                .symbol(document, LinePosition.from(3, 12)).get()).getInternalSymbol().pkgID.orgName;
        String sourceFile = ((BInvokableSymbol) ((BallerinaFunctionSymbol) semanticModel
                .symbol(document, LinePosition.from(3, 12)).get()).getInternalSymbol()).source;

        // find path of file containing mod_b1:func1();
        Path filepath = null;
        Collection<ResolvedPackageDependency> dependencies =
                currentPackage.getResolution().dependencyGraph().getNodes();

        for (ResolvedPackageDependency depNode : dependencies) {
            Package depPackage = depNode.packageInstance();
            for (ModuleId moduleId : depPackage.moduleIds()) {
                if (depPackage.packageOrg().value().equals(orgName.getValue()) &&
                        depPackage.module(moduleId).moduleName().toString().equals(moduleName.getValue())) {
                    Module module = depPackage.module(moduleId);
                    for (DocumentId docId : module.documentIds()) {
                        if (module.document(docId).name().equals(sourceFile)) {
                            filepath =
                                    module.project().documentPath(docId).orElseThrow();
                            break;
                        }
                    }
                }
            }
        }
        assert filepath != null;
        Path expectedPath = Paths.get("build/repo/bala/samjs/package_b/0.1.0/any/modules/package_b.mod_b1")
                .resolve("mod1.bal").toAbsolutePath();
        Assert.assertEquals(filepath.toString(), expectedPath.toString());

        // get document id of dependency filepath
        Project balaProject = TestUtils.loadProject(
                Paths.get("build/repo/bala/samjs/package_b/0.1.0/any/"));
        DocumentId documentId1 = balaProject.documentId(filepath);

        Module mod1 = balaProject.currentPackage().module(
                ModuleName.from(balaProject.currentPackage().packageName(), "mod_b1"));

        Assert.assertEquals(documentId1,
                mod1.documentIds().stream().findFirst().get());
    }

    @Test
    public void testGetLangLibFilePath() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_resolution_tests").resolve("package_a");
        Project project = TestUtils.loadProject(projectDirPath);
        Package currentPackage = project.currentPackage();
        PackageCompilation compilation = currentPackage.getCompilation();

        Module rootModule = currentPackage.module(
                ModuleName.from(PackageName.from("package_a"), "mod_a1"));
        SemanticModel semanticModel = compilation.getSemanticModel(rootModule.moduleId());

        // get mod1.bal in package_a.mod_a1 module
        DocumentId documentId = rootModule.documentIds().stream().findFirst().get();
        Document document = rootModule.document(documentId);

        // get info from position of float:isFinite(float x);
        Name moduleName = ((BallerinaFunctionSymbol) semanticModel
                .symbol(document, LinePosition.from(2, 23)).get()).getInternalSymbol().pkgID.name;
        Name orgName = ((BallerinaFunctionSymbol) semanticModel
                .symbol(document, LinePosition.from(2, 23)).get()).getInternalSymbol().pkgID.orgName;
        String sourceFile = ((BInvokableSymbol) ((BallerinaFunctionSymbol) semanticModel
                .symbol(document, LinePosition.from(2, 23)).get()).getInternalSymbol()).source;

        // find path of file containing float:isFinite();
        Path filepath = null;

        Package langLibPackage = project.projectEnvironmentContext().environment().getService(PackageCache.class)
                .getPackages(PackageOrg.from(orgName.getValue()), PackageName.from(moduleName.getValue())).get(0);

        for (ModuleId moduleId : langLibPackage.moduleIds()) {
            Module module = langLibPackage.module(moduleId);
            for (DocumentId docId : module.documentIds()) {
                if (module.document(docId).name().equals(sourceFile)) {
                    filepath =
                            module.project().documentPath(docId).orElseThrow();
                    break;
                }
            }
        }

        assert filepath != null;
        Path expectedPath = Paths.get("build/repo/bala/ballerina/lang.float/0.0.0/any/modules/lang.float")
                .resolve("float.bal").toAbsolutePath();
        Assert.assertEquals(filepath.toString(), expectedPath.toString());

        // get document id of dependency filepath
        Project balaProject = TestUtils.loadProject(
                Paths.get("build/repo/bala/ballerina/lang.float/0.0.0/any"));
        DocumentId documentId1 = balaProject.documentId(filepath);
        Assert.assertEquals(documentId1,
                balaProject.currentPackage().getDefaultModule().documentIds().stream().findFirst().get());
    }
}
