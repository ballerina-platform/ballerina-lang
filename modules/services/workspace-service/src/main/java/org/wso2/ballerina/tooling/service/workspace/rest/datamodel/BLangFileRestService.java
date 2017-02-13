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

package org.wso2.ballerina.tooling.service.workspace.rest.datamodel;

import com.google.gson.JsonObject;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.GlobalScope;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.parser.BallerinaLexer;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.parser.BallerinaParserErrorStrategy;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.semantics.SemanticAnalyzer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
        InputStream stream = new FileInputStream(new File(location));
        String response = parseJsonDataModel(stream);
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Path("/model/content")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBallerinaJsonDataModelGivenContent(BFileContent content) throws IOException {
        InputStream stream = new ByteArrayInputStream(content.getContent().getBytes(StandardCharsets.UTF_8));
        String response = parseJsonDataModel(stream);
        return Response.ok(response, MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", '*').build();
    }

    @OPTIONS
    @Path("/model/content")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response options() {
        return Response.ok()
                       .header("Access-Control-Allow-Origin", "*")
                       .header("Access-Control-Allow-Credentials", "true")
                       .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                       .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                       .build();
    }
    
    /**
     * Parses an input stream into a json model. During this parsing we are compiling the code as well.
     * @param stream - The input stream.
     * @return A string which contains a json model.
     * @throws IOException
     */
    private String parseJsonDataModel(InputStream stream) throws IOException {

        ANTLRInputStream antlrInputStream = new ANTLRInputStream(stream);
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
        ballerinaParser.setErrorHandler(new BallerinaParserErrorStrategy());

        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BLangPackage bLangPackage = new BLangPackage(globalScope);

        BLangModelBuilder bLangModelBuilder = new BLangModelBuilder(bLangPackage);
        BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(bLangModelBuilder);
        ballerinaParser.addParseListener(ballerinaBaseListener);
        ballerinaParser.compilationUnit();
        BallerinaFile bFile = bLangModelBuilder.build();

        BuiltInNativeConstructLoader.loadConstructs(globalScope);

        JsonObject response = new JsonObject();
        BLangJSONModelBuilder jsonModelBuilder = new BLangJSONModelBuilder(response);
        bFile.accept(jsonModelBuilder);

        return response.toString();
    }

}
