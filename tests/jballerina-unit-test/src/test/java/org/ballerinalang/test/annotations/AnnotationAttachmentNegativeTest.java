/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.annotations;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Class to test annotation attachments.
 *
 * @since 1.0
 */
@Test
public class AnnotationAttachmentNegativeTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/annotations/annot_attachments_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 265);
    }

    @Test
    public void testInvalidAttachmentOnType() {
        int index = 0;
        int line = 39;
        validateError(compileResult, index++, "annotation 'v2' is not allowed on type", line, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on type", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on type", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on type", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on type", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on type", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on type", ++line, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on type", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on type", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on type", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on type", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on type", line += 3, 1);
        validateError(compileResult, index, "annotation 'v15' is not allowed on type", line + 3, 1);
    }

    @Test
    public void testInvalidAttachmentOnObjectType() {
        int index = 13;
        int line = 80;
        validateError(compileResult, index++, "annotation 'v3' is not allowed on class", line, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on class", ++line, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on class", line += 3, 1);
        validateError(compileResult, index, "annotation 'v15' is not allowed on class", line + 3, 1);
    }

    @Test
    public void testInvalidAttachmentOnObjectMethodDefinition() {
        int index = 25;
        int line = 117;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on object_method, function",
                line, 5);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on object_method, function",
                line += 6, 5);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on object_method, function",
                ++line, 5);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index, "annotation 'v15' is not allowed on object_method, function",
                line + 3, 5);
    }

    @Test
    public void testInvalidAttachmentOnObjectMethodDeclaration() {
        int index = 36;
        int line = 155;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on object_method, function",
                line, 5);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on object_method, function",
                line += 6, 5);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on object_method, function",
                ++line, 5);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on object_method, function",
                line += 3, 5);
        validateError(compileResult, index, "annotation 'v15' is not allowed on object_method, function",
                line + 3, 5);
    }

    @Test
    public void testInvalidAttachmentOnFunction() {
        int index = 47;
        int line = 192;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on function", line, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on function", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on function", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on function", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on function", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on function", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on function", ++line, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on function", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on function", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on function", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on function", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on function", line += 3, 1);
        validateError(compileResult, index, "annotation 'v15' is not allowed on function", line + 3, 1);
    }

    @Test
    public void testInvalidAttachmentOnParam() {
        int index = 60;
        int line = 233;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on parameter", line, 31);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on parameter", line += 3, 29);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on parameter", line += 3, 29);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on parameter", line += 3, 29);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on parameter", line += 3, 29);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on parameter", line += 3, 29);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on parameter", ++line, 29);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on parameter", line += 3, 29);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on parameter", line += 3, 29);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on parameter", line += 3, 29);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on parameter", line += 3, 29);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on parameter", line += 3, 29);
        validateError(compileResult, index, "annotation 'v15' is not allowed on parameter", line + 3, 29);
    }

    @Test
    public void testInvalidAttachmentOnReturn() {
        int index = 73;
        int line = 269;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on return", line, 53);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on return", line += 3, 53);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on return", line += 3, 53);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on return", line += 3, 53);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on return", line += 3, 53);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on return", line += 3, 53);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on return", line += 3, 53);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on return", line += 3, 53);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on return", line += 3, 53);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on return", line += 3, 53);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on return", line += 3, 53);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on return", line += 3, 53);
        validateError(compileResult, index, "annotation 'v15' is not allowed on return", line + 3, 53);
    }

    @Test
    public void testInvalidAttachmentOnListener() {
        int index = 86;
        int line = 311;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on listener", line, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on listener", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on listener", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on listener", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on listener", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on listener", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on listener", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on listener", ++line, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on listener", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on listener", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on listener", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on listener", line += 3, 1);
        validateError(compileResult, index, "annotation 'v15' is not allowed on listener", line + 3, 1);
    }

    @Test
    public void testInvalidAttachmentOnService() {
        int index = 99;
        int line = 350;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on service", line, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on service", ++line, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on service", line += 3, 1);
        validateError(compileResult, index, "annotation 'v15' is not allowed on service", line + 3, 1);
    }

    @Test
    public void testInvalidAttachmentOnResource() {
        int index = 112;
        int line = 389;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on object_method, function", line,
                5);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on object_method, function", line += 3,
                5);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on object_method, function", line += 6,
                5);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on object_method, function", line += 3,
                5);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on object_method, function", ++line,
                5);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on object_method, function", line += 3,
                5);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on object_method, function", line += 3,
                5);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on object_method, function", line += 3,
                5);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on object_method, function", line += 3,
                5);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on object_method, function", line += 3,
                5);
        validateError(compileResult, index, "annotation 'v15' is not allowed on object_method, function", line + 3, 5);
    }

    @Test
    public void testInvalidAttachmentOnAnnotation() {
        int index = 123;
        int line = 452;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on annotation", line, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on annotation", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on annotation", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on annotation", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on annotation", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on annotation", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on annotation", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on annotation", ++line, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on annotation", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on annotation", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on annotation", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on annotation", line += 3, 1);
        validateError(compileResult, index, "annotation 'v15' is not allowed on annotation", line + 3, 1);
    }

    @Test
    public void testInvalidAttachmentOnVar() {
        int index = 136;
        int line = 491;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on var", line, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on var", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on var", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on var", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on var", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on var", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on var", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on var", ++line, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on var", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on var", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on var", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on var", line += 3, 1);
        validateError(compileResult, index, "annotation 'v15' is not allowed on var", line + 3, 1);
    }

    @Test
    public void testInvalidAttachmentOnLetVar() {
        int index = 149;
        int line = 530;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on var", line, 13);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on var", line += 3, 13);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on var", line += 3, 13);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on var", line += 3, 13);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on var", line += 3, 13);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on var", line += 3, 13);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on var", line += 3, 13);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on var", ++line, 13);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on var", line += 3, 13);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on var", line += 3, 13);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on var", line += 3, 13);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on var", line += 3, 13);
        validateError(compileResult, index, "annotation 'v15' is not allowed on var", line + 3, 13);
    }

    @Test
    public void testInvalidAttachmentOnConst() {
        int index = 162;
        int line = 568;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on const", line, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on const", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on const", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on const", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on const", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on const", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on const", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on const", ++line, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on const", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on const", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on const", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on const", line += 3, 1);
        validateError(compileResult, index, "annotation 'v15' is not allowed on const", line + 3, 1);
    }

    @Test
    public void testInvalidAttachmentOnExternal() {
        int index = 175;
        int line = 607;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on external", line, 62);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on external", line += 3, 61);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on external", line += 3, 61);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on external", line += 3, 61);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on external", line += 3, 61);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on external", line += 3, 61);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on external", line += 3, 61);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on external", ++line, 61);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on external", line += 3, 61);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on external", line += 3, 61);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on external", line += 3, 61);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on external", line += 3, 61);
        validateError(compileResult, index, "annotation 'v15' is not allowed on external", line + 3, 61);
    }

    @Test
    public void testInvalidAttachmentOnServiceVariable() {
        int index = 188;
        int line = 645;
        validateError(compileResult, index++, "annotation 'v8' is not allowed on var", line, 1);
        validateError(compileResult, index++, "annotation 'v1' is not allowed on service", line += 4, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on service", ++line, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on service", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on service", line += 3, 1);
        validateError(compileResult, index, "annotation 'v15' is not allowed on service", line + 3, 1);
    }

    @Test
    public void testInvalidAttachmentOnWorker() {
        int index = 202;
        int line = 695;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on worker", line, 5);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on worker", ++line, 5);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index, "annotation 'v13' is not allowed on worker", line + 3, 5);
    }

    @Test
    public void testInvalidAttachmentOnStart() {
        int index = 215;
        int line = 739;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on worker", line, 5);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on worker", ++line, 5);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on worker", line += 3, 5);
        validateError(compileResult, index, "annotation 'v13' is not allowed on worker", line + 3, 5);
    }

    @Test
    public void testInvalidAttachmentForField() {
        int index = 228;
        int line = 783;
        validateError(compileResult, index++, "annotation 'v16' is not allowed on var", line, 1);
        validateError(compileResult, index++, "annotation 'v16' is not allowed on function", line += 2, 1);
        validateError(compileResult, index++, "annotation 'v17' is not allowed on function", line += 1, 1);
        validateError(compileResult, index++, "annotation 'v18' is not allowed on function", line += 1, 1);
        validateError(compileResult, index++, "annotation 'v16' is not allowed on type", line += 5, 1);
        validateError(compileResult, index++, "annotation 'v17' is not allowed on type", line += 1, 1);
        validateError(compileResult, index++, "annotation 'v18' is not allowed on type", line += 1, 1);
        validateError(compileResult, index++, "annotation 'v17' is not allowed on record_field, field", line += 2, 5);
        validateError(compileResult, index++, "annotation 'v16' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v17' is not allowed on class", line += 1, 1);
        validateError(compileResult, index++, "annotation 'v18' is not allowed on class", line += 1, 1);
        validateError(compileResult, index, "annotation 'v18' is not allowed on object_field, field", line + 2, 5);
    }

    @Test
    public void testInvalidAttachmentForTypeConversionExpr() {
        int index = 240;
        validateError(compileResult, index++, "annotation 'v16' is not allowed on type", 811, 17);
    }

    @Test
    public void testInvalidAttachmentForClass() {
        int index = 241;
        validateError(compileResult, index++, "annotation 'v19' is not allowed on class", 816, 6);
    }

    @Test
    public void testQualifiedNameInInvalidAttachmentError() {
        validateError(compileResult, 242,
                      "annotation 'ballerina/lang.annotations:0.0.0:tainted' is not allowed on class", 823, 1);
    }

    @Test
    public void testInvalidAttachmentWithValue() {
        validateError(compileResult, 243,
                      "no annotation value expected for annotation 'ballerina/lang.annotations:0.0.0:tainted'",
                      828, 10);
        validateError(compileResult, 244, "no annotation value expected for annotation 'v7'",
                      833, 35);
    }

    @Test
    public void testInvalidAttachmentWithoutValue() {
        int line = 835;
        validateError(compileResult, 245, "annotation value expected for annotation of " +
                        "record type 'Annot' with required fields", line, 1);
        validateError(compileResult, 246, "annotation value expected for annotation of " +
                        "record type 'Annot' with required fields", line += 3, 1);
        validateError(compileResult, 247, "annotation value expected for annotation of " +
                        "record type 'Annot' with required fields", line += 1, 22);
        validateError(compileResult, 248, "annotation value expected for annotation of " +
                "record type 'Annot' with required fields", line += 6, 1);
        validateError(compileResult, 249, "annotation value expected for annotation of " +
                "record type 'Annot' with required fields", line + 1, 1);
    }

    @Test
    public void testInvalidAttachmentCount() {
        validateError(compileResult, 250, "cannot specify more than one annotation value for " +
                              "annotation 'ballerina/lang.annotations:0.0.0:tainted'", 850, 1);
        validateError(compileResult, 251,
                      "cannot specify more than one annotation value for annotation 'v1'", 852, 1);
    }

    @Test
    public void testInvalidAttachmentOnServiceClass() {
        int index = 252;
        int line = 856;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on class", line, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on class", line += 6, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on class", ++line, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on class", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v13' is not allowed on class", line += 3, 1);
        validateError(compileResult, index, "annotation 'v15' is not allowed on class", line + 3, 1);
    }
}
