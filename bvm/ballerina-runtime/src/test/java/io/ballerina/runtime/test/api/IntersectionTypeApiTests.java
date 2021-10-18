/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.test.api;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.ErrorType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test cases for runtime APIs related to intersection type.
 *
 * @since 2.0.0
 */
public class IntersectionTypeApiTests {

    private static final Module module = new Module("myOrg", "test_module", "1.0.0");

    @Test
    public void createIntersectionTypeWithPrimitives() {
        BIntersectionType bIntersectionType =
                new BIntersectionType(module, new BType[]{}, PredefinedTypes.TYPE_INT, 0, true);
        Assert.assertEquals(bIntersectionType.getTag(), TypeTags.INTERSECTION_TAG);
        Assert.assertEquals(bIntersectionType.getEffectiveType(), PredefinedTypes.TYPE_INT);
    }

    @Test
    public void createIntersectionTypeWithStructuredTypes() {
        ArrayType arrayType = TypeCreator.createArrayType(PredefinedTypes.TYPE_INT);
        BIntersectionType bIntersectionType1 =
                new BIntersectionType(module, new BType[]{}, arrayType, 0, true);
        Assert.assertEquals(bIntersectionType1.getTag(), TypeTags.INTERSECTION_TAG);
        Assert.assertEquals(bIntersectionType1.getEffectiveType(), arrayType);
        Assert.assertTrue(arrayType.getIntersectionType().isPresent());
        Assert.assertEquals(arrayType.getIntersectionType().get(), bIntersectionType1);
    }

    @Test
    public void createIntersectionTypeWithIntersectionType() {
        ArrayType arrayType = TypeCreator.createArrayType(PredefinedTypes.TYPE_INT);
        BIntersectionType bIntersectionType1 =
                new BIntersectionType(module, new Type[]{arrayType}, arrayType, 0, true);
        BIntersectionType bIntersectionType2 =
                new BIntersectionType(module, new Type[]{bIntersectionType1}, bIntersectionType1, 0, true);
        Assert.assertEquals(bIntersectionType2.getTag(), TypeTags.INTERSECTION_TAG);
        Assert.assertEquals(bIntersectionType2.getEffectiveType(), bIntersectionType1);
        Assert.assertTrue(arrayType.getIntersectionType().isPresent());
        Assert.assertTrue(bIntersectionType1.getIntersectionType().isPresent());
        Assert.assertFalse(bIntersectionType2.getIntersectionType().isPresent());
        Assert.assertEquals(arrayType.getIntersectionType().get(), bIntersectionType1);
        Assert.assertEquals(bIntersectionType1.getIntersectionType().get(), bIntersectionType2);
        Assert.assertNotEquals(arrayType.getIntersectionType().get(), bIntersectionType2);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Unsupported " +
            "intersection type found: \\(test_module:TestError & error\\).*")
    public void testNonReadonlyIntersectionType() {
        ErrorType type1 = TypeCreator.createErrorType("TestError", module, PredefinedTypes.TYPE_MAP);
        BIntersectionType bIntersectionType =
                new BIntersectionType(module, new Type[]{type1, PredefinedTypes.TYPE_ERROR},
                        PredefinedTypes.TYPE_READONLY, 0, true);
        ReadOnlyUtils.getMutableType(bIntersectionType);
    }

    @Test
    public void getConstituentTypesAPITest() {
        ArrayType arrayType = TypeCreator.createArrayType(PredefinedTypes.TYPE_INT);
        IntersectionType intersectionType =
                new BIntersectionType(module, new Type[]{arrayType, PredefinedTypes.TYPE_READONLY},
                        arrayType, 0, true);
        List<Type> constituentTypes = intersectionType.getConstituentTypes();
        Assert.assertEquals(constituentTypes.size(), 2);
        Assert.assertEquals(constituentTypes.get(0), arrayType);
        Assert.assertEquals(constituentTypes.get(1), PredefinedTypes.TYPE_READONLY);
    }
}
