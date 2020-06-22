package org.ballerinalang.langserver.compiler.formatter;

import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocuments;
import org.ballerinalang.langserver.compiler.format.FormattingTreeModifier;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FormatterTest {
    private final Path formattingDirectory = Paths.get("src/test/resources/").toAbsolutePath().resolve("formatter");

    @Test(dataProvider = "fileProvider")
    public void formatTest(String expectedFile, String testFile) throws IOException {
        Path expectedFilePath = formattingDirectory.resolve("expected").resolve(expectedFile);
        Path inputFilePath = formattingDirectory.resolve("source").resolve(testFile);

        String content = new String(Files.readAllBytes(inputFilePath));

        TextDocument textDocument = TextDocuments.from(content);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        FormattingTreeModifier formattingTransformer = new FormattingTreeModifier();

        ModulePartNode newModulePart = formattingTransformer.transform((ModulePartNode) syntaxTree.rootNode());
        SyntaxTree newSyntaxTree = syntaxTree.modifyWith(newModulePart);
    }

    @DataProvider(name = "fileProvider")
    public Object[][] fileProvider() {
        return new Object[][]{
                {"expectedFunction.bal", "function.bal"},
                {"expectedEndpoint.bal", "endpoint.bal"},
                {"expectedWorker.bal", "worker.bal"},
                {"expectedExpressionStatement.bal", "expressionStatement.bal"},
                {"expectedService.bal", "service.bal"},
                {"expectedReturn.bal", "return.bal"},
                {"expectedCompilationUnit.bal", "compilationUnit.bal"},
                {"expectedImport.bal", "import.bal"},
                {"expectedTupleType.bal", "tupleType.bal"},
                {"expectedVariableDefinition.bal", "variableDefinition.bal"},
                {"expectedUnionType.bal", "unionType.bal"},
                {"expectedRecord.bal", "record.bal"},
                {"expectedObject.bal", "object.bal"},
                {"expectedFieldBasedAccess.bal", "fieldBasedAccess.bal"},
                {"expectedFunctionType.bal", "functionType.bal"},
                {"expectedWhile.bal", "while.bal"},
                {"expectedIf.bal", "if.bal"},
                {"expectedBinaryExpr.bal", "binaryExpr.bal"},
                {"expectedArrayLiteralExpr.bal", "arrayLiteralExpr.bal"},
                {"expectedForeach.bal", "foreach.bal"},
                {"expectedConstrainedType.bal", "constrainedType.bal"},
                {"expectedBreak.bal", "break.bal"},
                {"expectedMatchStmt.bal", "matchStmt.bal"},
                //TODO Transaction
//                {"expectedAbort.bal", "abort.bal"},
//                {"expectedTransaction.bal", "transaction.bal"},
                {"expectedContinue.bal", "continue.bal"},
                {"expectedTypeDefinition.bal", "typeDefinition.bal"},
                //TODO Table remove - Fix
//                {"expectedTable.bal", "table.bal"},
                {"expectedCompilationUnitMultiEOF.bal", "compilationUnitMultiEOF.bal"},
                {"expectedAnnotation.bal", "annotation.bal"},
                {"expectedArrowExpr.bal", "arrowExpr.bal"},
                {"expectedAsyncExpr.bal", "asyncExpr.bal"},
                {"expectedBindingPatterns.bal", "bindingPatterns.bal"},
                {"expectedDocumentation.bal", "documentation.bal"},
                {"expectedWorkerInteractions.bal", "workerInteractions.bal"},
                {"expectedWait.bal", "wait.bal"},
                {"expectedCheck.bal", "check.bal"},
                {"expectedCompoundAssignment.bal", "compoundAssignment.bal"},
                {"expectedConstant.bal", "constant.bal"},
                {"expectedElvisExpr.bal", "elvisExpr.bal"},
                {"expectedErrorConstructor.bal", "errorConstructor.bal"},
                {"expectedRecordLiteralExpr.bal", "recordLiteralExpr.bal"},
                {"expectedRecordVariable.bal", "recordVariable.bal"},
                {"expectedTypeGuard.bal", "typeGuard.bal"},
                {"expectedTernaryExpr.bal", "ternaryExpr.bal"},
                {"expectedStringTemplateLiteral.bal", "stringTemplateLiteral.bal"},
                {"expectedTrap.bal", "trap.bal"},
                {"expectedPanic.bal", "panic.bal"},
                {"expectedErrorVariableDefinition.bal", "errorVariableDefinition.bal"},
                {"expectedErrorVariableReference.bal", "errorVariableReference.bal"},
                {"expectedErrorType.bal", "errorType.bal"},
                {"expectedIndexBasedAccess.bal", "indexBasedAccess.bal"},
                {"expectedIntegerRangeExpression.bal", "integerRangeExpression.bal"},
                {"expectedLock.bal", "lock.bal"},
                {"expectedImportOrder.bal", "importOrder.bal"},
                {"expectedBlockExpandOnDemand.bal", "blockExpandOnDemand.bal"},
                {"expectedNamedArgsExpr.bal", "namedArgsExpr.bal"},
                {"expectedRestArgsExpr.bal", "restArgsExpr.bal"},
                {"expectedRecordDestructure.bal", "recordDestructure.bal"},
                {"expectedCheckPanic.bal", "checkPanic.bal"},
                {"expectedAnonRecord.bal", "anonRecord.bal"},
                {"expectedInvocation.bal", "invocation.bal"},
                {"expectedTypeDesc.bal", "typeDesc.bal"},
                {"expectedAnonObject.bal", "anonObject.bal"},
                {"expectedUnaryExpr.bal", "unaryExpr.bal"},
                {"expectedTypeInitExpr.bal", "typeInitExpr.bal"},
                {"expectedServiceConstruct.bal", "serviceConstruct.bal"},
                {"expectedForkJoin.bal", "forkJoin.bal"},
                {"expectedEmpty.bal", "empty.bal"},
                {"expectedDelimiter.bal", "delimiter.bal"},
                {"expectedXmlns.bal", "xmlns.bal"},
                {"expectedXMLTextLiteral.bal", "xmlTextLiteral.bal"},
                {"expectedXMLCommentLiteral.bal", "xmlCommentLiteral.bal"},
                {"expectedXMLPILiteral.bal", "xmlPILiteral.bal"},
                {"expectedXMLElementLiteral.bal", "xmlElementLiteral.bal"},
                {"expectedXMLAttribute.bal", "xmlAttribute.bal"},
                {"expectedXMLAttributeAccessExpr.bal", "xmlAttributeAccessExpr.bal"},
                {"expectedXMLQName.bal", "xmlQName.bal"},
                {"expectedUnicodeCharTest.bal", "unicodeCharTest.bal"},
                {"expectedTupleTypeRest.bal", "tupleTypeRest.bal"},
                {"expectedTupleDestructure.bal", "tupleDestructure.bal"},
                {"expectedNegativeEOF.bal", "negativeEOF.bal"},
                {"expectedListConstructorExpr.bal", "listConstructorExpr.bal"},
                {"expectedRecordLiteralSpreadOp.bal", "recordLiteralSpreadOp.bal"},
                {"expectedLetExpr.bal", "letExpr.bal"},
                // TODO: #23370
                // {"expectedStreamingQueries.bal", "streamingQueries.bal"},
        };
    }
}
