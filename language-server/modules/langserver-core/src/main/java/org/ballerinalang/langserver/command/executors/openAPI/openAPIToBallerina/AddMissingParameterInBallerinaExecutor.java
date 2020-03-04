package org.ballerinalang.langserver.command.executors.openAPI.openAPIToBallerina;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.google.gson.JsonObject;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.util.references.ReferencesKeys;
import org.ballerinalang.langserver.util.references.ReferencesUtil;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.typemodel.BallerinaOpenApiOperation;
import org.ballerinalang.openapi.typemodel.BallerinaOpenApiParameter;
import org.ballerinalang.openapi.typemodel.BallerinaOpenApiPath;
import org.ballerinalang.openapi.utils.GeneratorConstants;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.langserver.command.CommandUtil.applyWorkspaceEdit;
import static org.ballerinalang.openapi.utils.TypeExtractorUtil.extractOpenApiOperations;

/**
 * Represents the command executor for creating a openAPI service resource method missing parameter.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class AddMissingParameterInBallerinaExecutor implements LSCommandExecutor {

    public static final String COMMAND = "ADD_MISSING_PARAMETER";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String documentUri = null;
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();
        String resourceParameter = null;
        String resourceMethod = null;
        String resourcePath = null;
        int line = -1;
        int column = -1;

        for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            String argKey = ((JsonObject) arg).get(ARG_KEY).getAsString();
            String argVal = ((JsonObject) arg).get(ARG_VALUE).getAsString();
            switch (argKey) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    documentUri = argVal;
                    textDocumentIdentifier.setUri(documentUri);
                    context.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
                    break;
                case CommandConstants.ARG_KEY_NODE_LINE:
                    line = Integer.parseInt(argVal);
                    break;
                case CommandConstants.ARG_KEY_NODE_COLUMN:
                    column = Integer.parseInt(argVal);
                    break;
                case CommandConstants.ARG_KEY_PARAMETER:
                    resourceParameter = argVal;
                    break;
                case CommandConstants.ARG_KEY_METHOD:
                    resourceMethod = argVal;
                    break;
                case CommandConstants.ARG_KEY_PATH:
                    resourcePath = argVal;
                    break;
                default:
            }
        }

        if (line == -1 || column == -1 || documentUri == null) {
            throw new LSCommandExecutorException("Invalid parameters received for the create function command!");
        }

        WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);

        BLangService serviceNode = null;
        try {
            LSDocumentIdentifier lsDocument = documentManager.getLSDocument(
                    CommonUtil.getPathFromURI(documentUri).get());
            Position pos = new Position(line, column + 1);
            context.put(ReferencesKeys.OFFSET_CURSOR_N_TRY_NEXT_BEST, true);
            context.put(ReferencesKeys.DO_NOT_SKIP_NULL_SYMBOLS, true);
            SymbolReferencesModel.Reference refAtCursor = ReferencesUtil.getReferenceAtCursor(context, lsDocument, pos);
            BLangNode bLangNode = refAtCursor.getbLangNode();
            if (bLangNode instanceof BLangService) {
                serviceNode = (BLangService) bLangNode;
            }
            String contractURI = null;

            if (serviceNode != null) {
                List<BLangAnnotationAttachment> annotations = serviceNode.annAttachments;
                for (BLangAnnotationAttachment annotation : annotations) {
                    if (annotation.getExpression() instanceof BLangRecordLiteral) {
                        BLangRecordLiteral recordLiteral = (BLangRecordLiteral) annotation.getExpression();
                        for (BLangRecordLiteral.RecordField field : recordLiteral.getFields()) {
                            BLangExpression keyExpr;
                            BLangExpression valueExpr;

                            if (field.isKeyValueField()) {
                                BLangRecordLiteral.BLangRecordKeyValueField keyValue =
                                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                                keyExpr = keyValue.getKey();
                                valueExpr = keyValue.getValue();
                            } else {
                                BLangRecordLiteral.BLangRecordVarNameField varNameField =
                                        (BLangRecordLiteral.BLangRecordVarNameField) field;
                                keyExpr = varNameField;
                                valueExpr = varNameField;
                            }

                            if (keyExpr instanceof BLangSimpleVarRef) {
                                BLangSimpleVarRef contract = (BLangSimpleVarRef) keyExpr;
                                String key = contract.getVariableName().getValue();
                                if (key.equals("contract")) {
                                    if (valueExpr instanceof BLangLiteral) {
                                        BLangLiteral value = (BLangLiteral) valueExpr;
                                        if (value.getValue() instanceof String) {
                                            contractURI = (String) value.getValue();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (contractURI != null) {
                OpenAPI openAPI = parseOpenAPIFile(contractURI);
                String editText = "";
                List<BallerinaOpenApiPath> paths = extractOpenApiPaths(openAPI.getPaths());
                for (BallerinaOpenApiPath path : paths) {
                    if (path.getPath().equals(resourcePath)) {
                        for (BallerinaOpenApiOperation operation : path.getOperationsList()) {
                            if (operation.getOpMethod().equalsIgnoreCase(resourceMethod)) {
                                for (BallerinaOpenApiParameter openApiParameter : operation.getParameterList()) {
                                    if (openApiParameter.getParamName().equals(resourceParameter)) {
                                        editText = getContent(openApiParameter, "/openAPITemplates",
                                                              "balFunctionParameter");
                                    }
                                }
                            }
                        }
                    }
                }

                BLangFunction function = null;
                for (BLangFunction resourceFunction : serviceNode.resourceFunctions) {
                    boolean isPathMatch = false;
                    boolean isMethodMatch = false;
                    for (RecordLiteralNode.RecordField field : ((BLangRecordLiteral) resourceFunction.annAttachments
                            .get(
                                    0).expr).fields) {
                        BLangRecordLiteral.BLangRecordKeyValueField fieldValue =
                                ((BLangRecordLiteral.BLangRecordKeyValueField) field);
                        if (((BLangSimpleVarRef) fieldValue.key.expr).variableName.value
                                .equals("path")) {
                            if (((BLangLiteral) fieldValue.valueExpr).value
                                    .equals(resourcePath)) {
                                isPathMatch = true;
                            }
                        }
                        if (((BLangSimpleVarRef) fieldValue.key.expr).variableName.value
                                .equals("methods")) {
                            if (fieldValue.valueExpr instanceof BLangListConstructorExpr &&
                                    ((BLangLiteral) ((BLangListConstructorExpr) fieldValue.valueExpr).exprs.get(
                                            0)).value.toString()
                                            .equalsIgnoreCase(resourceMethod)) {
                                isMethodMatch = true;
                            }
                        }
                        if (isPathMatch && isMethodMatch) {
                            function = resourceFunction;
                            break;
                        }
                    }
                    if (isPathMatch && isMethodMatch) {
                        break;
                    }
                }

                List<TextEdit> edits = new ArrayList<>();
                if (function != null) {
                    LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_CLIENT_KEY);
                    DiagnosticPos lastParaPos = function.requiredParams.get(function.requiredParams.size() - 1).pos;
                    Range range = new Range(new Position(lastParaPos.eLine - 1, lastParaPos.eCol - 1),
                                            new Position(lastParaPos.eLine - 1, lastParaPos.eCol - 1));
                    edits.add(new TextEdit(range, editText));
                    TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier, edits);
                    return applyWorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)), client);
                }
                return null;

            }
        } catch (CompilationFailedException e) {
            throw new LSCommandExecutorException("Error while compiling the source!");
        } catch (BallerinaOpenApiException | IOException | WorkspaceDocumentException e) {
            throw new LSCommandExecutorException("Couldn't find the function node!");
        }
        return null;
    }

    private TextEdit addPackage(String pkgName, LSContext context) {
        DiagnosticPos pos = null;
        // Filter the imports except the runtime import
        List<BLangImportPackage> imports = CommonUtil.getCurrentFileImports(context);
        if (!imports.isEmpty()) {
            BLangImportPackage lastImport = CommonUtil.getLastItem(imports);
            pos = lastImport.getPosition();
        }

        int endCol = 0;
        int endLine = pos == null ? 0 : pos.getEndLine();

        String editText = "import " + pkgName + ";\n";
        Range range = new Range(new Position(endLine, endCol), new Position(endLine, endCol));
        return new TextEdit(range, editText);
    }

    /**
     * Parse and get the {@link OpenAPI} for the given OpenAPI contract.
     *
     * @param definitionURI URI for the OpenAPI contract
     * @return {@link OpenAPI} OpenAPI model
     */
    static OpenAPI parseOpenAPIFile(String definitionURI) {
        Path contractPath = Paths.get(definitionURI);
        if (Files.exists(contractPath) && (definitionURI.endsWith(".yaml") || definitionURI.endsWith(".json"))) {
            return new OpenAPIV3Parser().read(definitionURI);
        }
        return null;
    }

    private static List<BallerinaOpenApiPath> extractOpenApiPaths(io.swagger.v3.oas.models.Paths defPaths) throws
                                                                                                           BallerinaOpenApiException {
        List<BallerinaOpenApiPath> paths = new ArrayList<>();
        final Iterator<Map.Entry<String, PathItem>> pathIterator = defPaths.entrySet().iterator();

        while (pathIterator.hasNext()) {
            final Map.Entry<String, PathItem> nextPath = pathIterator.next();
            final String pathName = nextPath.getKey();
            final PathItem pathObject = nextPath.getValue();
            BallerinaOpenApiPath typePath = new BallerinaOpenApiPath();

            typePath.setPath(pathName);
            typePath.setOperationsList(extractOpenApiOperations(pathObject.readOperationsMap(), pathName));

            paths.add(typePath);
        }

        return paths;
    }

    private String getContent(BallerinaOpenApiParameter object, String templateDir, String templateName)
            throws IOException {
        Template template = compileTemplate(templateDir, templateName);
        Context context = Context.newBuilder(object)
                .resolver(MapValueResolver.INSTANCE, JavaBeanValueResolver.INSTANCE, FieldValueResolver.INSTANCE)
                .build();
        return template.apply(context);
    }

    private Template compileTemplate(String defaultTemplateDir, String templateName) throws IOException {
        defaultTemplateDir = defaultTemplateDir.replaceAll("\\\\", "/");
        String templatesDirPath = System.getProperty(GeneratorConstants.TEMPLATES_DIR_PATH_KEY, defaultTemplateDir);
        ClassPathTemplateLoader cpTemplateLoader = new ClassPathTemplateLoader((templatesDirPath));
        FileTemplateLoader fileTemplateLoader = new FileTemplateLoader(templatesDirPath);
        cpTemplateLoader.setSuffix(GeneratorConstants.TEMPLATES_SUFFIX);
        fileTemplateLoader.setSuffix(GeneratorConstants.TEMPLATES_SUFFIX);

        Handlebars handlebars = new Handlebars().with(cpTemplateLoader, fileTemplateLoader);
        handlebars.setInfiniteLoops(true); //This will allow templates to call themselves with recursion.
        handlebars.registerHelpers(StringHelpers.class);
        handlebars.registerHelper("equals", (object, options) -> {
            CharSequence result;
            Object param0 = options.param(0);

            if (param0 == null) {
                throw new IllegalArgumentException("found 'null', expected 'string'");
            }
            if (object != null) {
                if (object.toString().equals(param0.toString())) {
                    result = options.fn(options.context);
                } else {
                    result = options.inverse();
                }
            } else {
                result = null;
            }

            return result;
        });

        return handlebars.compile(templateName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
