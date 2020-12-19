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
public class AnnotationAttachmentPointTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/annotations/annot_attachments_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 243);
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
        int line = 230;
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
        int line = 271;
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
        int line = 307;
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
        int line = 349;
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
        int line = 388;
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
        int line = 427;
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
        int line = 490;
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
        int line = 529;
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
        int line = 568;
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
        int line = 606;
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
        int line = 645;
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
        int line = 683;
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
        int line = 733;
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
        int line = 776;
        validateError(compileResult, index++, "action invocation as an expression not allowed here", line, 1);
        validateError(compileResult, index++, "annotation 'v1' is not allowed on worker", line, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on worker", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on worker", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on worker", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on worker", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on worker", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on worker", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on worker", ++line, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on worker", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on worker", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on worker", line += 3, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on worker", line += 3, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on worker", line + 3, 1);
    }

    @Test
    public void testInvalidAttachmentForField() {
        int index = 229;
        validateError(compileResult, index++, "annotation 'v16' is not allowed on var", 819, 1);
        validateError(compileResult, index++, "annotation 'v16' is not allowed on function", 821, 1);
        validateError(compileResult, index++, "annotation 'v17' is not allowed on function", 822, 1);
        validateError(compileResult, index++, "annotation 'v18' is not allowed on function", 823, 1);
        validateError(compileResult, index++, "annotation 'v16' is not allowed on type", 828, 1);
        validateError(compileResult, index++, "annotation 'v17' is not allowed on type", 829, 1);
        validateError(compileResult, index++, "annotation 'v18' is not allowed on type", 830, 1);
        validateError(compileResult, index++, "annotation 'v17' is not allowed on record_field, field", 832, 5);
        validateError(compileResult, index++, "annotation 'v16' is not allowed on class", 835, 1);
        validateError(compileResult, index++, "annotation 'v17' is not allowed on class", 836, 1);
        validateError(compileResult, index++, "annotation 'v18' is not allowed on class", 837, 1);
        validateError(compileResult, index, "annotation 'v18' is not allowed on object_field, field", 839, 5);
    }

    @Test
    public void testInvalidAttachmentForTypeConversionExpr() {
        int index = 241;
        validateError(compileResult, index++, "annotation 'v16' is not allowed on type", 847, 17);
    }

    @Test
    public void testInvalidAttachmentForClass() {
        int index = 242;
        validateError(compileResult, index++, "annotation 'v19' is not allowed on class", 852, 6);
    }
}
