package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.util.codegen.ResourceInfo;

/**
 * Created by rajith on 9/4/17.
 */
public class BResource implements Resource {
    private ResourceInfo resourceInfo;

    public BResource(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }
}
