package io.ballerina.projects;

public class PackageMd {

    private final MdDocumentContext documentContext;
    private final Package aPackage;

    PackageMd(MdDocumentContext documentContext, Package aPackage) {
        this.documentContext = documentContext;
        this.aPackage = aPackage;
    }

    public static PackageMd from(DocumentConfig documentConfig, Package aPackage) {
        MdDocumentContext documentContext = MdDocumentContext.from(documentConfig);
        return new PackageMd(documentContext, aPackage);
    }

    public static PackageMd from(MdDocumentContext documentContext, Package aPackage) {
        return new PackageMd(documentContext, aPackage);
    }
}
