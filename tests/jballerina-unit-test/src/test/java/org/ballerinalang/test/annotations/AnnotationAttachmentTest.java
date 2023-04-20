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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.TypeDefinition;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.desugar.AnnotationDesugar;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class to test annotation attachments.
 *
 * @since 1.0
 */
public class AnnotationAttachmentTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/annotations/annot_attachments.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
    }

    @Test
    public void testAnnotOnType() {
        List<BLangAnnotationAttachment> attachments =
                getTypeDefinition(compileResult.getAST().getTypeDefinitions(), "T1").getAnnotationAttachments();
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v1", "val", "v1 value");
    }

    @Test
    public void testAnnotOnObjectType() {
        List<BLangAnnotationAttachment> attachments =
                getClassDefinition(((BLangPackage) compileResult.getAST()).topLevelNodes, "T2")
                        .getAnnotationAttachments();
        Assert.assertEquals(attachments.size(), 2);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v1", "val", "v1 value object");
        assertAnnotationNameAndKeyValuePair(attachments.get(1), "v2", "val", "v2 value");
    }

    @Test
    public void testAnnotOnObjectFunction() {
        BLangFunction function = getFunction("T2.setName");
        List<BLangAnnotationAttachment> attachments = function.annAttachments;
        Assert.assertEquals(attachments.size(), 2);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v3", "val", "v31 value");
        assertAnnotationNameAndKeyValuePair(attachments.get(1), "v4", "val", 41L);

        attachments = function.requiredParams.get(0).annAttachments;
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v6", "val", "v61 value required");

        attachments = function.requiredParams.get(1).annAttachments;
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v6", "val", "v61 value defaultable");

        attachments = function.restParam.annAttachments;
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v6", "val", "v61 value rest");

        attachments = function.returnTypeAnnAttachments;
        Assert.assertEquals(attachments.size(), 1);
        BLangAnnotationAttachment attachment = attachments.get(0);
        Assert.assertEquals(attachment.annotationName.getValue(), "v7");
        Assert.assertNull(attachment.expr);
    }

    @Test
    public void testAnnotOnFunction() {
        BLangFunction function = getFunction("func");
        List<BLangAnnotationAttachment> attachments = function.annAttachments;
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v3", "val", "v33 value");

        attachments = function.requiredParams.get(0).annAttachments;
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v6", "val", "v63 value required");

        attachments = function.requiredParams.get(1).annAttachments;
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v6", "val", "v63 value defaultable");

        attachments = function.restParam.annAttachments;
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v6", "val", "v63 value rest");

        attachments = function.returnTypeAnnAttachments;
        Assert.assertEquals(attachments.size(), 1);
        BLangAnnotationAttachment attachment = attachments.get(0);
        Assert.assertEquals(attachment.annotationName.getValue(), "v7");
        Assert.assertNull(attachment.expr);
    }

    @Test
    public void testAnnotOnListener() {
        List<BLangAnnotationAttachment> attachments = new ArrayList<>();
        for (BLangVariable globalVar : compileResult.getAST().getGlobalVariables()) {
            if (((BLangSimpleVariable) globalVar).getName().getValue().equals("lis")) {
                attachments.addAll(globalVar.getAnnotationAttachments());
            }
        }
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v9", "val", "v91");
    }

    @Test
    public void testAnnotOnServiceOne() {
        List<BLangAnnotationAttachment> attachments = (List<BLangAnnotationAttachment>)
                compileResult.getAST().getServices().stream()
                        .filter(serviceNode ->
                                serviceNode.getAbsolutePath().stream().anyMatch(p -> p.getValue().contains("ser")))
                        .findFirst()
                        .get().getServiceClass().getAnnotationAttachments()
                        .stream()
                        .filter(ann -> !isServiceIntropAnnot((BLangAnnotationAttachment) ann))
                        .collect(Collectors.toList());
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v8", "val", "v8");
    }

    @Test
    public void testIntrospectionInfoAnnot() {
        Optional<ServiceNode> serviceDeclarationOpt = (Optional<ServiceNode>) compileResult.getAST()
                .getServices().stream()
                .filter(serviceNode ->
                        serviceNode.getAbsolutePath().stream().anyMatch(p -> p.getValue().contains("introspection")))
                .findFirst();
        Assert.assertTrue(serviceDeclarationOpt.isPresent());
        ServiceNode serviceDeclaration = serviceDeclarationOpt.get();
        BSymbol symbol = ((BLangService) serviceDeclaration).symbol;
        String serviceName = symbol.getOriginalName().getValue();
        PackageID moduleId = symbol.pkgID;
        Location position = serviceDeclaration.getPosition();
        String serviceId = String.format("%d", Objects.hash(serviceName, moduleId, position.lineRange()));
        List<BLangAnnotationAttachment> attachments = (List<BLangAnnotationAttachment>)
                serviceDeclaration.getServiceClass().getAnnotationAttachments();
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0),
                "IntrospectionDocConfig", "name", serviceId);
    }

    private boolean isServiceIntropAnnot(BLangAnnotationAttachment annot) {
        return AnnotationDesugar.SERVICE_INTROSPECTION_INFO_ANN.equals(annot.annotationName.value);
    }

    @Test
    public void testAnnotOnResourceOne() {
        BLangFunction function = getFunction("$anonType$_0.$get$res");
        List<BLangAnnotationAttachment> attachments = function.annAttachments;
        Assert.assertEquals(attachments.size(), 2);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v3", "val", "v34");
        assertAnnotationNameAndKeyValuePair(attachments.get(1), "v5", "val", "54");

        attachments = function.requiredParams.get(0).annAttachments;
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v6", "val", "v64");

        attachments = function.returnTypeAnnAttachments;
        Assert.assertEquals(attachments.size(), 1);
        BLangAnnotationAttachment attachment = attachments.get(0);
        Assert.assertEquals(attachment.annotationName.getValue(), "v7");
        Assert.assertNull(attachment.expr);
    }

    @Test
    public void testAnnotOnServiceTwo() {
        List<BLangAnnotationAttachment> attachments = (List<BLangAnnotationAttachment>)
                compileResult.getAST().getClassDefinitions().stream()
                        .filter(classNode -> classNode.getName().getValue().equals("$anonType$_1"))
                        .findFirst()
                        .get().getAnnotationAttachments()
                        .stream()
                        .filter(ann -> !isServiceIntropAnnot((BLangAnnotationAttachment) ann))
                        .collect(Collectors.toList());
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v8", "val", "v82");
    }

    @Test
    public void testAnnotOnResourceTwo() {
        BLangFunction function = getFunction("$anonType$_1.$get$res");
        List<BLangAnnotationAttachment> attachments = function.annAttachments;
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v5", "val", "542");

        attachments = function.requiredParams.get(0).annAttachments;
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v6", "val", "v642");

        attachments = function.returnTypeAnnAttachments;
        Assert.assertEquals(attachments.size(), 1);
        BLangAnnotationAttachment attachment = attachments.get(0);
        Assert.assertEquals(attachment.annotationName.getValue(), "v7");
        Assert.assertNull(attachment.expr);
    }

    @Test
    public void testAnnotOnAnnot() {
        List<BLangAnnotationAttachment> attachments = (List<BLangAnnotationAttachment>)
                compileResult.getAST().getAnnotations().stream()
                .filter(annotationNode ->  annotationNode.getName().toString().equals("v14"))
                .findFirst()
                .get().getAnnotationAttachments();
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v10", "str", "v10 value");
    }

    @Test
    public void testAnnotOnVar() {
        List<BLangAnnotationAttachment> attachments = new ArrayList<>();
        List<String> targetVariables = new ArrayList<>(Arrays.asList("i", "intVar", "stringVar", "myA", "message",
                "errorNo"));
        for (BLangVariable globalVar : compileResult.getAST().getGlobalVariables()) {
            if (targetVariables.contains(((BLangSimpleVariable) globalVar).getName().getValue())) {
                attachments.addAll(globalVar.getAnnotationAttachments());
            }
        }
        Assert.assertEquals(attachments.size(), 6);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v11", "val", 11L);
        assertAnnotationNameAndKeyValuePair(attachments.get(1), "v11", "val", 2L);
        assertAnnotationNameAndKeyValuePair(attachments.get(2), "v11", "val", 2L);
        assertAnnotationNameAndKeyValuePair(attachments.get(3), "v11", "val", 3L);
        assertAnnotationNameAndKeyValuePair(attachments.get(4), "v11", "val", 4L);
        assertAnnotationNameAndKeyValuePair(attachments.get(5), "v11", "val", 4L);
    }

    @Test
    public void testAnnotOnConst() {
        List<BLangAnnotationAttachment> attachments =
                ((BLangConstant) compileResult.getAST().getConstants().stream()
                        .filter(constant -> ((BLangConstant) constant).name.toString().equals("F"))
                        .findFirst()
                        .get())
                        .getAnnotationAttachments();
        Assert.assertEquals(attachments.size(), 1);
        assertAnnotationNameAndKeyValuePair(attachments.get(0), "v12", "str", "v12 value");
    }

    @Test
    public void testAnnotOnExternal() {
        BLangFunction function = getFunction("externalFunction");
        List<BLangAnnotationAttachment> attachments = function.annAttachments;
        Assert.assertEquals(attachments.size(), 0);

        attachments = function.returnTypeAnnAttachments;
        Assert.assertEquals(attachments.size(), 1);
        BLangAnnotationAttachment attachment = attachments.get(0);
        Assert.assertEquals(attachment.annotationName.getValue(), "v7");
        Assert.assertNull(attachment.expr);

        attachments = ((BLangExternalFunctionBody) function.body).annAttachments;
        Assert.assertEquals(attachments.size(), 2);
        BLangAnnotationAttachment externalAttachment = attachments.get(0);
        if (!externalAttachment.annotationName.getValue().equals("v13")) {
            externalAttachment = attachments.get(1);
        }

        BLangRecordLiteral mappingConstructor = getMappingConstructor(externalAttachment, "v13");
        Assert.assertEquals(mappingConstructor.getFields().size(), 2);
        BLangRecordLiteral.BLangRecordKeyValueField keyValuePairOne =
                (BLangRecordLiteral.BLangRecordKeyValueField) mappingConstructor.getFields().get(0);
        BLangRecordLiteral.BLangRecordKeyValueField keyValuePairTwo =
                (BLangRecordLiteral.BLangRecordKeyValueField) mappingConstructor.getFields().get(1);

        Object strOneValue;
        Object strTwoValue;
        if (getKeyString(keyValuePairOne).equals("strOne")) {
            strOneValue = ((BLangLiteral) keyValuePairOne.getValue()).value;
            strTwoValue = ((BLangLiteral) keyValuePairTwo.getValue()).value;
        } else {
            strTwoValue = ((BLangLiteral) keyValuePairOne.getValue()).value;
            strOneValue = ((BLangLiteral) keyValuePairTwo.getValue()).value;
        }
        Assert.assertEquals(strOneValue, "one");
        Assert.assertEquals(strTwoValue, "two");
    }

    private void assertAnnotationNameAndKeyValuePair(BLangAnnotationAttachment attachment, String annotName,
                                                     String fieldName, Object value) {
        BLangRecordLiteral recordLiteral = getMappingConstructor(attachment, annotName);
        Assert.assertEquals(recordLiteral.getFields().size(), 1);

        BLangRecordLiteral.BLangRecordKeyValueField keyValuePair =
                (BLangRecordLiteral.BLangRecordKeyValueField) recordLiteral.getFields().get(0);
        Assert.assertEquals(getKeyString(keyValuePair), fieldName);
        Assert.assertEquals(((BLangLiteral) keyValuePair.getValue()).value, value);
    }


    private BLangRecordLiteral getMappingConstructor(BLangAnnotationAttachment attachment, String annotName) {
        Assert.assertEquals(attachment.annotationName.getValue(), annotName);
        BLangExpression expression = getActualExpressionFromAnnotationAttachmentExpr(attachment.expr);
        Assert.assertEquals(expression.getKind(), NodeKind.RECORD_LITERAL_EXPR);
        return (BLangRecordLiteral) expression;
    }

    private BLangExpression getActualExpressionFromAnnotationAttachmentExpr(BLangExpression expression) {
        if (expression.getKind() == NodeKind.TYPE_CONVERSION_EXPR) {
            BLangTypeConversionExpr expr = (BLangTypeConversionExpr) expression;
            if (expr.getKind() == NodeKind.INVOCATION) {
                return ((BLangInvocation) expr.expr).expr;
            }
        }
        if (expression.getKind() == NodeKind.INVOCATION) {
            return ((BLangInvocation) expression).expr;
        }
        return expression;
    }

    private String getKeyString(BLangRecordLiteral.BLangRecordKeyValueField keyValuePair) {
        return keyValuePair.getKey() instanceof BLangSimpleVarRef ?
                ((BLangSimpleVarRef) keyValuePair.key.expr).variableName.value :
                ((BLangLiteral) keyValuePair.getKey()).value.toString();
    }

    private BLangFunction getFunction(String functionName) {
        return (BLangFunction) compileResult.getAST().getFunctions().stream()
                .filter(function ->  functionName.equals(((BLangFunction) function).symbol.getName().toString()))
                .findFirst()
                .get();
    }

    @Test
    public void testAnnotsWithConstLists() {
        CompileResult result = BCompileUtil.compile("test-src/annotations/annots_with_list_consts.bal");
        List<BLangAnnotationAttachment> attachments = (List<BLangAnnotationAttachment>) result.getAST()
                .getClassDefinitions().get(0).getAnnotationAttachments()
                .stream()
                .filter(ann -> !isServiceIntropAnnot((BLangAnnotationAttachment) ann))
                .collect(Collectors.toList());
        Assert.assertEquals(attachments.size(), 1);
        BLangAnnotationAttachment attachment = attachments.get(0);
        BLangRecordLiteral recordLiteral = getMappingConstructor(attachment, "v1");
        Assert.assertEquals(recordLiteral.getFields().size(), 2);

        BLangRecordLiteral.BLangRecordKeyValueField keyValuePair =
                (BLangRecordLiteral.BLangRecordKeyValueField) recordLiteral.getFields().get(0);
        Assert.assertEquals(getKeyString(keyValuePair), "f1");
        BLangExpression expression = keyValuePair.valueExpr;
        Assert.assertEquals(expression.getKind(), NodeKind.TUPLE_LITERAL_EXPR);
        BLangListConstructorExpr listConstructorExpr = (BLangListConstructorExpr) expression;
        Assert.assertEquals(listConstructorExpr.exprs.size(), 2);

        BLangExpression element = listConstructorExpr.exprs.get(0);
        Assert.assertEquals(element.getKind(), NodeKind.RECORD_LITERAL_EXPR);
        BLangRecordLiteral elementRecordLiteral = (BLangRecordLiteral) element;
        Assert.assertEquals(elementRecordLiteral.getFields().size(), 2);
        keyValuePair = (BLangRecordLiteral.BLangRecordKeyValueField) elementRecordLiteral.getFields().get(0);
        Assert.assertEquals(getKeyString(keyValuePair), "s");
        Assert.assertEquals(((BLangLiteral) keyValuePair.getValue()).value, "s");
        keyValuePair = (BLangRecordLiteral.BLangRecordKeyValueField) elementRecordLiteral.getFields().get(1);
        Assert.assertEquals(getKeyString(keyValuePair), "t");
        Assert.assertEquals(((BLangLiteral) keyValuePair.getValue()).value, "t");

        element = listConstructorExpr.exprs.get(1);
        Assert.assertEquals(element.getKind(), NodeKind.RECORD_LITERAL_EXPR);
        elementRecordLiteral = (BLangRecordLiteral) element;
        Assert.assertEquals(elementRecordLiteral.getFields().size(), 2);
        keyValuePair = (BLangRecordLiteral.BLangRecordKeyValueField) elementRecordLiteral.getFields().get(0);
        Assert.assertEquals(getKeyString(keyValuePair), "s");
        Assert.assertEquals(((BLangLiteral) keyValuePair.getValue()).value, "s2");
        keyValuePair = (BLangRecordLiteral.BLangRecordKeyValueField) elementRecordLiteral.getFields().get(1);
        Assert.assertEquals(getKeyString(keyValuePair), "t");
        Assert.assertEquals(((BLangLiteral) keyValuePair.getValue()).value, "t2");

        keyValuePair = (BLangRecordLiteral.BLangRecordKeyValueField) recordLiteral.getFields().get(1);
        Assert.assertEquals(getKeyString(keyValuePair), "f2");
        expression = keyValuePair.valueExpr;
        Assert.assertEquals(expression.getKind(), NodeKind.TUPLE_LITERAL_EXPR);
        listConstructorExpr = (BLangListConstructorExpr) expression;
        Assert.assertEquals(listConstructorExpr.exprs.size(), 2);

        element = listConstructorExpr.exprs.get(0);
        Assert.assertEquals(element.getKind(), NodeKind.RECORD_LITERAL_EXPR);
        elementRecordLiteral = (BLangRecordLiteral) element;
        Assert.assertEquals(elementRecordLiteral.getFields().size(), 2);
        keyValuePair = (BLangRecordLiteral.BLangRecordKeyValueField) elementRecordLiteral.getFields().get(0);
        Assert.assertEquals(getKeyString(keyValuePair), "s");
        Assert.assertEquals(((BLangLiteral) keyValuePair.getValue()).value, "s3");
        keyValuePair = (BLangRecordLiteral.BLangRecordKeyValueField) elementRecordLiteral.getFields().get(1);
        Assert.assertEquals(getKeyString(keyValuePair), "t");
        Assert.assertEquals(((BLangLiteral) keyValuePair.getValue()).value, "t3");

        element = listConstructorExpr.exprs.get(1);
        Assert.assertEquals(element.getKind(), NodeKind.LITERAL);
        Assert.assertEquals(((BLangLiteral) element).value, "test");
    }

    @Test
    public void testAnnotWithEmptyMapConstructorOnType() {
        List<BLangAnnotationAttachment> attachments =
                getTypeDefinition(compileResult.getAST().getTypeDefinitions(), "MyType").getAnnotationAttachments();
        validateEmptyMapConstructorExprInAnnot(attachments, "v16", "A");
    }

    @Test
    public void testAnnotWithEmptyMapConstructorOnFunction() {
        BLangFunction function = getFunction("myFunction1");
        validateEmptyMapConstructorExprInAnnot(function.annAttachments, "v17", "A");
        validateEmptyMapConstructorExprInAnnot(function.requiredParams.get(0).annAttachments, "v19", "A");
    }

    @Test
    public void testAnnotWithEmptyMapConstructorOnService() {
        List<BLangAnnotationAttachment> attachments = (List<BLangAnnotationAttachment>)
                compileResult.getAST().getClassDefinitions().stream()
                        .filter(classNode -> classNode.getName().getValue().equals("$anonType$_3"))
                        .findFirst()
                        .get().getAnnotationAttachments()
                        .stream()
                        .filter(ann -> !isServiceIntropAnnot((BLangAnnotationAttachment) ann))
                        .collect(Collectors.toList());
        validateEmptyMapConstructorExprInAnnot(attachments, "v20", "A");
    }

    @Test
    public void testAnnotWithEmptyMapConstructorOnResource() {
        BLangFunction function = getFunction("$anonType$_2.$get$res");
        validateEmptyMapConstructorExprInAnnot(function.annAttachments, "v18", "A");
        validateEmptyMapConstructorExprInAnnot(function.requiredParams.get(0).annAttachments, "v19", "A");
    }

    @Test
    public void testAnnotWithEmptyMapConstructorOnFunction2() {
        BLangFunction function = getFunction("myFunction2");
        validateEmptyMapConstructorExprInAnnot(function.annAttachments, "v21", "map");
        validateEmptyMapConstructorExprInAnnot(function.requiredParams.get(0).annAttachments, "v22", "map");
    }

    @Test
    public void testAnnotWithEmptyMapConstructorOnFunction3() {
        BLangFunction function = getFunction("myFunction3");
        validateEmptyMapConstructorExprInAnnot(function.annAttachments, "v23", "A");
    }

    @Test
    public void testAnnotWithEmptyMapConstructorOnFunction4() {
        BLangFunction function = getFunction("myFunction4");
        validateEmptyMapConstructorExprInAnnot(function.annAttachments, "v17", "A");
        validateEmptyMapConstructorExprInAnnot(function.requiredParams.get(0).annAttachments, "v19", "A");
    }

    @Test
    public void testAnnotWithEmptyMapConstructorOnFunction5() {
        BLangFunction function = getFunction("myFunction5");
        validateEmptyMapConstructorExprInAnnot(function.annAttachments, "v23", "A");
    }

    @Test
    public void testAnnotWithEmptyMapConstructorOnFunction6() {
        BLangFunction function = getFunction("myFunction6");
        validateEmptyMapConstructorExprInAnnot(function.annAttachments, "v24", "C");
    }

    public void validateEmptyMapConstructorExprInAnnot(List<BLangAnnotationAttachment> attachments,
                                                       String annotationName, String typeName) {
        int i = 0;
        for (BLangAnnotationAttachment attachment : attachments) {
            Assert.assertEquals(attachment.annotationName.getValue(), annotationName);
            BLangExpression expression = ((BLangInvocation) attachment.expr).expr;
            Assert.assertEquals(expression.getKind(), NodeKind.RECORD_LITERAL_EXPR);
            BLangRecordLiteral recordLiteral = (BLangRecordLiteral) expression;
            Assert.assertEquals(recordLiteral.getFields().size(), 0);
            Assert.assertTrue(getConstrainedTypeFromRef(recordLiteral.getBType()).tag == TypeTags.RECORD
                    || getConstrainedTypeFromRef(recordLiteral.getBType()).tag == TypeTags.MAP);
            if (getConstrainedTypeFromRef(recordLiteral.getBType()).tag == TypeTags.RECORD) {
                Assert.assertEquals(recordLiteral.getBType().tsymbol.name.value, typeName);
            } else {
                Assert.assertEquals(getConstrainedTypeFromRef(recordLiteral.getBType()).tag, TypeTags.MAP);
                Assert.assertEquals(((BMapType) getConstrainedTypeFromRef(recordLiteral.getBType())).constraint.tag,
                        TypeTags.INT);
            }
            i++;
        }
        Assert.assertEquals(attachments.size(), i);
    }

   private BType getConstrainedTypeFromRef(BType type) {
       BType constraint = type;
       if (type.tag == org.wso2.ballerinalang.compiler.util.TypeTags.TYPEREFDESC) {
           constraint = ((BTypeReferenceType) type).referredType;
       }
       return constraint.tag == org.wso2.ballerinalang.compiler.util.TypeTags.TYPEREFDESC ?
               getConstrainedTypeFromRef(constraint) : constraint;
   }

    @Test
    public void testAnnotWithNullValues() {
        BLangFunction function = getFunction("fooFunction");
        Assert.assertEquals(function.annAttachments.size(), 1);
        Assert.assertEquals(function.annAttachments.get(0).annotationName.getValue(), "f2");
        BLangExpression expression = ((BLangInvocation) function.annAttachments.get(0).expr).expr;
        Assert.assertEquals(expression.getKind(), NodeKind.RECORD_LITERAL_EXPR);
        BLangRecordLiteral recordLiteral = (BLangRecordLiteral) expression;
        List<RecordLiteralNode.RecordField> recordFields = recordLiteral.getFields();
        Assert.assertEquals(recordFields.size(), 2);
        BLangRecordLiteral.BLangRecordKeyValueField keyValuePair =
                (BLangRecordLiteral.BLangRecordKeyValueField) recordFields.get(0);
        Assert.assertEquals(getKeyString(keyValuePair), "s1");
        Assert.assertEquals(((BLangLiteral) keyValuePair.getValue()).value, "str");
        keyValuePair = (BLangRecordLiteral.BLangRecordKeyValueField) recordFields.get(1);
        Assert.assertEquals(getKeyString(keyValuePair), "s2");
        Assert.assertNull(((BLangLiteral) keyValuePair.getValue()).value);
    }

    @Test
    public void testAnnotOnTupleMember() {
        BLangTupleTypeNode tp = (BLangTupleTypeNode) getTypeDefinition(
                compileResult.getAST().getTypeDefinitions(), "Tp").getTypeNode();
        BLangSimpleVariable m1 = tp.getMemberNodes().get(0);
        Assert.assertEquals(m1.annAttachments.size(), 1);
        Assert.assertEquals(m1.annAttachments.get(0).annotationName.getValue(), "v30");
    }

    private BLangTypeDefinition getTypeDefinition(List<? extends TypeDefinition> typeDefinitions, String name) {
        for (TypeDefinition typeDefinition : typeDefinitions) {
            BLangTypeDefinition bLangTypeDefinition = (BLangTypeDefinition) typeDefinition;
            if (name.equals(bLangTypeDefinition.symbol.name.value)) {
                return bLangTypeDefinition;
            }
        }
        throw new RuntimeException("Type Definition '" + name + "' not found.");
    }

    private BLangClassDefinition getClassDefinition(List<? extends TopLevelNode> typeDefinitions, String name) {
        for (TopLevelNode topLevelNode : typeDefinitions) {
            if (topLevelNode.getKind() != NodeKind.CLASS_DEFN) {
                continue;
            }

            BLangClassDefinition classDefinition = (BLangClassDefinition) topLevelNode;
            if (name.equals(classDefinition.symbol.name.value)) {
                return classDefinition;
            }
        }
        throw new RuntimeException("Class Definition '" + name + "' not found.");
    }
}
