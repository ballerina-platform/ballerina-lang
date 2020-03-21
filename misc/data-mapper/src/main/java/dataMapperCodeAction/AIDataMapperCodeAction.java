package dataMapperCodeAction;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;

import static org.ballerinalang.langserver.util.references.ReferencesUtil.getReferenceAtCursor;

/**
 * Code Action provider for automatic data mapping.
 */

@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AIDataMapperCodeAction extends AbstractCodeActionProvider {

    private final static String REMOTE_URL = "https://ai-data-mapper.development.choreo.dev/uploader";
    private final static String REMOTE_AI_SERVICE_URL_ENV = "REMOTE_AI_SERVICE_URL";
    private static final String CUSTOM_URL = System.getenv(REMOTE_AI_SERVICE_URL_ENV);
    private static String AI_SERVICE_URL = (CUSTOM_URL == null || CUSTOM_URL.length() == 0) ? REMOTE_URL : CUSTOM_URL;


    public static CodeAction getAIDataMapperCommand(LSDocumentIdentifier document, Diagnostic diagnostic,
                                                    LSContext context) {
        /* TODO: Complete the command and code action */
        Position startingPosition = diagnostic.getRange().getStart();
        Position endingPosition = diagnostic.getRange().getEnd();

        try {
            Position afterAliasPos = new Position(startingPosition.getLine(),
                    (startingPosition.getCharacter() + endingPosition.getCharacter()) / 2);
            SymbolReferencesModel.Reference refAtCursor;
            if (endingPosition.getCharacter() - startingPosition.getCharacter() > 1) {
                refAtCursor = getReferenceAtCursor(context, document, afterAliasPos);
            } else {
                refAtCursor = getReferenceAtCursor(context, document, endingPosition);
            }
            int symbolAtCursorTag = refAtCursor.getSymbol().type.tag;

            if (refAtCursor.getbLangNode().parent instanceof BLangFieldBasedAccess) {
                return null;
            } else {
                if (symbolAtCursorTag == 12) { // tag 12 is user defined records or non-primitive types (?)
                    String commandTitle = "AI Data Mapper";
                    CodeAction action = new CodeAction(commandTitle);
                    action.setKind(CodeActionKind.QuickFix);

                    String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
                    List<TextEdit> fEdits = getAIDataMapperCodeActionEdits(document, context, refAtCursor, diagnostic);
                    action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                            new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), fEdits)))));
                    List<Diagnostic> diagnostics = new ArrayList<>();
                    action.setDiagnostics(diagnostics);
                    return action;
                } else {
                    return null;
                }
            }
        } catch (CompilationFailedException | WorkspaceDocumentException | IOException e) { // | IOException e) {
            // ignore
        }
        return null;
    }

    private static List<TextEdit> getAIDataMapperCodeActionEdits(LSDocumentIdentifier document, LSContext context,
                                                                 SymbolReferencesModel.Reference refAtCursor,
                                                                 Diagnostic diagnostic)
            throws IOException, WorkspaceDocumentException {

        List<TextEdit> fEdits = new ArrayList<>();
        String diagnosticMessage = diagnostic.getMessage();
        Matcher matcher = CommandConstants.INCOMPATIBLE_TYPE_PATTERN.matcher(diagnosticMessage);

        if (matcher.find() && matcher.groupCount() > 1) {
            String foundTypeLeft = matcher.group(1)
                    .replaceAll(refAtCursor.getSymbol().type.tsymbol.pkgID.toString() + ":",
                            "");  // variable at left side of the equal sign
            String foundTypeRight = matcher.group(2)
                    .replaceAll(refAtCursor.getSymbol().type.tsymbol.pkgID.toString() + ":",
                            "");  // variable at right side of the equal sign

            // Insert function call in the code where error is found
            BLangNode bLangNode = refAtCursor.getbLangNode();
            Position startPos = new Position(bLangNode.pos.sLine - 1, bLangNode.pos.sCol - 1);
            Position endPosWithSemiColon = new Position(bLangNode.pos.eLine - 1, bLangNode.pos.eCol);
            Range newTextRange = new Range(startPos, endPosWithSemiColon);

            BSymbol symbolAtCursor = refAtCursor.getSymbol();
            String variableAtCursor = symbolAtCursor.name.value;
            String generatedFunctionName =
                    String.format("map%sTo%s(%s);", foundTypeRight, foundTypeLeft, variableAtCursor);

            TextEdit functionNameEdit = new TextEdit(newTextRange, generatedFunctionName);
            fEdits.add(functionNameEdit);

            // Insert function declaration at the bottom of the file
            WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
            Optional<Path> filePath = CommonUtil.getPathFromURI(context.get(DocumentServiceKeys.FILE_URI_KEY));
            String fileContent = docManager.getFileContent(Paths.get(String.valueOf(filePath.get())));
            String functionName = String.format("map%sTo%s (%s", foundTypeRight, foundTypeLeft, foundTypeRight);
            int indexOfFunctionName = fileContent.indexOf(functionName);
            if (indexOfFunctionName == -1) {
                int numberOfLinesInFile = fileContent.split("\n").length;
                Position startPosOfLastLine = new Position(numberOfLinesInFile + 3, 0);
                Position endPosOfLastLine = new Position(numberOfLinesInFile + 3, 1);
                Range newFunctionRange = new Range(startPosOfLastLine, endPosOfLastLine);
                String generatedRecordMappingFunction =
                        getGeneratedRecordMappingFunction(bLangNode, document, context, diagnostic, symbolAtCursor,
                                docManager, foundTypeLeft, foundTypeRight);
                TextEdit functionEdit = new TextEdit(newFunctionRange, generatedRecordMappingFunction);
                fEdits.add(functionEdit);
            }
        }
        return fEdits;
    }

    private static String getGeneratedRecordMappingFunction(BLangNode bLangNode, LSDocumentIdentifier document,
                                                            LSContext context, Diagnostic diagnostic,
                                                            BSymbol symbolAtCursor, WorkspaceDocumentManager docManager,
                                                            String foundTypeLeft, String foundTypeRight)
            throws IOException {
        JsonObject rightRecordJSON = new JsonObject();
        JsonObject leftRecordJSON = new JsonObject();

        // Schema 1
        BType variableTypeMappingFrom = symbolAtCursor.type;
        if (variableTypeMappingFrom instanceof BRecordType) {
            List<BField> rightSchemaFields = ((BRecordType) variableTypeMappingFrom).fields;
            JsonObject rightSchema = (JsonObject) recordToJSON(rightSchemaFields);

            rightRecordJSON.addProperty("schema", foundTypeRight);
            rightRecordJSON.addProperty("id", "dummy_id");
            rightRecordJSON.addProperty("type", "object");
            rightRecordJSON.add("properties", rightSchema);
        }
        // Schema 2

        BType variableTypeMappingTo = ((BLangSimpleVarRef) bLangNode).expectedType;
        if (variableTypeMappingTo instanceof BRecordType) {
            List<BField> leftSchemaFields = ((BRecordType) variableTypeMappingTo).fields;
            JsonObject leftSchema = (JsonObject) recordToJSON(leftSchemaFields);

            leftRecordJSON.addProperty("schema", foundTypeLeft);
            leftRecordJSON.addProperty("id", "dummy_id");
            leftRecordJSON.addProperty("type", "object");
            leftRecordJSON.add("properties", leftSchema);
        }

        JsonArray schemas = new JsonArray();
        schemas.add(leftRecordJSON);
        schemas.add(rightRecordJSON);

        String schemasToSend = schemas.toString();
        URL url = new URL(AI_SERVICE_URL);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(schemasToSend.getBytes(StandardCharsets.UTF_8));
            try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
                return org.apache.commons.io.IOUtils.toString(in, StandardCharsets.UTF_8);
            }
        }
    }

    private static JsonElement recordToJSON(List<BField> schemaFields) {

        JsonObject properties = new JsonObject();
        for (BField attribute : schemaFields) {
            JsonObject fieldDetails = new JsonObject();
            fieldDetails.addProperty("id", "dummy_id");
            /* TODO: Do we need to go to lower levels? */
            if (attribute.type instanceof BRecordType) {
                if (attribute.type.tag == 12) {
                    fieldDetails.addProperty("type", "ballerina_type");
                    fieldDetails.add("properties", recordToJSON(((BRecordType) attribute.type).fields));
                } else {
                    fieldDetails.addProperty("type", String.valueOf(attribute.type));
                }
            }
            properties.add(String.valueOf(attribute.name), fieldDetails);
        }
        return properties;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public List<CodeAction> getDiagBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> diagnosticsOfRange,
                                                    List<Diagnostic> allDiagnostics) {

        List<CodeAction> actions = new ArrayList<>();
        WorkspaceDocumentManager documentManager = lsContext.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(lsContext.get(DocumentServiceKeys.FILE_URI_KEY));
        LSDocumentIdentifier document = null;
        try {
            document = documentManager.getLSDocument(filePath.get());
        } catch (WorkspaceDocumentException e) {
            // ignore
        }
        if (document == null) {
            return actions;
        }
        for (Diagnostic diagnostic : diagnosticsOfRange) {
            if (diagnostic.getMessage().toLowerCase(Locale.ROOT).contains(CommandConstants.INCOMPATIBLE_TYPES)) {
                CodeAction codeAction = getAIDataMapperCommand(document, diagnostic, lsContext);
                if (codeAction != null) {
                    actions.add(codeAction);
                }
            }
        }
        return actions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> allDiagnostics) {

        throw new UnsupportedOperationException("Not supported");
    }
}

