package io.ballerina.projects;

/**
 * Represents the 'Package.md' file in a package.
 *
 * @since 2.0.0
 */
public class PackageMd {

    private final MdDocumentContext mdDocumentContext;
    private final Package packageInstance;

    PackageMd(MdDocumentContext documentContext, Package packageInstance) {
        this.mdDocumentContext = documentContext;
        this.packageInstance = packageInstance;
    }

    public static PackageMd from(DocumentConfig documentConfig, Package aPackage) {
        MdDocumentContext documentContext = MdDocumentContext.from(documentConfig);
        return new PackageMd(documentContext, aPackage);
    }

    public static PackageMd from(MdDocumentContext documentContext, Package aPackage) {
        return new PackageMd(documentContext, aPackage);
    }

    public MdDocumentContext mdDocumentContext() {
        return mdDocumentContext;
    }

    public Package packageInstance() {
        return packageInstance;
    }
}
