package org.ballerinalang.composer.service.workspace.langserver.util.completion;

import org.ballerinalang.composer.service.workspace.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.utils.BallerinaProgramContentProvider;

/**
 * Class for resolving the items in a given package
 * Singleton instance
 */
public class PackageItemResolver {

    private static PackageItemResolver instance= null;
    private static final Object mutex= new Object();
    private final BallerinaProgramContentProvider ballerinaProgramContentProvider =
            BallerinaProgramContentProvider.getInstance();

    /**
     * Private constructor
     */
    private PackageItemResolver(){
    }

    /**
     * Static get instance method
     * @return PackageItemResolver - PackageItemResolver instance
     */
    public static PackageItemResolver getInstance(){
        if(instance==null){
            synchronized (mutex){
                if(instance==null) instance= new PackageItemResolver();
            }
        }
        return instance;
    }

    /**
     * Get the function invocations in the package
     * @param packageName - package name
     */
    public void getFunctionInvocations(String packageName) {
        ModelPackage mPackage = this.ballerinaProgramContentProvider.getAllPackages().get(packageName);
    }
}
