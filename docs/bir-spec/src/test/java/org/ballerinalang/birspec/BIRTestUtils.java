/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.birspec;

import io.ballerina.tools.diagnostics.Location;
import io.kaitai.struct.ByteBufferKaitaiStream;
import io.kaitai.struct.KaitaiStruct;
import org.ballerinalang.build.kaitai.Bir;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BirScope;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility methods to help with testing BIR model.
 */
class BIRTestUtils {

    private static final String TEST_RESOURCES_ROOT = "src/test/resources/test-src";
    private static final Path TEST_RESOURCES_ROOT_PATH = Paths.get(TEST_RESOURCES_ROOT);

    private static final String LANG_LIB_TEST_SRC_ROOT = "../../langlib/langlib-test/src/test/resources/test-src";
    private static final Path LANG_LIB_TEST_ROOT_PATH = Paths.get(LANG_LIB_TEST_SRC_ROOT);

    @DataProvider(name = "createTestSources")
    public static Object[][] createTestDataProvider() throws IOException {
        assert LANG_LIB_TEST_ROOT_PATH.toFile().exists();
        List<String> testSources = findBallerinaSourceFiles(LANG_LIB_TEST_ROOT_PATH);

        assert TEST_RESOURCES_ROOT_PATH.toFile().exists();
        testSources.addAll(findBallerinaSourceFiles(TEST_RESOURCES_ROOT_PATH));

        Object[][] data = new Object[testSources.size()][];
        for (int i = 0; i < data.length; i++) {
            data[i] = new Object[]{testSources.get(i)};
        }
        return data;
    }

    private static List<String> findBallerinaSourceFiles(Path testSourcesPath) throws IOException {
        return Files.walk(testSourcesPath)
                .filter(file -> Files.isRegularFile(file))
                .map(file -> file.toAbsolutePath().normalize().toString())
                .filter(file -> file.endsWith(".bal") && !file.contains("negative") && !file.contains("subtype"))
                .collect(Collectors.toList());
    }

    static void validateBIRSpec(String testSource) {
        BCompileUtil.BIRCompileResult compileResult = BCompileUtil.generateBIR(testSource);
        Assert.assertNotNull(compileResult, "Compilation may contain errors!");

        BIRNode.BIRPackage expectedBIRModule = compileResult.getExpectedBIR();
        byte[] actualBIRbinary = compileResult.getActualBIR();
        Bir kaitaiBir = new Bir(new ByteBufferKaitaiStream(actualBIRbinary));


        ArrayList<Bir.ConstantPoolEntry> constantPoolEntries = kaitaiBir.constantPool().constantPoolEntries();
        Bir.Module actualBIRModule = kaitaiBir.module();

        // assert constants
        assertValues(expectedBIRModule, actualBIRModule, constantPoolEntries);

        // assert type definitions
        assertTypeDefs(expectedBIRModule, actualBIRModule, constantPoolEntries);

        // assert annotations
        assertAnnotations(expectedBIRModule, actualBIRModule, constantPoolEntries);

        // assert functions
        assertFunctions(expectedBIRModule, actualBIRModule, constantPoolEntries);

        // assert service decls
        assertServiceDecls(expectedBIRModule, actualBIRModule, constantPoolEntries);
    }

    private static void assertFunctions(BIRNode.BIRPackage expectedBIR, Bir.Module birModule,
                                        ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        List<BIRNode.BIRFunction> expectedFunctions = expectedBIR.functions;
        ArrayList<Bir.Function> actualFunctions = birModule.functions();
        Assert.assertEquals(birModule.functionCount(), expectedFunctions.size());

        for (int i = 0; i < expectedFunctions.size(); i++) {
            Bir.Function actualFunction = actualFunctions.get(i);
            BIRNode.BIRFunction expectedFunction = expectedFunctions.get(i);

            // assert name
            Bir.ConstantPoolEntry constantPoolEntryName = constantPoolEntries.get(actualFunction.nameCpIndex());
            assertConstantPoolEntry(constantPoolEntryName, expectedFunction.name.value);

            // assert original name
            Bir.ConstantPoolEntry constantPoolEntryOrigName =
                    constantPoolEntries.get(actualFunction.originalNameCpIndex());
            assertConstantPoolEntry(constantPoolEntryOrigName, expectedFunction.originalName.value);

            // assert markdown document
            assertMarkdownDocument(actualFunction.doc(), expectedFunction.markdownDocAttachment, constantPoolEntries);

            // assert annotation attachments
            assertAnnotationAttachments(actualFunction.annotationAttachmentsContent(),
                    expectedFunction.annotAttachments, constantPoolEntries);

            // assert return type annotation attachments
            assertAnnotationAttachments(actualFunction.returnTypeAnnotations(), expectedFunction.returnTypeAnnots,
                                        constantPoolEntries);

            // assert required param count
            Assert.assertEquals(actualFunction.requiredParamCount(), expectedFunction.requiredParams.size());

            Bir.FunctionBody actualFunctionBody = actualFunction.functionBody();
            // assert arguments count
            Assert.assertEquals(actualFunctionBody.argsCount(), expectedFunction.argsCount);
            Assert.assertEquals(actualFunctionBody.defaultParameterCount(), expectedFunction.parameters.size());

            // assert dependent global variables
            assertDepGlobalVar(actualFunction, expectedFunction.dependentGlobalVars.toArray(), constantPoolEntries);

            // assert basic blocks
            assertBasicBlocks(actualFunctionBody.functionBasicBlocksInfo(), expectedFunction.basicBlocks,
                    constantPoolEntries);

            assertScopes(actualFunction.scopeEntries(), expectedFunction);
        }
    }

    private static void assertScopes(ArrayList<Bir.ScopeEntry> scopeEntries, BIRNode.BIRFunction function) {
        int instructionOffset = 0;
        Map<Integer, ExpectedScopeEntry> scopes = new HashMap<>();
        Set<BirScope> visitedScopes = new HashSet<>();

        generateExpectedScopeEntries(function.basicBlocks, instructionOffset, scopes, visitedScopes);

        for (Bir.ScopeEntry actualScopeEntry : scopeEntries) {
            ExpectedScopeEntry expectedScopeEntry = scopes.get(actualScopeEntry.currentScopeIndex());
            Assert.assertNotNull(expectedScopeEntry);

            Assert.assertEquals(actualScopeEntry.instructionOffset(), expectedScopeEntry.instructionOffset);
            Assert.assertEquals(actualScopeEntry.hasParent(), expectedScopeEntry.hasParent);
            Assert.assertEquals(actualScopeEntry.parentScopeIndex(), expectedScopeEntry.parentId);
        }
    }

    private static int generateExpectedScopeEntries(List<BIRNode.BIRBasicBlock> bbList, int instructionOffset,
            Map<Integer, ExpectedScopeEntry> scopes, Set<BirScope> visitedScopes) {
        for (BIRNode.BIRBasicBlock bb : bbList) {
            boolean hasParent;
            ExpectedScopeEntry expectedScopeEntry;
            for (BIRAbstractInstruction instruction : bb.instructions) {
                instructionOffset++;
                BirScope instructionScope = instruction.scope;

                if (visitedScopes.contains(instructionScope)) {
                    continue;
                }

                visitedScopes.add(instructionScope);
                hasParent = instructionScope.parent() != null;

                expectedScopeEntry = new ExpectedScopeEntry(instructionScope.id(),
                        instructionOffset,  hasParent ? 1 : 0, hasParent ? instructionScope.parent().id() : null);
                scopes.put(instructionScope.id(), expectedScopeEntry);
                putParentScopesAsWell(scopes, instructionScope.parent(), instructionOffset);
            }
            
            BIRTerminator terminator = bb.terminator;
            BirScope terminatorScope = terminator.scope;
            if (terminatorScope != null) {
                if (visitedScopes.contains(terminatorScope)) {
                    continue;
                }
                visitedScopes.add(terminatorScope);
                hasParent = terminatorScope.parent() != null;
                expectedScopeEntry = new ExpectedScopeEntry(terminatorScope.id(), instructionOffset, hasParent ? 1 : 0,
                        hasParent ? terminatorScope.parent().id() : null);
                scopes.put(terminatorScope.id(), expectedScopeEntry);
                putParentScopesAsWell(scopes, terminatorScope.parent(), instructionOffset);
            }
        }
        return instructionOffset;
    }

    private static void putParentScopesAsWell(Map<Integer, ExpectedScopeEntry> scopes, BirScope parent,
            int instructionOffset) {

        if (parent == null) {
            return;
        }

        if (scopes.containsKey(parent.id())) {
            return;
        }

        boolean hasParent = parent.parent() != null;
        ExpectedScopeEntry expectedParentScopeEntry = new ExpectedScopeEntry(parent.id(),
                instructionOffset,  hasParent ? 1 : 0, hasParent ? parent.parent().id() : null);
        scopes.put(parent.id(), expectedParentScopeEntry);

        if (hasParent) {
            putParentScopesAsWell(scopes, parent.parent(), instructionOffset);
        }
    }

    private static void assertDepGlobalVar(Bir.Function actualFunction, Object[] expectedGlobalVars,
            ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {

        ArrayList<Integer> actualGlobalVarCpEntries = actualFunction.dependentGlobalVarCpEntry();
        Assert.assertEquals(actualGlobalVarCpEntries.size(), expectedGlobalVars.length);

        for (int i = 0; i < actualGlobalVarCpEntries.size(); i++) {
            Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(actualGlobalVarCpEntries.get(i));
            String expectedName = ((BIRNode.BIRGlobalVariableDcl) expectedGlobalVars[i]).name.value;
            assertConstantPoolEntry(constantPoolEntry, expectedName);
        }
    }

    private static void assertMarkdownDocument(Bir.Markdown actualDocument, MarkdownDocAttachment expectedDocument,
                                               ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        if (actualDocument.hasDoc() == 0) {
            Assert.assertNull(expectedDocument);
            return;
        }

        Bir.MarkdownContent actualDocContent = actualDocument.markdownContent();
        assertMarkdownEntry(constantPoolEntries, actualDocContent.descriptionCpIndex(), expectedDocument.description);

        assertMarkdownEntry(constantPoolEntries, actualDocContent.returnValueDescriptionCpIndex(),
                            expectedDocument.returnValueDescription);

        List<MarkdownDocAttachment.Parameter> expectedParameters = expectedDocument.parameters;
        Assert.assertEquals(actualDocContent.parametersCount(), expectedParameters.size());
        ArrayList<Bir.MarkdownParameter> actualParameters = actualDocContent.parameters();

        assertMarkdownParams(constantPoolEntries, actualParameters, expectedParameters);

        assertMarkdownEntry(constantPoolEntries, actualDocContent.deprecatedDocsCpIndex(),
                            expectedDocument.deprecatedDocumentation);

        List<MarkdownDocAttachment.Parameter> expDeprecatedParams = expectedDocument.deprecatedParams;
        ArrayList<Bir.MarkdownParameter> actualDeprecatedParams = actualDocContent.deprecatedParams();
        Assert.assertEquals(actualDocContent.deprecatedParamsCount(), expDeprecatedParams.size());
        Assert.assertEquals(actualDeprecatedParams.size(), actualDocContent.deprecatedParamsCount());

        assertMarkdownParams(constantPoolEntries, actualDeprecatedParams, expDeprecatedParams);
    }

    private static void assertMarkdownParams(ArrayList<Bir.ConstantPoolEntry> constantPoolEntries,
                                             ArrayList<Bir.MarkdownParameter> actualParams,
                                             List<MarkdownDocAttachment.Parameter> expParams) {
        for (int i = 0; i < actualParams.size(); i++) {
            Bir.MarkdownParameter actualParameter = actualParams.get(i);
            MarkdownDocAttachment.Parameter expectedParameter = expParams.get(i);

            assertMarkdownEntry(constantPoolEntries, actualParameter.nameCpIndex(), expectedParameter.name);

            assertMarkdownEntry(constantPoolEntries, actualParameter.descriptionCpIndex(),
                                expectedParameter.description);
        }
    }

    private static void assertMarkdownEntry(ArrayList<Bir.ConstantPoolEntry> constantPoolEntries, int cpIndex,
                                            String description) {
        if (cpIndex < 0) {
            return;
        }

        Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(cpIndex);
        assertConstantPoolEntry(constantPoolEntry, description);
    }

    private static void assertBasicBlocks(Bir.BasicBlocksInfo actualBasicBlocksInfo,
                                          List<BIRNode.BIRBasicBlock> expectedBasicBlocks,
                                          ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        Assert.assertEquals(actualBasicBlocksInfo.basicBlocksCount(), expectedBasicBlocks.size());

        ArrayList<Bir.BasicBlock> actualBasicBlocks = actualBasicBlocksInfo.basicBlocks();
        for (int i = 0; i < actualBasicBlocks.size(); i++) {
            Bir.BasicBlock actualBasicBlock = actualBasicBlocks.get(i);
            BIRNode.BIRBasicBlock expectedBasicBlock = expectedBasicBlocks.get(i);

            Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(actualBasicBlock.nameCpIndex());
            assertConstantPoolEntry(constantPoolEntry, expectedBasicBlock.id.value);

            ArrayList<Bir.Instruction> actualInstructions = actualBasicBlock.instructions();
            List<BIRNonTerminator> expectedInstructions = expectedBasicBlock.instructions;

            Assert.assertEquals(actualBasicBlock.instructionsCount(), expectedInstructions.size() + 1);

            for (int j = 0; j < actualInstructions.size() - 1; j++) {
                assertInstruction(actualInstructions.get(j), expectedInstructions.get(j), constantPoolEntries);
            }

            Bir.Instruction actualTerminator = actualInstructions.get(actualBasicBlock.instructionsCount() - 1);
            assertInstruction(actualTerminator, expectedBasicBlock.terminator, constantPoolEntries);
        }
    }

    private static void assertInstruction(Bir.Instruction actualInstruction, BIRInstruction expectedInstruction,
                                          ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        Bir.Instruction.InstructionKindEnum instructionKindEnum = actualInstruction.instructionKind();
        KaitaiStruct instructionStructure = actualInstruction.instructionStructure();
        switch (instructionKindEnum) {
            case INSTRUCTION_KIND_MOVE:
                assertMoveInstruction((Bir.InstructionMove) instructionStructure,
                        (BIRNonTerminator.Move) expectedInstruction,
                        constantPoolEntries);
                break;
            case INSTRUCTION_KIND_GOTO:
                assertGotoInstruction((Bir.InstructionGoto) instructionStructure,
                        (BIRTerminator.GOTO) expectedInstruction, constantPoolEntries);
                break;
            case INSTRUCTION_KIND_CALL:
                assertCallInstruction((Bir.InstructionCall) instructionStructure,
                        (BIRTerminator.Call) expectedInstruction, constantPoolEntries);
                break;
            case INSTRUCTION_KIND_BRANCH:
                assertBranchInstruction((Bir.InstructionBranch) instructionStructure,
                        (BIRTerminator.Branch) expectedInstruction, constantPoolEntries);
                break;
            default:
                Assert.assertEquals(instructionKindEnum.id(), expectedInstruction.getKind().getValue());
        }
    }

    private static void assertBranchInstruction(Bir.InstructionBranch actualBranch,
                                                BIRTerminator.Branch expectedBranch,
                                                ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        assertOperand(actualBranch.branchOperand(), expectedBranch.op, constantPoolEntries);

        // assert true bb name
        Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(actualBranch.trueBbIdNameCpIndex());
        assertConstantPoolEntry(constantPoolEntry, expectedBranch.trueBB.id.value);

        // assert false bb name
        constantPoolEntry = constantPoolEntries.get(actualBranch.falseBbIdNameCpIndex());
        assertConstantPoolEntry(constantPoolEntry, expectedBranch.falseBB.id.value);
    }

    private static void assertCallInstruction(Bir.InstructionCall actualCall, BIRTerminator.Call expectedCall,
                                              ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        Bir.CallInstructionInfo callInstructionInfo = actualCall.callInstructionInfo();

        Assert.assertEquals(callInstructionInfo.argumentsCount(), expectedCall.args.size());
        Assert.assertEquals(callInstructionInfo.hasLhsOperand() == 1, expectedCall.getLhsOperand() != null);
        Assert.assertEquals(callInstructionInfo.isVirtual() == 1, expectedCall.isVirtual);

        // assert pkg name
        Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(callInstructionInfo.packageIndex());
        assertConstantPoolEntry(constantPoolEntry, expectedCall.calleePkg);

        // assert call name
        constantPoolEntry = constantPoolEntries.get(callInstructionInfo.callNameCpIndex());
        assertConstantPoolEntry(constantPoolEntry, expectedCall.name.value);

        // assert then bb id
        constantPoolEntry = constantPoolEntries.get(actualCall.thenBbIdNameCpIndex());
        assertConstantPoolEntry(constantPoolEntry, expectedCall.thenBB.id.value);
    }

    private static void assertGotoInstruction(Bir.InstructionGoto actualGoto, BIRTerminator.GOTO expectedInstruction,
                                              ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        // assert goto bb id
        Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(actualGoto.targetBbIdNameCpIndex());
        assertConstantPoolEntry(constantPoolEntry, expectedInstruction.targetBB.id.value);
    }

    private static void assertMoveInstruction(Bir.InstructionMove actual, BIRNonTerminator.Move expected,
                                              ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        Bir.Operand actualLhsOperand = actual.lhsOperand();
        BIROperand expectedLhsOp = expected.lhsOp;
        assertOperand(actualLhsOperand, expectedLhsOp, constantPoolEntries);

        Bir.Operand actualRhsOperand = actual.rhsOperand();
        BIROperand expectedRhsOp = expected.rhsOp;
        assertOperand(actualRhsOperand, expectedRhsOp, constantPoolEntries);
    }

    private static void assertOperand(Bir.Operand actual, BIROperand expected,
                                      ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        BIRNode.BIRVariableDcl expectedVar = expected.variableDcl;
        Assert.assertEquals(actual.ignoredVariable() == 1, expectedVar.ignoreVariable);

        if (actual.ignoredVariable() == 0) {
            Bir.Variable actualVar = actual.variable();
            Assert.assertEquals(actualVar.kind(), expectedVar.kind.getValue());
            Assert.assertEquals(actualVar.scope(), expectedVar.scope.getValue());

            // assert var name
            Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(actualVar.variableDclNameCpIndex());
            assertConstantPoolEntry(constantPoolEntry, expectedVar.name.value);
        }
    }

    private static void assertValues(BIRNode.BIRPackage expectedBIR, Bir.Module birModule,
                                     ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        List<BIRNode.BIRConstant> expectedConstants = expectedBIR.constants;
        ArrayList<Bir.Constant> actualConstants = birModule.constants();
        Assert.assertEquals(birModule.constCount(), expectedConstants.size());

        for (int i = 0; i < expectedConstants.size(); i++) {
            Bir.Constant actualConstant = actualConstants.get(i);
            BIRNode.BIRConstant expectedConstant = expectedConstants.get(i);

            // assert name
            assertConstantPoolEntry(constantPoolEntries.get(actualConstant.nameCpIndex()), expectedConstant.name.value);

            // assert flags
            assertFlags(actualConstant.flags(), expectedConstant.flags);

            // assert type
            assertConstantPoolEntry(constantPoolEntries.get(actualConstant.typeCpIndex()), expectedConstant.type);

            // assert value
            assertConstantValue(actualConstant.constantValue(), expectedConstant.constValue.value, constantPoolEntries);
        }
    }

    private static void assertTypeDefs(BIRNode.BIRPackage expectedBIR, Bir.Module birModule,
                                       ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        List<BIRNode.BIRTypeDefinition> expectedTypeDefs = expectedBIR.typeDefs;
        ArrayList<Bir.TypeDefinition> actualTypeDefinitions = birModule.typeDefinitions();

        Assert.assertEquals(actualTypeDefinitions.size(), expectedTypeDefs.size());

        for (int i = 0; i < expectedTypeDefs.size(); i++) {
            BIRNode.BIRTypeDefinition expectedTypeDefinition = expectedTypeDefs.get(i);
            Bir.TypeDefinition actualTypeDefinition = actualTypeDefinitions.get(i);

            // assert name
            assertConstantPoolEntry(constantPoolEntries.get(actualTypeDefinition.nameCpIndex()),
                    expectedTypeDefinition.internalName.value);

            // assert original name
            assertConstantPoolEntry(constantPoolEntries.get(actualTypeDefinition.originalNameCpIndex()),
                    expectedTypeDefinition.originalName.value);

            // assert flags
            assertFlags(actualTypeDefinition.flags(), expectedTypeDefinition.flags);

            // assert type
            assertConstantPoolEntry(constantPoolEntries.get(actualTypeDefinition.typeCpIndex()),
                    expectedTypeDefinition.type);

            // assert position
            if (expectedTypeDefinition.pos != null) {
                assertPosition(actualTypeDefinition.position(), expectedTypeDefinition.pos);
            }

            // assert markdown document
            assertMarkdownDocument(actualTypeDefinition.doc(), expectedTypeDefinition.markdownDocAttachment,
                                   constantPoolEntries);
        }
    }

    private static void assertServiceDecls(BIRNode.BIRPackage expectedBIR, Bir.Module birModule,
                                           ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        List<BIRNode.BIRServiceDeclaration> expServiceDecls = expectedBIR.serviceDecls;
        ArrayList<Bir.ServiceDeclaration> actualServiceDecls = birModule.serviceDeclarations();

        Assert.assertEquals(actualServiceDecls.size(), expServiceDecls.size());

        for (int i = 0; i < expServiceDecls.size(); i++) {
            BIRNode.BIRServiceDeclaration expServiceDecl = expServiceDecls.get(i);
            Bir.ServiceDeclaration actualServiceDecl = actualServiceDecls.get(i);

            // assert name
            assertConstantPoolEntry(constantPoolEntries.get(actualServiceDecl.nameCpIndex()),
                                    expServiceDecl.generatedName.value);

            // assert associated class name
            assertConstantPoolEntry(constantPoolEntries.get(actualServiceDecl.associatedClassNameCpIndex()),
                                    expServiceDecl.associatedClassName.value);

            // assert flags
            assertFlags(actualServiceDecl.flags(), expServiceDecl.flags);

            // assert type
            if (expServiceDecl.type != null) {
                assertConstantPoolEntry(constantPoolEntries.get(actualServiceDecl.typeCpIndex()),
                                        expServiceDecl.type);
            }

            // assert position
            if (expServiceDecl.pos != null) {
                assertPosition(actualServiceDecl.position(), expServiceDecl.pos);
            }

            // assert attach points
            if (expServiceDecl.attachPoint != null) {
                Assert.assertEquals(actualServiceDecl.attachPoints().size(), expServiceDecl.attachPoint.size());

                List<String> attachPoint = expServiceDecl.attachPoint;
                for (int j = 0, attachPointSize = attachPoint.size(); j < attachPointSize; j++) {
                    assertConstantPoolEntry(constantPoolEntries.get(actualServiceDecl.attachPoints().get(j)),
                                            attachPoint.get(j));
                }
            }

            // assert attach point literal
            if (expServiceDecl.attachPointLiteral != null) {
                assertConstantPoolEntry(constantPoolEntries.get(actualServiceDecl.attachPointLiteral()),
                                        expServiceDecl.attachPointLiteral);
            }

            // assert listener types
            Assert.assertEquals(actualServiceDecl.listenerTypes().size(), expServiceDecl.listenerTypes.size());

            Iterator<BType> iterator = expServiceDecl.listenerTypes.iterator();
            for (int j = 0; j < expServiceDecl.listenerTypes.size(); j++) {
                assertConstantPoolEntry(constantPoolEntries.get(actualServiceDecl.listenerTypes().get(j).typeCpIndex()),
                                        iterator.next());
            }
        }
    }

    private static void assertPosition(Bir.Position actualPosition, Location expectedPosition) {
        Assert.assertEquals(actualPosition.sLine(), expectedPosition.lineRange().startLine().line());
        Assert.assertEquals(actualPosition.eLine(), expectedPosition.lineRange().endLine().line());
        Assert.assertEquals(actualPosition.sCol(), expectedPosition.lineRange().startLine().offset());
        Assert.assertEquals(actualPosition.eCol(), expectedPosition.lineRange().endLine().offset());
    }

    private static void assertAnnotations(BIRNode.BIRPackage expectedBIR, Bir.Module birModule,
                                          ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        List<BIRNode.BIRAnnotation> expectedAnnotations = expectedBIR.annotations;
        ArrayList<Bir.Annotation> actualAnnotations = birModule.annotations();

        Assert.assertEquals(actualAnnotations.size(), expectedAnnotations.size());

        for (int i = 0; i < expectedAnnotations.size(); i++) {
            BIRNode.BIRAnnotation expectedAnnotation = expectedAnnotations.get(i);
            Bir.Annotation actualAnnotation = actualAnnotations.get(i);

            // assert name
            assertConstantPoolEntry(constantPoolEntries.get(actualAnnotation.nameCpIndex()),
                    expectedAnnotation.name.value);

            // assert original name
            assertConstantPoolEntry(constantPoolEntries.get(actualAnnotation.originalNameCpIndex()),
                    expectedAnnotation.originalName.value);

            // assert type
            assertConstantPoolEntry(constantPoolEntries.get(actualAnnotation.annotationTypeCpIndex()),
                    expectedAnnotation.annotationType);

            // assert attach points
            assertAttachPoints(actualAnnotation.attachPoints(), expectedAnnotation.attachPoints, constantPoolEntries);
        }
    }

    private static void assertAttachPoints(ArrayList<Bir.AttachPoint> actualAttachPoints,
                                           Set<AttachPoint> expectedAttachPoints,
                                           ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        Assert.assertEquals(actualAttachPoints.size(), expectedAttachPoints.size());
        AttachPoint[] expected = expectedAttachPoints.toArray(new AttachPoint[0]);
        for (int i = 0; i < expected.length; i++) {
            AttachPoint expectedAttachPoint = expected[i];
            Bir.AttachPoint actualAttachPoint = actualAttachPoints.get(i);

            assertConstantPoolEntry(constantPoolEntries.get(actualAttachPoint.pointNameCpIndex()),
                    expectedAttachPoint.point.getValue());
        }
    }

    private static void assertConstantPoolEntry(Bir.ConstantPoolEntry constantPoolEntry, Object expectedValue) {
        switch (constantPoolEntry.tag()) {
            case CP_ENTRY_INTEGER:
                Bir.IntCpInfo intCpInfo = (Bir.IntCpInfo) constantPoolEntry.cpInfo();
                Assert.assertEquals(intCpInfo.value(), (long) expectedValue);
                break;
            case CP_ENTRY_STRING:
                Bir.StringCpInfo stringCpInfo = (Bir.StringCpInfo) constantPoolEntry.cpInfo();
                Assert.assertEquals(stringCpInfo.value(), (String) expectedValue);
                break;
            case CP_ENTRY_SHAPE:
                assertType(constantPoolEntry, (BType) expectedValue);
                break;
            case CP_ENTRY_FLOAT:
                Bir.FloatCpInfo floatCpInfo = (Bir.FloatCpInfo) constantPoolEntry.cpInfo();
                double expectedFloatVal = expectedValue instanceof String ? Double.parseDouble((String) expectedValue)
                        : (Double) expectedValue;
                Assert.assertEquals(floatCpInfo.value(), expectedFloatVal);
                break;
            case CP_ENTRY_PACKAGE:
                Bir.PackageCpInfo packageCpInfo = (Bir.PackageCpInfo) constantPoolEntry.cpInfo();
                PackageID packageID = (PackageID) expectedValue;
                ArrayList<Bir.ConstantPoolEntry> constantPoolEntries = constantPoolEntry._parent().
                        constantPoolEntries();
                assertConstantPoolEntry(constantPoolEntries.get(packageCpInfo.orgIndex()), packageID.orgName.value);
                assertConstantPoolEntry(constantPoolEntries.get(packageCpInfo.nameIndex()), packageID.name.value);
                assertConstantPoolEntry(constantPoolEntries.get(packageCpInfo.versionIndex()), packageID.version.value);
                break;
            case CP_ENTRY_BYTE:
                Bir.ByteCpInfo byteCpInfo = (Bir.ByteCpInfo) constantPoolEntry.cpInfo();
                Assert.assertEquals(byteCpInfo.value(), (int) expectedValue);
                break;
            case CP_ENTRY_BOOLEAN:
                Bir.BooleanCpInfo booleanCpInfo = (Bir.BooleanCpInfo) constantPoolEntry.cpInfo();
                Assert.assertEquals(booleanCpInfo.value(), expectedValue);
                break;
            default:
                Assert.fail(String.format("Unknown constant pool entry: %s", constantPoolEntry.tag().name()));
        }
    }

    private static void assertType(Bir.ConstantPoolEntry constantPoolEntry, BType expectedValue) {
        Bir.TypeInfo typeInfo = ((Bir.ShapeCpInfo) constantPoolEntry.cpInfo()).shape();
        Assert.assertEquals(typeInfo.typeTag().id(), expectedValue.tag);
        Assert.assertEquals(typeInfo.nameAsStr(), expectedValue.name.getValue());
        assertFlags(typeInfo.typeFlag(), expectedValue.flags);
        KaitaiStruct typeStructure = typeInfo.typeStructure();

        if (typeStructure instanceof Bir.TypeObjectOrService objectOrService) {
            BTypeIdSet expTypeIdSet = ((BObjectType) expectedValue.tsymbol.type).typeIdSet;
            Bir.TypeId actualTypeIdSet = objectOrService.typeIds();
            assertDistinctTypeIds(expTypeIdSet, actualTypeIdSet, constantPoolEntry._parent());
        } else if (typeStructure instanceof Bir.TypeError errorType) {
            BTypeIdSet expTypeIdSet = ((BErrorType) expectedValue.tsymbol.type).typeIdSet;
            Bir.TypeId actualTypeIdSet = errorType.typeIds();
            assertDistinctTypeIds(expTypeIdSet, actualTypeIdSet, constantPoolEntry._parent());
        }
    }

    private static void assertDistinctTypeIds(BTypeIdSet expTypeIdSet, Bir.TypeId actualTypeIdSet,
                                              Bir.ConstantPoolSet constantPoolSet) {
        Assert.assertEquals(actualTypeIdSet.primaryTypeIdCount(), expTypeIdSet.getPrimary().size());
        Assert.assertEquals(actualTypeIdSet.secondaryTypeIdCount(), expTypeIdSet.getSecondary().size());

        ArrayList<Bir.TypeIdSet> primaryTypeId = actualTypeIdSet.primaryTypeId();
        for (Bir.TypeIdSet typeId : primaryTypeId) {
            Assert.assertTrue(containsTypeId(typeId, expTypeIdSet.getPrimary(), constantPoolSet));
        }

        ArrayList<Bir.TypeIdSet> secondaryTypeId = actualTypeIdSet.secondaryTypeId();
        for (Bir.TypeIdSet typeId : secondaryTypeId) {
            Assert.assertTrue(containsTypeId(typeId, expTypeIdSet.getSecondary(), constantPoolSet));
        }
    }

    private static boolean containsTypeId(Bir.TypeIdSet aTypeIdSet, Set<BTypeIdSet.BTypeId> set,
                                          Bir.ConstantPoolSet constantPoolSet) {
        for (BTypeIdSet.BTypeId expId : set) {
            Bir.ConstantPoolEntry typeNameEntry =
                    constantPoolSet.constantPoolEntries().get(aTypeIdSet.typeIdNameCpIndex());
            Bir.ConstantPoolEntry pkgIdEntry = constantPoolSet.constantPoolEntries().get(aTypeIdSet.pkgIdCpIndex());
            boolean samePublicness = expId.publicId == (aTypeIdSet.isPublicId() != 0);

            Bir.StringCpInfo stringCpInfo = (Bir.StringCpInfo) typeNameEntry.cpInfo();
            boolean sameName = stringCpInfo.value().equals(expId.name);

            if (samePublicness && sameName) {
                assertConstantPoolEntry(pkgIdEntry, expId.packageID);
                return true;
            }
        }
        return false;
    }

    private static void assertFlags(long actualFlags, long expectedFlags) {

        Assert.assertEquals(actualFlags, expectedFlags, "Invalid flags");
    }

    private static void assertConstantValue(Bir.ConstantValue actualConstantValue, Object expectedValue,
                                            ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        assertConstantValue(actualConstantValue, expectedValue, constantPoolEntries,
                            actualConstantValue.type().shape());
    }

    private static void assertConstantValue(Bir.ConstantValue actualConstantValue, Object expectedValue,
                                            ArrayList<Bir.ConstantPoolEntry> constantPoolEntries,
                                            Bir.TypeInfo typeInfo) {
        Bir.TypeTagEnum typeTag = typeInfo.typeTag();
        KaitaiStruct constantValueInfo = actualConstantValue.constantValueInfo();
        switch (typeTag) {
            case TYPE_TAG_INT:
                Bir.IntConstantInfo intConstantInfo = (Bir.IntConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(intConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_BYTE:
                Bir.ByteConstantInfo byteConstantInfo = (Bir.ByteConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(byteConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_STRING:
                Bir.StringConstantInfo stringConstantInfo = (Bir.StringConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(stringConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_FLOAT:
                Bir.FloatConstantInfo floatConstantInfo = (Bir.FloatConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(floatConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_DECIMAL:
                Bir.DecimalConstantInfo decimalConstantInfo = (Bir.DecimalConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(decimalConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_BOOLEAN:
                Bir.BooleanConstantInfo booleanConstantInfo = (Bir.BooleanConstantInfo) constantValueInfo;
                Assert.assertEquals(booleanConstantInfo.valueBooleanConstant() == 1, expectedValue);
                break;
            case TYPE_TAG_RECORD:
                Bir.MapConstantInfo actualMapConst;
                if (constantValueInfo instanceof Bir.MapConstantInfo) {
                    actualMapConst = (Bir.MapConstantInfo) constantValueInfo;
                } else {
                    actualMapConst =
                        (Bir.MapConstantInfo) ((Bir.IntersectionConstantInfo) constantValueInfo).constantValueInfo();
                }
                Map<String, BIRNode.ConstValue> expectedMapConst = (Map<String, BIRNode.ConstValue>) expectedValue;
                Assert.assertEquals(actualMapConst.mapConstantSize(), expectedMapConst.size());
                break;
            case TYPE_TAG_TUPLE:
                Bir.ListConstantInfo actualListConst =
                        (Bir.ListConstantInfo) ((Bir.IntersectionConstantInfo) constantValueInfo).constantValueInfo();
                ArrayList<Bir.ConstantValue> actualConstValues = actualListConst.listMemberValueInfo();
                BIRNode.ConstValue[] expectedListConst = (BIRNode.ConstValue[]) expectedValue;
                Assert.assertEquals(actualListConst.listConstantSize(), expectedListConst.length);
                for (int i = 0; i < expectedListConst.length; i++) {
                    assertConstantValue(actualConstValues.get(i), expectedListConst[i], constantPoolEntries);
                }
                break;
            case TYPE_TAG_INTERSECTION:
                Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(
                        ((Bir.TypeIntersection) typeInfo.typeStructure()).effectiveTypeCpIndex());
                Bir.TypeInfo effectiveTypeInfo = ((Bir.ShapeCpInfo) constantPoolEntry.cpInfo()).shape();
                assertConstantValue(actualConstantValue, expectedValue, constantPoolEntries, effectiveTypeInfo);
                break;
            default:
                Assert.fail(String.format("Unknown constant value type: %s", typeTag.name()));
        }
    }

    private static void assertAnnotationAttachments(Bir.AnnotationAttachmentsContent actualAnnots,
                                                    List<BIRNode.BIRAnnotationAttachment> expAnnots,
                                                    ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {
        Assert.assertEquals(actualAnnots.attachmentsCount(), expAnnots.size());

        for (int i = 0; i < expAnnots.size(); i++) {
            Bir.AnnotationAttachment actualAnnot = actualAnnots.annotationAttachments().get(i);
            BIRNode.BIRAnnotationAttachment expAnnot = expAnnots.get(i);

            Bir.ConstantPoolEntry pkgId = constantPoolEntries.get(actualAnnot.packageIdCpIndex());
            assertConstantPoolEntry(pkgId, expAnnot.annotPkgId);

            assertPosition(actualAnnot.position(), expAnnot.pos);

            Bir.ConstantPoolEntry annotTag = constantPoolEntries.get(actualAnnot.tagReferenceCpIndex());
            assertConstantPoolEntry(annotTag, expAnnot.annotTagRef.value);

            boolean constAnnotAttachment = expAnnot instanceof BIRNode.BIRConstAnnotationAttachment;
            Assert.assertEquals(actualAnnot.isConstAnnot() == 1, constAnnotAttachment);

            if (!constAnnotAttachment) {
                continue;
            }

            BIRNode.ConstValue expAnnotValue = ((BIRNode.BIRConstAnnotationAttachment) expAnnot).annotValue;
            Bir.ConstantValue actualAnnotValue = actualAnnot.constantValue();

            assertConstantPoolEntry(constantPoolEntries.get(actualAnnotValue.constantValueTypeCpIndex()),
                                    expAnnotValue.type);
            assertConstantValue(actualAnnotValue, expAnnotValue.value, constantPoolEntries);
        }
    }

    static class ExpectedScopeEntry {
        public final int id;
        public final int instructionOffset;
        public final int hasParent;
        public final Integer parentId;

        ExpectedScopeEntry(int id, int instructionOffset, int hasParent, Integer parentId) {
            this.id = id;
            this.instructionOffset = instructionOffset;
            this.hasParent = hasParent;
            this.parentId = parentId;
        }
    }
}
