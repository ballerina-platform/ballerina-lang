/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.parser.test.syntax.misc;

import org.testng.annotations.Test;

/**
 * Test parsing annotations.
 * 
 * @since 1.3.0
 */
public class AnnotationsTest extends AbstractMiscTest {

    @Test
    public void testAnnotsInTopLevelConstructs() {
        testFile("annotations/annotations_source_01.bal", "annotations/annotations_assert_01.json");
    }

    @Test
    public void testComplexAnnot() {
        testFile("annotations/annotations_source_02.bal", "annotations/annotations_assert_02.json");
    }

    @Test
    public void testAnnotsInFuncSignatureAndVars() {
        testFile("annotations/annotations_source_07.bal", "annotations/annotations_assert_07.json");
    }

    // Recovery tests

    @Test
    public void testRecoveryInAnnotValue() {
        testFile("annotations/annotations_source_03.bal", "annotations/annotations_assert_03.json");
    }

    @Test
    public void testHavingOnlyAtToken() {
        testFile("annotations/annotations_source_04.bal", "annotations/annotations_assert_04.json");
    }

    @Test
    public void testRecoveryInTopLevelConstructs() {
        testFile("annotations/annotations_source_05.bal", "annotations/annotations_assert_05.json");
    }

    @Test
    public void testAdditionalTokensInAnnot() {
        testFile("annotations/annotations_source_06.bal", "annotations/annotations_assert_06.json");
    }

    @Test
    public void testRecoveryInFuncSigAnnots() {
        testFile("annotations/annotations_source_08.bal", "annotations/annotations_assert_08.json");
    }

    @Test
    public void testAdditionalTokensBeforeParamAnnot() {
        testFile("annotations/annotations_source_09.bal", "annotations/annotations_assert_09.json");
    }

    @Test
    public void testAnnotAttachmentAtEOF() {
        testFile("annotations/annotations_source_10.bal", "annotations/annotations_assert_10.json");
    }

    @Test
    public void testAnnotAtExprLevelRecovery() {
        testFile("annotations/annotations_source_11.bal", "annotations/annotations_assert_11.json");
    }
}
