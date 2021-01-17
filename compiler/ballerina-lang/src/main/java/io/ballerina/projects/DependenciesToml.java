package io.ballerina.projects;

public class DependenciesToml {
    private TomlDocumentContext ballerinaTomlContext;
    private Package aPackage;
    private DiagnosticResult diagnostics;

    private DependenciesToml(Package aPackage, TomlDocumentContext ballerinaTomlContext) {
        this.aPackage = aPackage;
        this.ballerinaTomlContext = ballerinaTomlContext;
    }

    public static DependenciesToml from(Package pkg, TomlDocumentContext ballerinaTomlContext) {
        return new DependenciesToml(pkg, ballerinaTomlContext);
    }

}
