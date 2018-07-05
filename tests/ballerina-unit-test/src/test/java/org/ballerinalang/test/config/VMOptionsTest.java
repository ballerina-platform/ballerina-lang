package org.ballerinalang.test.config;

import org.ballerinalang.util.VMOptions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for the VMOptions registry.
 */
public class VMOptionsTest {

    private VMOptions vmOptions;

    @BeforeClass
    public void setup() {
        vmOptions = VMOptions.getInstance();
    }

    @Test
    public void testVMOptions() {
        vmOptions.addOptions(getOptionsMap());
        vmOptions.add("brelog.level", "DEBUG");

        Assert.assertTrue(vmOptions.contains("foo"));
        Assert.assertFalse(vmOptions.contains("def"));
        Assert.assertEquals(vmOptions.get("foo"), "abc");
        Assert.assertNull(vmOptions.get("def"));
        Assert.assertEquals(vmOptions.remove("bar"), "xyz");
        Assert.assertNotNull(vmOptions.optionNamesIterator());
        Assert.assertNotNull(vmOptions.optionValuesIterator());

        vmOptions.reset();
        Assert.assertNull(vmOptions.get("brelog.level"));
        Assert.assertNull(vmOptions.get("foo"));
    }

    private Map<String, String> getOptionsMap() {
        Map<String, String> options = new HashMap<>();
        options.put("foo", "abc");
        options.put("bar", "xyz");
        return options;
    }
}
