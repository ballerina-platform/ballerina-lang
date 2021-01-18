package io.ballerina.projects;

/**
 * Represents the 'Dependencies.toml' file in a package.
 *
 * @since 2.0.0
 */
public class DependenciesToml {
    private TomlDocumentContext dependenciesTomlContext;
    private Package packageInstance;

    private DependenciesToml(Package aPackage, TomlDocumentContext dependenciesTomlContext) {
        this.packageInstance = aPackage;
        this.dependenciesTomlContext = dependenciesTomlContext;
    }

    public static DependenciesToml from(TomlDocumentContext ballerinaTomlContext, Package pkg) {
        return new DependenciesToml(pkg, ballerinaTomlContext);
    }

    public TomlDocumentContext getBallerinaTomlContext() {
        return dependenciesTomlContext;
    }

    public Package packageInstance() {
        return packageInstance;
    }
}
