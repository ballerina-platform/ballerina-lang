package io.ballerina.projects;

import io.ballerina.projects.internal.DefaultDiagnosticResult;

import java.util.Collections;
import java.util.List;

/**
 * Represents a Dependencies.toml file.
 *
 * @since 2.0.0
 */
public class DependencyManifest {

    private final List<Package> packages;
    private final DiagnosticResult diagnostics;

    private DependencyManifest(List<Package> packages, DiagnosticResult diagnostics) {
        this.packages = Collections.unmodifiableList(packages);
        this.diagnostics = diagnostics;
    }

    public static DependencyManifest from(List<Package> dependencies, DiagnosticResult diagnostics) {
        return new DependencyManifest(dependencies, diagnostics);
    }

    public static DependencyManifest from(List<Package> dependencies) {
        return new DependencyManifest(dependencies, new DefaultDiagnosticResult(Collections.emptyList()));
    }

    public List<Package> packages() {
        return packages;
    }

    public DiagnosticResult diagnostics() {
        return diagnostics;
    }

    /**
     * Represents a dependency package.
     *
     * @since 2.0.0
     */
    public static class Package {
        private final PackageName packageName;
        private final PackageOrg packageOrg;
        private final PackageVersion semanticVersion;
        private final String scope;
        private final boolean transitive;
        private final List<Dependency> dependencies;
        private final List<Module> modules;

        public Package(PackageName packageName, PackageOrg packageOrg, PackageVersion semanticVersion) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.semanticVersion = semanticVersion;
            this.scope = null;
            this.transitive = false;
            this.dependencies = Collections.emptyList();
            this.modules = Collections.emptyList();
        }

        public Package(PackageName packageName, PackageOrg packageOrg, PackageVersion semanticVersion, String scope,
                       boolean transitive, List<Dependency> dependencies, List<Module> modules) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.semanticVersion = semanticVersion;
            this.scope = scope;
            this.transitive = transitive;
            this.dependencies = dependencies;
            this.modules = modules;
        }

        public PackageName name() {
            return packageName;
        }

        public PackageOrg org() {
            return packageOrg;
        }

        public PackageVersion version() {
            return semanticVersion;
        }

        public String scope() {
            return scope;
        }

        public boolean isTransitive() {
            return transitive;
        }

        public List<Dependency> dependencies() {
            return dependencies;
        }

        public List<Module> modules() {
            return modules;
        }
    }

    /**
     * Represents a dependency of a package.
     *
     * @since 2.0.0
     */
    public static class Dependency {
        private final PackageName packageName;
        private final PackageOrg packageOrg;

        public Dependency(PackageName packageName, PackageOrg packageOrg) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
        }

        public PackageName name() {
            return packageName;
        }

        public PackageOrg org() {
            return packageOrg;
        }
    }

    /**
     * Represents a module of a dependency.
     *
     * @since 2.0.0
     */
    public static class Module {
        private final String org;
        private final String packageName;
        private final String moduleName;

        public Module(String org, String packageName, String moduleName) {
            this.org = org;
            this.packageName = packageName;
            this.moduleName = moduleName;
        }

        public String org() {
            return org;
        }

        public String packageName() {
            return packageName;
        }

        public String moduleName() {
            return moduleName;
        }
    }
}
