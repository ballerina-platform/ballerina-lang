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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Class to test annotation attachments.
 *
 * @since 1.0
 */
public class AnnotationAttachmentPointTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/annotations/annot_attachments_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 177);
    }

    @Test
    public void testInvalidAttachmentOnType() {
        int index = 0;
        validateError(compileResult, index++, "annotation 'v2' is not allowed on type", 38, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on type", 41, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on type", 44, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on type", 47, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on type", 50, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on type", 53, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on type", 54, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on type", 57, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on type", 60, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on type", 63, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on type", 66, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on type", 69, 1);
    }

    @Test
    public void testInvalidAttachmentOnObjectType() {
        int index = 12;
        validateError(compileResult, index++, "annotation 'v3' is not allowed on object, type", 76, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on object, type", 79, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on object, type", 82, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on object, type", 85, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on object, type", 88, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on object, type", 89, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on object, type", 92, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on object, type", 95, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on object, type", 98, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on object, type", 101, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on object, type", 104, 1);
    }

    @Test
    public void testInvalidAttachmentOnObjectMethodDefinition() {
        int index = 23;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on object_method, function",
                      110, 5);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on object_method, function",
                      113, 5);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on object_method, function",
                      116, 5);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on object_method, function",
                      119, 5);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on object_method, function",
                      122, 5);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on object_method, function",
                      123, 5);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on object_method, function",
                      126, 5);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on object_method, function",
                      129, 5);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on object_method, function",
                      132, 5);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on object_method, function",
                      135, 5);
        validateError(compileResult, index, "annotation 'v13' is not allowed on object_method, function",
                      138, 5);
    }

    @Test
    public void testInvalidAttachmentOnObjectMethodDeclaration() {
        int index = 34;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on object_method, function",
                      145, 5);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on object_method, function",
                      148, 5);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on object_method, function",
                      151, 5);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on object_method, function",
                      154, 5);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on object_method, function",
                      157, 5);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on object_method, function",
                      158, 5);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on object_method, function",
                      161, 5);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on object_method, function",
                      164, 5);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on object_method, function",
                      167, 5);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on object_method, function",
                      170, 5);
        validateError(compileResult, index, "annotation 'v13' is not allowed on object_method, function",
                      173, 5);
    }

    @Test
    public void testInvalidAttachmentOnFunction() {
        int index = 45;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on function", 214, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on function", 217, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on function", 220, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on function", 223, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on function", 226, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on function", 229, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on function", 230, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on function", 233, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on function", 236, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on function", 239, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on function", 242, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on function", 245, 1);
    }

    @Test
    public void testInvalidAttachmentOnParam() {
        int index = 57;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on parameter", 252, 31);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on parameter", 255, 29);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on parameter", 258, 29);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on parameter", 261, 29);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on parameter", 264, 29);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on parameter", 267, 29);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on parameter", 268, 29);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on parameter", 271, 29);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on parameter", 274, 29);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on parameter", 277, 29);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on parameter", 280, 29);
        validateError(compileResult, index, "annotation 'v13' is not allowed on parameter", 283, 29);
    }

    @Test
    public void testInvalidAttachmentOnReturn() {
        int index = 69;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on return", 285, 53);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on return", 288, 53);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on return", 291, 53);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on return", 294, 53);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on return", 297, 53);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on return", 300, 53);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on return", 303, 53);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on return", 306, 53);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on return", 309, 53);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on return", 312, 53);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on return", 315, 53);
        validateError(compileResult, index, "annotation 'v13' is not allowed on return", 318, 53);
    }

    @Test
    public void testInvalidAttachmentOnListener() {
        int index = 81;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on listener", 324, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on listener", 327, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on listener", 330, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on listener", 333, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on listener", 336, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on listener", 339, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on listener", 342, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on listener", 343, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on listener", 346, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on listener", 349, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on listener", 352, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on listener", 355, 1);
    }

    @Test
    public void testInvalidAttachmentOnService() {
        int index = 93;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on service", 360, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on service", 363, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on service", 366, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on service", 369, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on service", 372, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on service", 375, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on service", 378, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on service", 379, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on service", 382, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on service", 385, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on service", 388, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on service", 391, 1);
    }

    @Test
    public void testInvalidAttachmentOnResource() {
        int index = 105;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on resource, function", 396, 5);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on resource, function", 399, 5);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on resource, function", 402, 5);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on resource, function", 405, 5);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on resource, function", 408, 5);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on resource, function", 409, 5);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on resource, function", 412, 5);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on resource, function", 415, 5);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on resource, function", 418, 5);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on resource, function", 421, 5);
        validateError(compileResult, index, "annotation 'v13' is not allowed on resource, function", 424, 5);
    }

    @Test
    public void testInvalidAttachmentOnAnnotation() {
        int index = 116;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on annotation", 448, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on annotation", 451, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on annotation", 454, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on annotation", 457, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on annotation", 460, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on annotation", 463, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on annotation", 466, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on annotation", 467, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on annotation", 470, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on annotation", 473, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on annotation", 476, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on annotation", 479, 1);
    }

    @Test
    public void testInvalidAttachmentOnVar() {
        int index = 128;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on var", 484, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on var", 487, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on var", 490, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on var", 493, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on var", 496, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on var", 499, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on var", 502, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on var", 503, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on var", 506, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on var", 509, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on var", 512, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on var", 515, 1);
    }

    @Test
    public void testInvalidAttachmentOnConst() {
        int index = 140;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on const", 520, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on const", 523, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on const", 526, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on const", 529, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on const", 532, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on const", 535, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on const", 538, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on const", 539, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on const", 542, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on const", 545, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on const", 548, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on const", 551, 1);
    }

    @Test
    public void testInvalidAttachmentOnExternal() {
        int index = 152;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on external", 556, 62);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on external", 559, 61);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on external", 562, 61);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on external", 565, 61);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on external", 568, 61);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on external", 571, 61);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on external", 574, 61);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on external", 575, 61);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on external", 578, 61);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on external", 581, 61);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on external", 584, 61);
        validateError(compileResult, index, "annotation 'v12' is not allowed on external", 587, 61);
    }

    @Test
    public void testInvalidAttachmentOnServiceVariable() {
        int index = 164;
        validateError(compileResult, index++, "annotation 'v8' is not allowed on var", 591, 1);
        validateError(compileResult, index++, "annotation 'v1' is not allowed on service", 595, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on service", 598, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on service", 601, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on service", 604, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on service", 607, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on service", 610, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on service", 613, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on service", 614, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on service", 617, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on service", 620, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on service", 623, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on service", 626, 1);
    }
}
