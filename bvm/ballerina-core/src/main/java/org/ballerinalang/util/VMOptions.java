package org.ballerinalang.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A holder for BVM options (specified as -B arguments)
 *
 * @since 0.966.0
 */
public class VMOptions {

    private static final VMOptions vmOptionsRegistry = new VMOptions();

    private Map<String, String> vmOptions = new HashMap<>();

    private VMOptions() {
    }

    public static VMOptions getInstance() {
        return vmOptionsRegistry;
    }

    /**
     * Adds the specified option to the VM options map.
     *
     * @param optionName  Name of the option
     * @param optionValue Value of the option
     */
    public void add(String optionName, String optionValue) {
        vmOptions.put(optionName, optionValue);
    }

    /**
     * Adds the specified options map to the VM options map.
     *
     * @param options A VM options map
     */
    public void addOptions(Map<String, String> options) {
        vmOptions.putAll(options);
    }

    /**
     * Checks whether the specified VM option exists.
     *
     * @param optionName The name of the option to be checked
     * @return Returns true if the specified option name is in the map.
     */
    public boolean contains(String optionName) {
        return vmOptions.containsKey(optionName);
    }

    /**
     * Retrieves the option value the given option name maps to.
     *
     * @param optionName Name of the option to retrieve
     * @return The value of the option if it exists. If not, null.
     */
    public String get(String optionName) {
        return vmOptions.get(optionName);
    }

    /**
     * Removes the specified option and returns its value.
     *
     * @param optionName Option to be removed
     * @return The removed option
     */
    public String remove(String optionName) {
        return vmOptions.remove(optionName);
    }

    /**
     * Resets the VM options registry to its initial state.
     */
    public void reset() {
        vmOptions = new HashMap<>();
    }

    /**
     * Returns an iterator to iterate through the option names set of the VM options map.
     *
     * @return An iterator for the option names set
     */
    public Iterator<String> optionNamesIterator() {
        return vmOptions.keySet().iterator();
    }

    /**
     * Returns an iterator to iterate through the option values set of the VM options map.
     *
     * @return An iterator for the option values set
     */
    public Iterator<String> optionValuesIterator() {
        return vmOptions.values().iterator();
    }
}
