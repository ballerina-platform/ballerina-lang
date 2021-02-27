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
import io.ballerina.projects.internal.PackageDependencyGraphBuilder;
import io.ballerina.projects.utils.DependencyGraphJson;
import io.ballerina.projects.utils.DependencyGraphJson.DependencyJson;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contain cases to test the version conflict resolution logic.
 *
 * @since 2.0.0
 */
public class VersionConflictResolutionTests {
    private static final Gson gson = new Gson();
    private static final Path testSourcesDirectory = Paths.get(
            "src/test/resources/project_api/version_conflicts").toAbsolutePath().normalize();

    @Test
    public void testNoVersionConflictsCase() throws IOException {
        // Dependency graph
        // pkg_a(1.1.1) --> pkg_b(2.1.0) --> pkg_c(1.0.5)
        //              --> pkg_d(3.4.5) --> pkg_c(1.0.5)
        //              --> pkg_c(1.0.5)
        compareGraphs(getDependencyGraph(testSourcesDirectory.resolve("no_conflicts_1_input.json")),
                getDependencyGraph(testSourcesDirectory.resolve("no_conflicts_1_expected.json")));

        compareGraphs(getDependencyGraph(testSourcesDirectory.resolve("no_conflicts_2_input.json")),
                getDependencyGraph(testSourcesDirectory.resolve("no_conflicts_2_expected.json")));

        compareGraphs(getDependencyGraph(testSourcesDirectory.resolve("conflicts_1_input.json")),
                getDependencyGraph(testSourcesDirectory.resolve("conflicts_1_expected.json")));

        compareGraphs(getDependencyGraph(testSourcesDirectory.resolve("conflicts_2_input.json")),
                getDependencyGraph(testSourcesDirectory.resolve("conflicts_2_expected.json")));
    }

    @Test(description = "Override default scope dependency with another default version dependency")
    public void testConflictResolutionCase1A() {
        PackageDescriptor rootNode = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("a"), PackageVersion.from("0.1.0"));
        PackageDependencyGraphBuilder graphBuilder = new PackageDependencyGraphBuilder(rootNode);

        PackageDescriptor dependencyCurrent = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.1.0"));
        graphBuilder.addDependency(rootNode, dependencyCurrent, PackageDependencyScope.DEFAULT);

        // Now lets add a newer version of the dependency
        PackageDescriptor dependencyNew = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.5.0"));
        graphBuilder.addDependency(rootNode, dependencyNew, PackageDependencyScope.DEFAULT);

        DependencyGraph<PackageDescriptor> dependencyGraph = graphBuilder.build();
        Collection<PackageDescriptor> directDependencies = dependencyGraph.getDirectDependencies(rootNode);
        // There is only one here
        PackageDescriptor packageB = directDependencies.iterator().next();
        Assert.assertEquals(packageB, dependencyNew);
    }

    @Test(description = "Override default scope dependency with another default version dependency")
    public void testConflictResolutionCase1B() {
        PackageDescriptor rootNode = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("a"), PackageVersion.from("0.1.0"));
        PackageDependencyGraphBuilder graphBuilder = new PackageDependencyGraphBuilder(rootNode);

        PackageDescriptor dependencyCurrent = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.1.0"));
        graphBuilder.addDependency(rootNode, dependencyCurrent, PackageDependencyScope.DEFAULT);

        // Now lets add a older version of the dependency
        PackageDescriptor dependencyNew = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.0.0"));
        graphBuilder.addDependency(rootNode, dependencyNew, PackageDependencyScope.DEFAULT);

        DependencyGraph<PackageDescriptor> dependencyGraph = graphBuilder.build();
        Collection<PackageDescriptor> directDependencies = dependencyGraph.getDirectDependencies(rootNode);
        // There is only one here
        PackageDescriptor packageB = directDependencies.iterator().next();
        Assert.assertEquals(packageB, dependencyCurrent);
    }

    @Test(description = "Override test scope dependency with another test version dependency")
    public void testConflictResolutionCase2A() {
        PackageDescriptor rootNode = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("a"), PackageVersion.from("0.1.0"));
        PackageDependencyGraphBuilder graphBuilder = new PackageDependencyGraphBuilder(rootNode);

        PackageDescriptor dependencyCurrent = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.1.0"));
        graphBuilder.addDependency(rootNode, dependencyCurrent, PackageDependencyScope.TEST_ONLY);

        // Now lets add a newer version of the dependency
        PackageDescriptor dependencyNew = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.5.0"));
        graphBuilder.addDependency(rootNode, dependencyNew, PackageDependencyScope.TEST_ONLY);

        DependencyGraph<PackageDescriptor> dependencyGraph = graphBuilder.build();
        Collection<PackageDescriptor> directDependencies = dependencyGraph.getDirectDependencies(rootNode);
        // There is only one here
        PackageDescriptor packageB = directDependencies.iterator().next();
        Assert.assertEquals(packageB, dependencyNew);
    }

    @Test(description = "Override test scope dependency with another test version dependency")
    public void testConflictResolutionCase2B() {
        PackageDescriptor rootNode = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("a"), PackageVersion.from("0.1.0"));
        PackageDependencyGraphBuilder graphBuilder = new PackageDependencyGraphBuilder(rootNode);

        PackageDescriptor dependencyCurrent = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.1.0"));
        graphBuilder.addDependency(rootNode, dependencyCurrent, PackageDependencyScope.TEST_ONLY);

        // Now lets add a older version of the dependency
        PackageDescriptor dependencyNew = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.0.0"));
        graphBuilder.addDependency(rootNode, dependencyNew, PackageDependencyScope.TEST_ONLY);

        DependencyGraph<PackageDescriptor> dependencyGraph = graphBuilder.build();
        Collection<PackageDescriptor> directDependencies = dependencyGraph.getDirectDependencies(rootNode);
        // There is only one here
        PackageDescriptor packageB = directDependencies.iterator().next();
        Assert.assertEquals(packageB, dependencyCurrent);
    }

    @Test(description = "Override test scope dependency with another default version dependency")
    public void testConflictResolutionCase3A() {
        PackageDescriptor rootNode = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("a"), PackageVersion.from("0.1.0"));
        PackageDependencyGraphBuilder graphBuilder = new PackageDependencyGraphBuilder(rootNode);

        PackageDescriptor dependencyCurrent = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.1.0"));
        graphBuilder.addDependency(rootNode, dependencyCurrent, PackageDependencyScope.TEST_ONLY);

        // Now lets add a newer version of the dependency
        PackageDescriptor dependencyNew = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.5.0"));
        graphBuilder.addDependency(rootNode, dependencyNew, PackageDependencyScope.DEFAULT);

        DependencyGraph<PackageDescriptor> dependencyGraph = graphBuilder.build();
        Collection<PackageDescriptor> directDependencies = dependencyGraph.getDirectDependencies(rootNode);
        // There is only one here
        PackageDescriptor packageB = directDependencies.iterator().next();
        Assert.assertEquals(packageB, dependencyNew);
    }

    @Test(description = "Override test scope dependency with another default version dependency")
    public void testConflictResolutionCase3B() {
        PackageDescriptor rootNode = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("a"), PackageVersion.from("0.1.0"));
        PackageDependencyGraphBuilder graphBuilder = new PackageDependencyGraphBuilder(rootNode);

        PackageDescriptor dependencyCurrent = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.1.0"));
        graphBuilder.addDependency(rootNode, dependencyCurrent, PackageDependencyScope.TEST_ONLY);

        // Now lets add a older version of the dependency
        PackageDescriptor dependencyNew = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.0.0"));
        graphBuilder.addDependency(rootNode, dependencyNew, PackageDependencyScope.DEFAULT);

        DependencyGraph<PackageDescriptor> dependencyGraph = graphBuilder.build();
        Collection<PackageDescriptor> directDependencies = dependencyGraph.getDirectDependencies(rootNode);
        // There is only one here
        PackageDescriptor packageB = directDependencies.iterator().next();
        Assert.assertEquals(packageB, dependencyNew);
    }

    @Test(description = "Override default scope dependency with another test version dependency")
    public void testConflictResolutionCase4A() {
        PackageDescriptor rootNode = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("a"), PackageVersion.from("0.1.0"));
        PackageDependencyGraphBuilder graphBuilder = new PackageDependencyGraphBuilder(rootNode);

        PackageDescriptor dependencyCurrent = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.1.0"));
        graphBuilder.addDependency(rootNode, dependencyCurrent, PackageDependencyScope.DEFAULT);

        // Now lets add a newer version of the dependency
        PackageDescriptor dependencyNew = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.5.0"));
        graphBuilder.addDependency(rootNode, dependencyNew, PackageDependencyScope.TEST_ONLY);

        DependencyGraph<PackageDescriptor> dependencyGraph = graphBuilder.build();
        Collection<PackageDescriptor> directDependencies = dependencyGraph.getDirectDependencies(rootNode);
        // There is only one here
        PackageDescriptor packageB = directDependencies.iterator().next();
        Assert.assertEquals(packageB, dependencyCurrent);
    }

    @Test(description = "Override default scope dependency with another test version dependency")
    public void testConflictResolutionCase4B() {
        PackageDescriptor rootNode = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("a"), PackageVersion.from("0.1.0"));
        PackageDependencyGraphBuilder graphBuilder = new PackageDependencyGraphBuilder(rootNode);

        PackageDescriptor dependencyCurrent = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.1.0"));
        graphBuilder.addDependency(rootNode, dependencyCurrent, PackageDependencyScope.DEFAULT);

        // Now lets add a older version of the dependency
        PackageDescriptor dependencyNew = PackageDescriptor.from(PackageOrg.from("samjs"),
                PackageName.from("b"), PackageVersion.from("1.0.0"));
        graphBuilder.addDependency(rootNode, dependencyNew, PackageDependencyScope.TEST_ONLY);

        DependencyGraph<PackageDescriptor> dependencyGraph = graphBuilder.build();
        Collection<PackageDescriptor> directDependencies = dependencyGraph.getDirectDependencies(rootNode);
        // There is only one here
        PackageDescriptor packageB = directDependencies.iterator().next();
        Assert.assertEquals(packageB, dependencyCurrent);
    }

    @Test(expectedExceptions = ProjectException.class, expectedExceptionsMessageRegExp =
            "Two incompatible versions exist in the dependency graph: samjs/package_b versions: 1.1.0, 2.1.0")
    public void testVersionConflictsMajor() throws IOException {
        getDependencyGraph(testSourcesDirectory.resolve("conflicts_negative_1.json"));
    }

    private void compareGraphs(DependencyGraph<PackageDescriptor> actualDependencyGraph,
                               DependencyGraph<PackageDescriptor> expectedDependencyGraph) {
        Collection<PackageDescriptor> actualNodes = actualDependencyGraph.getNodes();
        Collection<PackageDescriptor> expectedNodes = expectedDependencyGraph.getNodes();
        Assert.assertEquals(actualNodes.size(), expectedNodes.size());

        for (PackageDescriptor expectedNode : expectedNodes) {
            Collection<PackageDescriptor> actualNodeDeps = actualDependencyGraph.getDirectDependencies(expectedNode);
            Collection<PackageDescriptor> expectedNodeDeps =
                    expectedDependencyGraph.getDirectDependencies(expectedNode);
            Assert.assertNotNull(actualNodeDeps,
                    "Cannot find the node in the expected graph: " + expectedNode);
            Assert.assertEquals(actualNodeDeps.size(), expectedNodeDeps.size());

            for (PackageDescriptor expectedNodeDep : expectedNodeDeps) {
                Assert.assertTrue(actualNodeDeps.contains(expectedNodeDep),
                        "Cannot find the dependency node in the expected graph: " + expectedNodeDep);
            }
        }
    }

    private PackageDescriptor getPackageDesc(DependencyJson dependency) {
        return PackageDescriptor.from(PackageOrg.from(dependency.getOrg()),
                PackageName.from(dependency.getName()),
                PackageVersion.from(dependency.getVersion()));
    }

    private DependencyGraph<PackageDescriptor> getDependencyGraph(Path jsonPath) throws IOException {
        PackageDependencyGraphBuilder depGraphBuilder = new PackageDependencyGraphBuilder();
        DependencyGraphJson dependencyGraphJson = gson
                .fromJson(Files.newBufferedReader(jsonPath),
                        DependencyGraphJson.class);

        List<DependencyJson> directDependencies = dependencyGraphJson.directDependencies();
        for (DependencyJson directDependency : directDependencies) {
            PackageDescriptor pkgDesc = getPackageDesc(directDependency);
            depGraphBuilder.addNode(pkgDesc, PackageDependencyScope.DEFAULT);
            List<PackageDescriptor> pkgDescDependencies = new ArrayList<>();
            for (DependencyJson dependency : directDependency.getDependencies()) {
                PackageDescriptor pkgDescDependency = getPackageDesc(dependency);
                pkgDescDependencies.add(pkgDescDependency);
            }
            depGraphBuilder.addDependencies(pkgDesc, pkgDescDependencies, PackageDependencyScope.DEFAULT);
        }
        return depGraphBuilder.build();
    }
}
