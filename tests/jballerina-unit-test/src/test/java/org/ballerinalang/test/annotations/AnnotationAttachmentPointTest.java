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
        Assert.assertEquals(compileResult.getErrorCount(), 188);
    }

    @Test
    public void testInvalidAttachmentOnType() {
        int index = 0;
        validateError(compileResult, index++, "annotation 'v2' is not allowed on type", 36, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on type", 39, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on type", 42, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on type", 45, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on type", 48, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on type", 51, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on type", 52, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on type", 55, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on type", 58, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on type", 61, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on type", 64, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on type", 67, 1);
    }

    @Test
    public void testInvalidAttachmentOnObjectType() {
        int index = 12;
        validateError(compileResult, index++, "annotation 'v3' is not allowed on object, type", 74, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on object, type", 77, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on object, type", 80, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on object, type", 83, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on object, type", 86, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on object, type", 87, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on object, type", 90, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on object, type", 93, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on object, type", 96, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on object, type", 99, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on object, type", 102, 1);
    }

    @Test
    public void testInvalidAttachmentOnObjectMethodDefinition() {
        int index = 23;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on object_method, function",
                      108, 5);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on object_method, function",
                      111, 5);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on object_method, function",
                      114, 5);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on object_method, function",
                      117, 5);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on object_method, function",
                      120, 5);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on object_method, function",
                      121, 5);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on object_method, function",
                      124, 5);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on object_method, function",
                      127, 5);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on object_method, function",
                      130, 5);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on object_method, function",
                      133, 5);
        validateError(compileResult, index, "annotation 'v13' is not allowed on object_method, function",
                      136, 5);
    }

    @Test
    public void testInvalidAttachmentOnObjectMethodDeclaration() {
        int index = 34;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on object_method, function",
                      143, 5);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on object_method, function",
                      146, 5);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on object_method, function",
                      149, 5);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on object_method, function",
                      152, 5);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on object_method, function",
                      155, 5);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on object_method, function",
                      156, 5);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on object_method, function",
                      159, 5);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on object_method, function",
                      162, 5);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on object_method, function",
                      165, 5);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on object_method, function",
                      168, 5);
        validateError(compileResult, index, "annotation 'v13' is not allowed on object_method, function",
                      171, 5);
    }

    @Test
    public void testInvalidAttachmentOnObjectOutsideMethodDefinition() {
        int index = 45;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on object_method, function",
                      177, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on object_method, function",
                      180, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on object_method, function",
                      183, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on object_method, function",
                      186, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on object_method, function",
                      189, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on object_method, function",
                      190, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on object_method, function",
                      193, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on object_method, function",
                      196, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on object_method, function",
                      199, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on object_method, function",
                      202, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on object_method, function",
                      205, 1);
    }

    @Test
    public void testInvalidAttachmentOnFunction() {
        int index = 56;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on function", 212, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on function", 215, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on function", 218, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on function", 221, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on function", 224, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on function", 227, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on function", 228, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on function", 231, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on function", 234, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on function", 237, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on function", 240, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on function", 243, 1);
    }

    @Test
    public void testInvalidAttachmentOnParam() {
        int index = 68;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on parameter", 250, 31);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on parameter", 253, 29);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on parameter", 256, 29);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on parameter", 259, 29);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on parameter", 262, 29);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on parameter", 265, 29);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on parameter", 266, 29);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on parameter", 269, 29);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on parameter", 272, 29);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on parameter", 275, 29);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on parameter", 278, 29);
        validateError(compileResult, index, "annotation 'v13' is not allowed on parameter", 281, 29);
    }

    @Test
    public void testInvalidAttachmentOnReturn() {
        int index = 80;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on return", 283, 53);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on return", 286, 53);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on return", 289, 53);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on return", 292, 53);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on return", 295, 53);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on return", 298, 53);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on return", 301, 53);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on return", 304, 53);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on return", 307, 53);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on return", 310, 53);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on return", 313, 53);
        validateError(compileResult, index, "annotation 'v13' is not allowed on return", 316, 53);
    }

    @Test
    public void testInvalidAttachmentOnListener() {
        int index = 92;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on listener", 322, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on listener", 325, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on listener", 328, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on listener", 331, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on listener", 334, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on listener", 337, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on listener", 340, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on listener", 341, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on listener", 344, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on listener", 347, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on listener", 350, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on listener", 353, 1);
    }

    @Test
    public void testInvalidAttachmentOnService() {
        int index = 104;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on service", 358, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on service", 361, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on service", 364, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on service", 367, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on service", 370, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on service", 373, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on service", 376, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on service", 377, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on service", 380, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on service", 383, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on service", 386, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on service", 389, 1);
    }

    @Test
    public void testInvalidAttachmentOnResource() {
        int index = 116;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on resource, function", 394, 5);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on resource, function", 397, 5);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on resource, function", 400, 5);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on resource, function", 403, 5);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on resource, function", 406, 5);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on resource, function", 407, 5);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on resource, function", 410, 5);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on resource, function", 413, 5);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on resource, function", 416, 5);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on resource, function", 419, 5);
        validateError(compileResult, index, "annotation 'v13' is not allowed on resource, function", 422, 5);
    }

    @Test
    public void testInvalidAttachmentOnAnnotation() {
        int index = 127;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on annotation", 446, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on annotation", 449, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on annotation", 452, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on annotation", 455, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on annotation", 458, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on annotation", 461, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on annotation", 464, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on annotation", 465, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on annotation", 468, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on annotation", 471, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on annotation", 474, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on annotation", 477, 1);
    }

    @Test
    public void testInvalidAttachmentOnVar() {
        int index = 139;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on var", 482, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on var", 485, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on var", 488, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on var", 491, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on var", 494, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on var", 497, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on var", 500, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on var", 501, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on var", 504, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on var", 507, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on var", 510, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on var", 513, 1);
    }

    @Test
    public void testInvalidAttachmentOnConst() {
        int index = 151;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on const", 518, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on const", 521, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on const", 524, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on const", 527, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on const", 530, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on const", 533, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on const", 536, 1);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on const", 537, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on const", 540, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on const", 543, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on const", 546, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on const", 549, 1);
    }

    @Test
    public void testInvalidAttachmentOnExternal() {
        int index = 163;
        validateError(compileResult, index++, "annotation 'v1' is not allowed on external", 554, 62);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on external", 557, 61);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on external", 560, 61);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on external", 563, 61);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on external", 566, 61);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on external", 569, 61);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on external", 572, 61);
        validateError(compileResult, index++, "annotation 'v8' is not allowed on external", 573, 61);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on external", 576, 61);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on external", 579, 61);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on external", 582, 61);
        validateError(compileResult, index, "annotation 'v12' is not allowed on external", 585, 61);
    }

    @Test
    public void testInvalidAttachmentOnServiceVariable() {
        int index = 175;
        validateError(compileResult, index++, "annotation 'v8' is not allowed on var", 589, 1);
        validateError(compileResult, index++, "annotation 'v1' is not allowed on service", 593, 1);
        validateError(compileResult, index++, "annotation 'v2' is not allowed on service", 596, 1);
        validateError(compileResult, index++, "annotation 'v3' is not allowed on service", 599, 1);
        validateError(compileResult, index++, "annotation 'v4' is not allowed on service", 602, 1);
        validateError(compileResult, index++, "annotation 'v5' is not allowed on service", 605, 1);
        validateError(compileResult, index++, "annotation 'v6' is not allowed on service", 608, 1);
        validateError(compileResult, index++, "annotation 'v7' is not allowed on service", 611, 1);
        validateError(compileResult, index++, "annotation 'v9' is not allowed on service", 612, 1);
        validateError(compileResult, index++, "annotation 'v10' is not allowed on service", 615, 1);
        validateError(compileResult, index++, "annotation 'v11' is not allowed on service", 618, 1);
        validateError(compileResult, index++, "annotation 'v12' is not allowed on service", 621, 1);
        validateError(compileResult, index, "annotation 'v13' is not allowed on service", 624, 1);
    }
}
