package org.ballerinalang.util;

import org.ballerinalang.spi.PackageProvider;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * Ballerina package provider class used in package management.
 */
public class BallerinaPackageProvider {
    private static BallerinaPackageProvider provider;
    private ServiceLoader<PackageProvider> loader;

    /**
     * Constructor of BallerinaPackageProvider.
     */
    private BallerinaPackageProvider() {
        loader = ServiceLoader.load(PackageProvider.class);
    }

    /**
     * Creates an instance of the BallerinaPackageProvider if its not already created.
     *
     * @return instance of BallerinaPackageProvider
     */
    public static BallerinaPackageProvider getInstance() {
        if (provider == null) {
            provider = new BallerinaPackageProvider();
        }
        return provider;
    }

    /**
     * Get an instance of PackageProvider after iterating over the loaded ServiceLoaders.
     *
     * @return instance of PackageProvider
     */
    public PackageProvider serviceImpl() {
        PackageProvider service = loader.iterator().next();
        if (service != null) {
            return service;
        } else {
            throw new NoSuchElementException("No implementation of the PackageProvider");
        }
    }
}
