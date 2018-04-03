package org.ballerinalang.util;

import org.ballerinalang.spi.EmbeddedExecutor;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * Ballerina package provider class used in package management.
 */
public class EmbeddedExecutorProvider {
    private static EmbeddedExecutorProvider provider = new EmbeddedExecutorProvider();

    private final ServiceLoader<EmbeddedExecutor> loader;

    /**
     * Constructor of EmbeddedExecutorProvider.
     */
    private EmbeddedExecutorProvider() {
        loader = ServiceLoader.load(EmbeddedExecutor.class);
    }

    /**
     * Creates an instance of the EmbeddedExecutorProvider if its not already created.
     *
     * @return instance of EmbeddedExecutorProvider
     */
    public static EmbeddedExecutorProvider getInstance() {
        return provider;
    }

    /**
     * Get an instance of EmbeddedExecutor after iterating over the loaded ServiceLoaders.
     *
     * @return instance of EmbeddedExecutor
     */
    public EmbeddedExecutor getExecutor() {
        EmbeddedExecutor service = loader.iterator().next();
        if (service != null) {
            return service;
        } else {
            throw new NoSuchElementException("No implementation of the EmbeddedExecutor");
        }
    }
}
