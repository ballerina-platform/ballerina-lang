package io.ballerina.projects.internal;

import io.ballerina.projects.ModuleDescriptor;

/**
 * Interface for {@code ModuleContext}.
 *
 * @since 2.0.0
 */
public interface ModuleContext {

    public boolean isExported();

    public ModuleDescriptor descriptor();
}
