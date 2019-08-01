/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the {@link NumericLiteralSupport} class.
 *
 * @since 0.995
 */
public class NumericLiteralSupportTest {

  @Test
  public void testStripDiscriminator() {
    Assert.assertEquals(NumericLiteralSupport.stripDiscriminator("3"), "3");
    Assert.assertEquals(NumericLiteralSupport.stripDiscriminator("abc"), "abc");
    Assert.assertEquals(NumericLiteralSupport.stripDiscriminator("abcf"), "abc");
    Assert.assertEquals(NumericLiteralSupport.stripDiscriminator("abcF"), "abc");
    Assert.assertEquals(NumericLiteralSupport.stripDiscriminator("abcd"), "abc");
    Assert.assertEquals(NumericLiteralSupport.stripDiscriminator("abcD"), "abc");
  }

  @Test
  public void testIsHexLiteral() {
    Assert.assertTrue(NumericLiteralSupport.isHexLiteral("p\u0000p"));
    Assert.assertTrue(NumericLiteralSupport.isHexLiteral("P\u0000P"));
    Assert.assertFalse(NumericLiteralSupport.isHexLiteral("1"));
  }

  @Test
  public void testParseBigDecimal() {
    Assert.assertEquals(NumericLiteralSupport.parseBigDecimal(1d).toString(),
        "1.0");
    Assert.assertEquals(NumericLiteralSupport.parseBigDecimal(1D).toString(),
        "1.0");
    Assert.assertEquals(NumericLiteralSupport.parseBigDecimal(1).toString(),
        "1");
  }

  @Test
  public void testIsDecimalDiscriminated() {
    Assert.assertTrue(NumericLiteralSupport.isDecimalDiscriminated("dddddddd"));
    Assert.assertTrue(NumericLiteralSupport.isDecimalDiscriminated("DDDDDDDD"));
    Assert.assertFalse(NumericLiteralSupport.isDecimalDiscriminated("Bar"));
    Assert.assertFalse(NumericLiteralSupport.isDecimalDiscriminated("3"));
  }

  @Test
  public void testIsFloatDiscriminated() {
    Assert.assertTrue(NumericLiteralSupport.isFloatDiscriminated("ffffffff"));
    Assert.assertTrue(NumericLiteralSupport.isFloatDiscriminated("FFFFFFFF"));
    Assert.assertFalse(NumericLiteralSupport.isFloatDiscriminated("Bar"));
    Assert.assertFalse(NumericLiteralSupport.isFloatDiscriminated("2"));
  }
}
