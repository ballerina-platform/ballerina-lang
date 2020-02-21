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
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
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
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
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
import java.util.function.BiConsumer;

import static org.ballerinalang.langserver.command.CommandUtil.applyWorkspaceEdit;
import static org.ballerinalang.langserver.command.CommandUtil.getServiceNode;
import static org.ballerinalang.openapi.utils.TypeExtractorUtil.extractOpenApiOperations;

/**
 * Represents the command executor for creating a openAPI service resource.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class CreateOpenApiServiceResourceMethodExecutor implements LSCommandExecutor {

    public static final String COMMAND = "CREATE_SERVICE_RESOURCE_METHOD";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String documentUri = null;
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();
        String resourcePath = null;
        String resourceMethod = null;
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
                case CommandConstants.ARG_KEY_PATH:
                    resourcePath = argVal;
                    break;
                case CommandConstants.ARG_KEY_METHOD:
                    resourceMethod = argVal;
                    break;
                default:
            }
        }

        if (line == -1 || column == -1 || documentUri == null) {
            throw new LSCommandExecutorException("Invalid parameters received for the create function command!");
        }

        WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);

        BLangService serviceNode;
        try {
            serviceNode = getServiceNode(line, column, documentUri, documentManager, context);
            List<BLangFunction> functions =
                    ((BLangObjectTypeNode) (serviceNode.serviceTypeDefinition.typeNode)).functions;
            String contractURI = null;

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

            if (contractURI != null) {
                OpenAPI openAPI = parseOpenAPIFile(contractURI);
                String editText = "";
                List<BallerinaOpenApiPath> paths = extractOpenApiPaths(openAPI.getPaths());
                for (BallerinaOpenApiPath path : paths) {
                    if (path.getPath().equals(resourcePath)) {
                        String finalResourceMethod = resourceMethod;
                        // remove already available methods
                        path.getOperationsList().removeIf(operation -> !operation.getOpMethod().equalsIgnoreCase(
                                finalResourceMethod));
                        editText = getContent(path, "/openAPITemplates", "balFunction");
                    }
                }

                BLangNode parent = serviceNode.parent;
                BLangPackage packageNode = CommonUtil.getPackageNode(serviceNode);

                List<TextEdit> edits = new ArrayList<>();
                if (parent != null && packageNode != null) {
                    BiConsumer<String, String> importsAcceptor = (orgName, alias) -> {
                        boolean notFound = packageNode.getImports().stream().noneMatch(
                                pkg -> (pkg.orgName.value.equals(orgName) && pkg.alias.value.equals(alias))
                        );
                        if (notFound) {
                            String pkgName = orgName + "/" + alias;
                            edits.add(addPackage(pkgName, context));
                        }
                    };
                } else {
                    throw new LSCommandExecutorException("Error occurred when retrieving function node!");
                }
                LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_CLIENT_KEY);
                DiagnosticPos serviceNodePos = serviceNode.pos;
                Range range = new Range(new Position(serviceNodePos.eLine - 1, serviceNodePos.eCol - 2),
                                        new Position(serviceNodePos.eLine - 1, serviceNodePos.eCol - 2));
                edits.add(new TextEdit(range, editText));
                TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier, edits);
                return applyWorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)), client);

            }
        } catch (CompilationFailedException e) {
            throw new LSCommandExecutorException("Error while compiling the source!");
        } catch (BallerinaOpenApiException | IOException e) {
            e.printStackTrace();
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

    private String getContent(BallerinaOpenApiPath object, String templateDir, String templateName) throws IOException {
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
