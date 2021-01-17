package io.ballerina.projects;

public class KubernetesToml {
    private TomlDocumentContext ballerinaTomlContext;
    private Package aPackage;
    private DiagnosticResult diagnostics;

    private KubernetesToml(Package aPackage, TomlDocumentContext ballerinaTomlContext) {
        this.aPackage = aPackage;
        this.ballerinaTomlContext = ballerinaTomlContext;
    }

    public static KubernetesToml from(Package pkg, TomlDocumentContext ballerinaTomlContext) {
        return new KubernetesToml(pkg, ballerinaTomlContext);
    }
}
