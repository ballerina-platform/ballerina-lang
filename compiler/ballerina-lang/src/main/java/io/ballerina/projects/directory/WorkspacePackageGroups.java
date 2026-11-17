/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.directory;

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageManifest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Groups the packages of a workspace that depend on one another, directly or transitively.
 * <p>
 * The packages of a group are compiled in one compiler context, since a package uses the compiled symbols of the
 * workspace packages it depends on. Packages in different groups are compiled in separate compiler contexts, so
 * that each can use its own resolution of a shared dependency.
 *
 * @since 2201.14.1
 */
final class WorkspacePackageGroups {

    private WorkspacePackageGroups() {
    }

    /**
     * Returns the groups of the given workspace packages, in the order the packages are given.
     *
     * @param packages packages of the workspace
     * @return groups of packages that depend on one another
     */
    static List<List<BuildProject>> of(List<BuildProject> packages) {
        Map<String, BuildProject> packagesByName = new HashMap<>();
        for (BuildProject buildProject : packages) {
            packagesByName.put(fullName(buildProject.currentPackage()), buildProject);
        }

        Map<BuildProject, BuildProject> parents = new LinkedHashMap<>();
        packages.forEach(buildProject -> parents.put(buildProject, buildProject));
        for (BuildProject buildProject : packages) {
            for (String dependency : packageDependencies(buildProject.currentPackage(), packagesByName)) {
                union(parents, buildProject, packagesByName.get(dependency));
            }
        }

        Map<BuildProject, List<BuildProject>> groups = new LinkedHashMap<>();
        for (BuildProject buildProject : packages) {
            groups.computeIfAbsent(find(parents, buildProject), root -> new ArrayList<>()).add(buildProject);
        }
        return new ArrayList<>(groups.values());
    }

    /** Workspace packages the given package imports, or declares as a dependency in its Ballerina.toml. */
    private static List<String> packageDependencies(Package pkg, Map<String, BuildProject> packagesByName) {
        List<String> dependencies = new ArrayList<>();
        for (PackageManifest.Dependency dependency : pkg.manifest().dependencies()) {
            String name = dependency.org().value() + "/" + dependency.name().value();
            if (packagesByName.containsKey(name)) {
                dependencies.add(name);
            }
        }
        for (Module module : pkg.modules()) {
            List<DocumentId> documentIds = new ArrayList<>(module.documentIds());
            documentIds.addAll(module.testDocumentIds());
            for (DocumentId documentId : documentIds) {
                Node rootNode = module.document(documentId).syntaxTree().rootNode();
                if (!(rootNode instanceof ModulePartNode modulePartNode)) {
                    continue;
                }
                for (ImportDeclarationNode importNode : modulePartNode.imports()) {
                    importedPackage(importNode, packagesByName).ifPresent(dependencies::add);
                }
            }
        }
        return dependencies;
    }

    /**
     * Returns the workspace package the import refers to. A module name starts with the name of its package,
     * which may itself contain dots, so the longest package name that matches is used.
     */
    private static Optional<String> importedPackage(ImportDeclarationNode importNode,
                                                    Map<String, BuildProject> packagesByName) {
        if (importNode.orgName().isEmpty()) {
            return Optional.empty();
        }
        String org = importNode.orgName().get().orgName().text();
        String moduleName = importNode.moduleName().stream().map(IdentifierToken::text)
                .collect(Collectors.joining("."));
        String packageName = moduleName;
        while (true) {
            String candidate = org + "/" + packageName;
            if (packagesByName.containsKey(candidate)) {
                return Optional.of(candidate);
            }
            int lastDot = packageName.lastIndexOf('.');
            if (lastDot < 0) {
                return Optional.empty();
            }
            packageName = packageName.substring(0, lastDot);
        }
    }

    private static String fullName(Package pkg) {
        return pkg.packageOrg().value() + "/" + pkg.packageName().value();
    }

    private static BuildProject find(Map<BuildProject, BuildProject> parents, BuildProject buildProject) {
        BuildProject root = buildProject;
        while (parents.get(root) != root) {
            root = parents.get(root);
        }
        parents.put(buildProject, root);
        return root;
    }

    private static void union(Map<BuildProject, BuildProject> parents, BuildProject first, BuildProject second) {
        BuildProject firstRoot = find(parents, first);
        BuildProject secondRoot = find(parents, second);
        if (firstRoot != secondRoot) {
            parents.put(secondRoot, firstRoot);
        }
    }
}
