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
package io.ballerina.projects.test;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.directory.BuildProject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains cases to test dependency graph changes with package edits.
 *
 * @since 2.0.0
 */
public class DependencyGraphTests extends BaseTest {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources").toAbsolutePath();

    @Test
    public void testVersionChange() {
        // 1) load the project
        Path projectPath = RESOURCE_DIRECTORY.resolve("projects_for_edit_api_tests/package_test_dependencies_toml");
        BuildProject project = BuildProject.load(projectPath);
        DependencyGraph<ResolvedPackageDependency> dependencyGraphOld = project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_c
        Assert.assertEquals(dependencyGraphOld.getNodes().size(), 2);

        // 2) update version of the package_c dependency in Dependencies.toml
        project.currentPackage().dependenciesToml().get().modify().withContent(
                        "[[dependency]]\n" +
                        "org = \"foo\"\n" +
                        "name = \"package_dep\"\n" +
                        "version = \"0.1.1\"\n").apply();

        // 3) compare dependency graphs before and after edit
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew = project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_c
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 2);
        DependencyGraph.Compatibility compatibility = dependencyGraphOld.compareTo(dependencyGraphNew);
        Assert.assertEquals(compatibility, DependencyGraph.Compatibility.INCOMPATIBLE);
    }

    @Test
    public void testRemoveDependency() {
        // 1) load the project
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_resolution_tests/package_b");
        BuildProject project = BuildProject.load(projectDirPath);
        DependencyGraph<ResolvedPackageDependency> dependencyGraphOld = project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_c
        Assert.assertEquals(dependencyGraphOld.getNodes().size(), 2);

        // 2) update the mod_b2/mod2.bal file to remove package_c dependency
        Module mod_b2 = project.currentPackage().module(ModuleName.from(PackageName.from("package_b"), "mod_b2"));
        Document document = mod_b2.document(mod_b2.documentIds().stream().findFirst().get());
        document.modify().withContent("public function func2() {\n" + "}").apply();

        // 3) compare dependency graphs before and after edit
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew = project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain only self
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 1);
        DependencyGraph.Compatibility compatibility = dependencyGraphOld.compareTo(dependencyGraphNew);
        Assert.assertEquals(compatibility, DependencyGraph.Compatibility.INCOMPATIBLE);
    }

    @Test
    public void testAddDependency() {
        // 1) load the project
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_resolution_tests/package_b");
        BuildProject project = BuildProject.load(projectDirPath);
        DependencyGraph<ResolvedPackageDependency> dependencyGraphOld = project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_c
        Assert.assertEquals(dependencyGraphOld.getNodes().size(), 2);

        // 2) update the mod_b2/mod2.bal file to add a new dependency
        Module mod_b2 = project.currentPackage().module(ModuleName.from(PackageName.from("package_b"), "mod_b2"));
        Document document = mod_b2.document(mod_b2.documentIds().stream().findFirst().get());
        document.modify().withContent(
                "import samjs/package_c.mod_c1;\n" +
                "import samjs/package_e as _;\n" +
                "\n" +
                "public function func2() {\n" +
                "    mod_c1:func1();\n" +
                "}"
        ).apply();

        // 3) compare dependency graphs before and after edit
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew = project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self, package_c and package_e
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 3);

        DependencyGraph.Compatibility compatibility = dependencyGraphOld.compareTo(dependencyGraphNew);
        Assert.assertEquals(compatibility, DependencyGraph.Compatibility.COMPATIBLE);
    }

    @Test
    public void testRemoveAndAddDependencies() {
        // 1) load the project
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_resolution_tests/package_b");
        BuildProject project = BuildProject.load(projectDirPath);
        DependencyGraph<ResolvedPackageDependency> dependencyGraphOld = project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_c
        Assert.assertEquals(dependencyGraphOld.getNodes().size(), 2);

        // 2) update the mod_b2/mod2.bal file to remove package_c dependency
        Module mod_b2 = project.currentPackage().module(ModuleName.from(PackageName.from("package_b"), "mod_b2"));
        Document document = mod_b2.document(mod_b2.documentIds().stream().findFirst().get());
        document.modify().withContent("import samjs/package_e as _;\n" +
                "public function func2() {\n" + "}").apply();

        // 3) compare dependency graphs before and after edit
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew = project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_e
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 2);
        DependencyGraph.Compatibility compatibility = dependencyGraphOld.compareTo(dependencyGraphNew);
        Assert.assertEquals(compatibility, DependencyGraph.Compatibility.INCOMPATIBLE);
    }

    @Test
    public void testUnaffectedEdit() {
        // 1) load the project
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_resolution_tests/package_b");
        BuildProject project = BuildProject.load(projectDirPath);
        DependencyGraph<ResolvedPackageDependency> dependencyGraphOld = project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_c
        Assert.assertEquals(dependencyGraphOld.getNodes().size(), 2);

        // 2) update the mod_b2/mod2.bal file to remove package_c dependency
        Module defaultModule = project.currentPackage().getDefaultModule();
        Document document = defaultModule.document(defaultModule.documentIds().stream().findFirst().get());
        document.modify().withContent("public function func2() {\n" + "}").apply();

        // 3) compare dependency graphs before and after edit
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew = project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_e
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 2);
        DependencyGraph.Compatibility compatibility = dependencyGraphOld.compareTo(dependencyGraphNew);
        Assert.assertEquals(compatibility, DependencyGraph.Compatibility.COMPATIBLE);
    }
}
