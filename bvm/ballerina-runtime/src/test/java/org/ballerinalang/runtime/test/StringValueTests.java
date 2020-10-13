package org.ballerinalang.runtime.test;

import io.ballerina.runtime.values.NonBmpStringValue;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test cases for {@link io.ballerina.runtime.values.StringValue class} implementations.
 */
public class StringValueTests {

    private static final String UNICODE_STR = "C\uD83D\uDEF8mmander Frav\uD83D\uDC7Dr";
    private static final NonBmpStringValue SUBJECT = new NonBmpStringValue(UNICODE_STR, new int[]{1, 14});

    @Test
    void testUnicodeCodePointAfterNonBmp() {
        Assert.assertEquals(SUBJECT.getCodePoint(15), 'r');
    }

    @Test(expectedExceptions = {StringIndexOutOfBoundsException.class},
            expectedExceptionsMessageRegExp = ".*String index out of range: 16.*")
    void testUnicodeCodePointOutOfBounds() {
        SUBJECT.getCodePoint(16);
    }

    @Test
    void testUnicodeCodePointAtNonBmp() {
        Assert.assertEquals(SUBJECT.getCodePoint(1), 0x1F6F8); // U+1F6F8 = FLYING SAUCER
        Assert.assertEquals(SUBJECT.getCodePoint(14), 0x1F47D); // U+1F47D = EXTRATERRESTRIAL ALIEN
    }

    @Test
    void testUnicodeCodePointBeforeNonBmp() {
        Assert.assertEquals(SUBJECT.getCodePoint(0), 'C');
    }

    @Test
    void testUnicodeCodePointLength() {
        Assert.assertEquals(SUBJECT.length(), 16);
    }

}
