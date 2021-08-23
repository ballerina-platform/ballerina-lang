package org.ballerina.semtypes;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests if placeholder is creted
 *
 */
public class SemtypeTests {

    @Test
    public void testPlaceholder() {
        SemtypePlaceholder sp = new SemtypePlaceholder();
        Assert.assertTrue(sp.s1 instanceof ComplexSemType);
        Assert.assertTrue(sp.s2 instanceof UniformTypeBitSet);
    }
}
