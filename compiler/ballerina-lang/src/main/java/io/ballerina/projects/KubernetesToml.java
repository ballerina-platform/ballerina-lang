package io.ballerina.projects;

/**
 * Represents the 'Kubernetes.toml' file in a package.
 *
 * @since 2.0.0
 */
public class KubernetesToml {
    private TomlDocumentContext kubernetesTomlContext;
    private Package packageInstance;

    private KubernetesToml(Package aPackage, TomlDocumentContext kubernetesTomlContext) {
        this.packageInstance = aPackage;
        this.kubernetesTomlContext = kubernetesTomlContext;
    }

    public static KubernetesToml from(TomlDocumentContext kubernetesTomlContext, Package pkg) {
        return new KubernetesToml(pkg, kubernetesTomlContext);
    }

    public TomlDocumentContext kubernetesTomlContext() {
        return kubernetesTomlContext;
    }

    public Package getPackageInstance() {
        return packageInstance;
    }
}
