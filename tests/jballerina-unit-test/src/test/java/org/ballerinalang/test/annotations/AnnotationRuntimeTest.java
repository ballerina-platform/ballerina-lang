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

//import org.ballerinalang.compiler.CompilerPhase;
//import org.ballerinalang.core.model.values.BBoolean;
//import org.ballerinalang.core.model.values.BValue;
//import io.ballerina.runtime.StringUtils;
//import io.ballerina.runtime.TypeChecker;
//import io.ballerina.runtime.types.AnnotatableType;
//import io.ballerina.runtime.types.TypeTags;
//import io.ballerina.runtime.values.MapValueImpl;
//import io.ballerina.runtime.values.TupleValueImpl;
//import io.ballerina.runtime.values.TypedescValue;
//import io.ballerina.runtime.values.api.BString;
//import org.ballerinalang.test.util.BCompileUtil;
//import org.ballerinalang.test.util.BRunUtil;
//import org.ballerinalang.test.util.CompileResult;
//import org.testng.Assert;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
//import org.wso2.ballerinalang.compiler.tree.BLangPackage;
//import org.wso2.ballerinalang.compiler.tree.BLangService;
//import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
//import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

/**
 * Class to test annotation access and availability at runtime.
 *
 * @since 1.0
 */
@Test
public class AnnotationRuntimeTest {

//    private CompileResult resultOne;
//    private CompileResult resultTwo;
//    private CompileResult resultThree;
//    private CompileResult resultFour;
//    private CompileResult resultAccessNegative;
//    private CompileResult readOnlyValues;
//
//    @BeforeClass
//    public void setup() {
//        resultOne = BCompileUtil.compile("test-src/annotations/annot_access.bal");
//        Assert.assertEquals(resultOne.getErrorCount(), 0);
//
//        resultTwo = BCompileUtil.compile("test-src/annotations/annot_access_with_source_only_points.bal");
//        Assert.assertEquals(resultTwo.getErrorCount(), 0);
//
//        resultThree = BCompileUtil.compile("test-src/annotations/annotations_constant_propagation.bal",
//                CompilerPhase.COMPILER_PLUGIN);
//        Assert.assertEquals(resultThree.getErrorCount(), 0);
//
//        resultFour = BCompileUtil.compile("test-src/annotations/annot_availability.bal");
//        Assert.assertEquals(resultFour.getErrorCount(), 0);
//
//        resultAccessNegative = BCompileUtil.compile("test-src/annotations/annotation_access_negative.bal");
//
//        readOnlyValues = BCompileUtil.compile("test-src/annotations/annotation_readonly_types.bal");
//    }
//
//    @Test(dataProvider = "annotAccessTests")
//    public void testAnnotAccess(String testFunction) {
//        BValue[] returns = BRunUtil.invoke(resultOne, testFunction);
//        Assert.assertEquals(returns.length, 1);
//        Assert.assertSame(returns[0].getClass(), BBoolean.class);
//        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
//    }
//
//    @Test(description = "test accessing source only annots at runtime, the annots should not be available",
//            dataProvider = "annotAccessWithSourceOnlyPointsTests")
//    public void testSourceOnlyAnnotAccess(String testFunction) {
//        BValue[] returns = BRunUtil.invoke(resultTwo, testFunction);
//        Assert.assertEquals(returns.length, 1);
//        Assert.assertSame(returns[0].getClass(), BBoolean.class);
//        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
//    }
//
//    @Test(description = "Test if constants used in annotation are replaced on constant propagation phase")
//    public void testConstantPropagationOnAnnotation() {
//        BLangPackage pkg = (BLangPackage) resultThree.getAST();
//
//        BLangService service = pkg.services.get(0);
//        BLangRecordLiteral serviceAnnotation = (BLangRecordLiteral) service.annAttachments.get(0).expr;
//        Assert.assertTrue(((BLangRecordLiteral.BLangRecordKeyValueField) serviceAnnotation.fields.get(0)).valueExpr
//                instanceof BLangConstRef);
//
//        BLangRecordLiteral helloFunctionAnnotation = (BLangRecordLiteral) service.resourceFunctions.get(0)
//                .annAttachments.get(0).expr;
//        Assert.assertTrue(((BLangRecordLiteral.BLangRecordKeyValueField) helloFunctionAnnotation.fields.get(0))
//                .valueExpr instanceof BLangConstRef);
//
//    }
//
//    @DataProvider(name = "annotAccessTests")
//    public Object[][] annotAccessTests() {
//        return new Object[][]{
//                { "testTypeAnnotAccess1" },
//                { "testTypeAnnotAccess2" },
//                { "testObjectTypeAnnotAccess1" },
//                { "testObjectTypeAnnotAccess2" },
//                { "testObjectTypeAnnotAccess3" },
//                { "testServiceAnnotAccess1" },
//                { "testServiceAnnotAccess2" },
//                { "testServiceAnnotAccess3" },
//                { "testServiceAnnotAccess4" },
//                { "testFunctionAnnotAccess1" },
//                { "testFunctionAnnotAccess2" }
//                // { "testInlineAnnotAccess" } // TODO: #17936
//        };
//    }
//
//    @DataProvider(name = "annotAccessWithSourceOnlyPointsTests")
//    public Object[][] annotAccessWithSourceOnlyPointsTests() {
//        return new Object[][]{
//                { "testAnnotAccessForAnnotWithSourceOnlyPoints1" },
//                { "testAnnotAccessForAnnotWithSourceOnlyPoints2" },
//                { "testAnnotAccessForAnnotWithSourceOnlyPoints3" },
//                { "testAnnotAccessForAnnotWithSourceOnlyPoints4" },
//                { "testAnnotAccessForAnnotWithSourceOnlyPoints5" },
//                { "testAnnotAccessForAnnotWithSourceOnlyPoints6" }
//        };
//    }
//
//    @Test
//    public void testAnnotAvailabilty() {
//        Object obj = BRunUtil.invokeAndGetJVMResult(resultFour, "testStructureAnnots");
//        Assert.assertEquals(TypeChecker.getType(obj).getTag(), TypeTags.TUPLE_TAG);
//
//        TupleValueImpl tupleValue = (TupleValueImpl) obj;
//
//        AnnotatableType annotatableType = (AnnotatableType) ((TypedescValue) tupleValue.get(0)).getDescribingType();
//        Assert.assertEquals(annotatableType.getAnnotation(StringUtils.fromString("W")), true);
//
//        Object fieldAnnots = annotatableType.getAnnotation(StringUtils.fromString("$field$.i"));
//        Assert.assertEquals(TypeChecker.getType(fieldAnnots).getTag(), TypeTags.MAP_TAG);
//        MapValueImpl<BString, Object> fieldAnnotMap = (MapValueImpl<BString, Object>) fieldAnnots;
//
//        Object annotValue = fieldAnnotMap.get(StringUtils.fromString("Z"));
//        Assert.assertEquals(TypeChecker.getType(annotValue).getTag(), TypeTags.BOOLEAN_TAG);
//        Assert.assertTrue((Boolean) annotValue);
//
//        annotValue = fieldAnnotMap.get(StringUtils.fromString("X"));
//        Assert.assertEquals(TypeChecker.getType(annotValue).getTag(), TypeTags.MAP_TAG);
//
//        MapValueImpl<BString, Object> mapValue = (MapValueImpl<BString, Object>) annotValue;
//        Assert.assertEquals(mapValue.size(), 1);
//        Assert.assertEquals(mapValue.get(StringUtils.fromString("p")), 2L);
//
//        annotatableType = (AnnotatableType) ((TypedescValue) tupleValue.get(1)).getDescribingType();
//        Assert.assertEquals(annotatableType.getAnnotation(StringUtils.fromString("W")), true);
//        fieldAnnots = annotatableType.getAnnotation(StringUtils.fromString("$field$.j"));
//        Assert.assertEquals(TypeChecker.getType(fieldAnnots).getTag(), TypeTags.MAP_TAG);
//        fieldAnnotMap = (MapValueImpl<BString, Object>) fieldAnnots;
//
//        annotValue = fieldAnnotMap.get(StringUtils.fromString("Z"));
//        Assert.assertEquals(TypeChecker.getType(annotValue).getTag(), TypeTags.BOOLEAN_TAG);
//        Assert.assertTrue((Boolean) annotValue);
//
//        annotValue = fieldAnnotMap.get(StringUtils.fromString("Y"));
//        Assert.assertEquals(TypeChecker.getType(annotValue).getTag(), TypeTags.MAP_TAG);
//
//        mapValue = (MapValueImpl<BString, Object>) annotValue;
//        Assert.assertEquals(mapValue.size(), 2);
//        Assert.assertEquals(mapValue.get(StringUtils.fromString("q")).toString(), "hello");
//        Assert.assertEquals(mapValue.get(StringUtils.fromString("r")).toString(), "world");
//    }
//
//    public void testRecordTypeAnnotationReadonlyValueEdit() {
//        BRunUtil.invoke(resultAccessNegative, "testRecordTypeAnnotationReadonlyValueEdit");
//    }
//
//    public void testAnnotationOnObjectTypeReadonlyValueEdit() {
//        BRunUtil.invoke(resultAccessNegative, "testAnnotationOnObjectTypeReadonlyValueEdit");
//    }
//
//    public void testAnnotationOnFunctionTypeReadonlyValueEdit() {
//        BRunUtil.invoke(resultAccessNegative, "testAnnotationOnFunctionTypeReadonlyValueEdit");
//    }
//
//    public void testReadonlyTypeAnnotationAttachment() {
//        BRunUtil.invoke(readOnlyValues, "testReadonlyTypeAnnotationAttachment");
//    }
}
