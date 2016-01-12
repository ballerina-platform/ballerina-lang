package org.wso2.siddhi.core.util.statistics;

/**
 * Estimate the memory usage of set of object
 */
public interface MemoryUsageTracker {

    /**
     * Register the object that needs to be measured the memory usage
     *
     * @param object Object
     * @param name   An unique value to identify the object.
     */
    void registerObject(Object object, String name);

    /**
     * @param object Object
     * @return Name of the mem tracker
     */
    String getName(Object object);
}
