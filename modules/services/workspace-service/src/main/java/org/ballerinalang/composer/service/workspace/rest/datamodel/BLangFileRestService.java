/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.composer.service.workspace.rest.datamodel;

import com.google.common.base.CaseFormat;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.parser.BallerinaLexer;
import org.ballerinalang.util.parser.BallerinaParser;
import org.ballerinalang.util.parser.antlr4.BLangAntlr4Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_ROOT;

/**
 * Basic classes which exposes ballerina language object model over REST service.
 */
@Path("/ballerina")
public class BLangFileRestService {

    private static final Logger logger = LoggerFactory.getLogger(BLangFileRestService.class);

    @GET
    @Path("/model")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBallerinaJsonDataModelGivenLocation(@QueryParam("location") String location) throws IOException {
        InputStream stream = null;
        try {
            stream = new FileInputStream(new File(location));
            //String response = parseJsonDataModel(stream, "temp.bal");
            return Response.ok("", MediaType.APPLICATION_JSON).build();
        } finally {
            if (null != stream) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateBallerinaSource(BFile bFile) throws IOException {
        InputStream stream = new ByteArrayInputStream(bFile.getContent().getBytes(StandardCharsets.UTF_8));
        return Response.status(Response.Status.OK)
                .entity(validate(stream, deriveFilePath(bFile.getFileName(), bFile.getFilePath())))
                .header("Access-Control-Allow-Origin", '*').type(MediaType.APPLICATION_JSON).build();
    }

    @OPTIONS
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateOptions() {
        return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Credentials",
                "true").header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }

    @POST
    @Path("/model/content")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBallerinaJsonDataModelGivenContent(BFile bFile) throws IOException {
        InputStream stream = new ByteArrayInputStream(bFile.getContent().getBytes(StandardCharsets.UTF_8));
        String response = "";
        if ("temp".equals(bFile.getFilePath()) && "untitled".equals(bFile.getFileName())) {
            response = parseJsonDataModel(bFile.getContent(), bFile.getFileName());
        } else {
            response = parseJsonDataModel(stream, bFile.getFilePath(), bFile.getFileName());
        }

        return Response.ok(response, MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", '*').build();
    }

    @OPTIONS
    @Path("/model/content")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response options() {
        return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Credentials",
                "true").header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }


    @POST
    @Path("/model/parse-fragment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBallerinaJsonDataModelGivenFragment(BLangSourceFragment sourceFragment) throws IOException {
        String response = BLangFragmentParser.parseFragment(sourceFragment);
        return Response.ok(response, MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", '*').build();
    }

    @OPTIONS
    @Path("/model/parse-fragment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response optionsParseFragment() {
        return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Credentials",
                "true").header("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }

    /**
     * Parses an input stream into a json model. During this parsing we are compiling the code as well.
     *
     * @param stream - The input stream.
     * @return A string which contains a json model.
     * @throws IOException
     */
    private static String parseJsonDataModel(InputStream stream, String filePath, String fileName) throws IOException {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(SOURCE_ROOT, filePath);

        Compiler compiler = Compiler.getInstance(context);
        org.wso2.ballerinalang.compiler.tree.BLangPackage model = compiler.getModel(fileName);
        BLangCompilationUnit compilationUnit = model.getCompilationUnits().stream().
                filter(compUnit -> fileName.equals(compUnit.getName())).findFirst().get();
        return generateJSONString(compilationUnit);
    }

    private static String generateJSONString(Node node) {
        try {
            return generateJSON(node).toString();
        } catch (InvocationTargetException | IllegalAccessException e) {
            // This should never occur.
            throw new AssertionError("Error while serializing source to JSON.");
        }
    }

    private static JsonElement generateJSON(Node node) throws InvocationTargetException, IllegalAccessException {
        if (node == null) {
            return JsonNull.INSTANCE;
        }
        List<Method> methods = Arrays.stream(node.getClass().getInterfaces())
                .flatMap(aClass -> Arrays.stream(aClass.getMethods()))
                .collect(Collectors.toList());
        JsonObject nodeJson = new JsonObject();

        JsonArray wsJson = new JsonArray();
        Set<Whitespace> ws = node.getWS();
        if (ws != null && !ws.isEmpty()) {
            for (Whitespace whitespace : ws) {
                wsJson.add(whitespace.getWs());
            }
            nodeJson.add("ws", wsJson);
        }
        Diagnostic.DiagnosticPosition position = node.getPosition();
        if (position != null) {
            JsonObject positionJson = new JsonObject();
            positionJson.addProperty("startColumn", position.startColumn());
            positionJson.addProperty("startLine", position.getStartLine());
            positionJson.addProperty("endColumn", position.endColumn());
            positionJson.addProperty("endLine", position.getEndLine());
            nodeJson.add("position", positionJson);
        }
        for (Method m : methods) {
            String name = m.getName();

            if (name.equals("getWS") || name.equals("getPosition")) {
                continue;
            }

            String jsonName = null;
            if (name.startsWith("get")) {
                jsonName = toJsonName(name, 3);
            } else if (name.startsWith("is")) {
                jsonName = toJsonName(name, 2);
            }

            if (jsonName != null) {
                Object prop = m.invoke(node);
                if (prop instanceof Node) {
                    nodeJson.add(jsonName, generateJSON((Node) prop));
                } else if (prop instanceof List) {
                    List listProp = (List) prop;
                    JsonArray listPropJson = new JsonArray();
                    nodeJson.add(jsonName, listPropJson);
                    for (Object listPropItem : listProp) {
                        if (listPropItem instanceof Node) {
                            listPropJson.add(generateJSON((Node) listPropItem));
                        } else {
                            throw new AssertionError("Assuming all lists are of type Node.");
                        }
                    }
                } else if (prop instanceof Set && jsonName.equals("flags")) {
                    Set flags = (Set) prop;
                    for (Flag flag : Flag.values()) {
                        nodeJson.addProperty(flag.toString().toLowerCase(), flags.contains(flag));
                    }
                } else if (prop instanceof PackageID) {
                    PackageID id = (PackageID) prop;
                    nodeJson.addProperty("package", id.getPackageName().toString());
                    nodeJson.addProperty("packageVersion", id.getPackageVersion().toString());
                    JsonArray comps = new JsonArray();
                    List<IdentifierNode> nameComps = id.getNameComps();
                    for (int compI = 0; compI < nameComps.size(); compI++) {
                        IdentifierNode i = nameComps.get(compI);
                        if (compI != 0) {
                            comps.add(".");
                        }
                        comps.add(i.getValue());
                    }
                    nodeJson.add("packageComps", comps);
                } else if (prop instanceof String) {
                    nodeJson.addProperty(jsonName, (String) prop);
                } else if (prop instanceof Number) {
                    nodeJson.addProperty(jsonName, (Number) prop);
                } else if (prop instanceof Boolean) {
                    nodeJson.addProperty(jsonName, (Boolean) prop);
                } else if (prop instanceof NodeKind) {
                    String KindName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, prop.toString());
                    nodeJson.addProperty(jsonName, KindName);
                } else if (prop instanceof TypeKind) {
                    nodeJson.addProperty(jsonName, prop.toString());
                } else if (prop != null) {
                    throw new AssertionError("Node " + node.getClass().getSimpleName() +
                            " contains unknown type prop: " + jsonName + " of type " + prop.getClass());
                }
            }
        }
        return nodeJson;
    }

    private static String toJsonName(String name, int prefixLen) {
        return Character.toLowerCase(name.charAt(prefixLen)) + name.substring(prefixLen + 1);
    }


    /**
     * Parses an input stream into a json model. During this parsing we are compiling the code as well.
     *
     * @param content - String content.
     * @return A string which contains a json model.
     * @throws IOException
     */
    private static String parseJsonDataModel(String content, String fileName) throws IOException {
        CompilerContext context = new CompilerContext();
        context.put(PackageRepository.class, new InMemoryPackageRepository(content.getBytes(StandardCharsets.UTF_8)));

        CompilerOptions options = CompilerOptions.getInstance(context);
        Compiler compiler = Compiler.getInstance(context);
        org.wso2.ballerinalang.compiler.tree.BLangPackage model = compiler.getModel(fileName);

        BLangCompilationUnit compilationUnit = model.getCompilationUnits().stream().findFirst().get();
        return generateJSONString(compilationUnit);
    }

    /**
     * Validates a given ballerina input
     *
     * @param stream - The input stream.
     * @return List of errors if any
     * @throws IOException
     */
    private JsonObject validate(InputStream stream, java.nio.file.Path filePath) throws IOException {

        ANTLRInputStream antlrInputStream = new ANTLRInputStream(stream);
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
        BallerinaComposerErrorStrategy errorStrategy = new BallerinaComposerErrorStrategy();
        ballerinaParser.setErrorHandler(errorStrategy);

        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BLangPackage bLangPackage = new BLangPackage(globalScope);
        BLangPackage.PackageBuilder packageBuilder = new BLangPackage.PackageBuilder(bLangPackage);

        BallerinaComposerModelBuilder bLangModelBuilder = new BallerinaComposerModelBuilder(packageBuilder,
                StringUtils.EMPTY);
        BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(bLangModelBuilder, filePath);
        ballerinaParser.addParseListener(ballerinaBaseListener);
        try {
            ballerinaParser.compilationUnit();
        } catch (Exception e) {
            logger.debug("Model building error", e);
        }

        JsonArray errors = new JsonArray();

        for (SyntaxError error : errorStrategy.getErrorTokens()) {
            errors.add(error.toJson());
        }

        JsonObject result = new JsonObject();
        result.add("errors", errors);

        return result;
    }

    private java.nio.file.Path deriveFilePath(String fileName, String filePath) {
        return Paths.get(filePath + File.separator + fileName);
    }

}
