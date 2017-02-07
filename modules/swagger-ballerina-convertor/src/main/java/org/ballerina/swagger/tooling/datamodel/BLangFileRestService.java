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

package org.ballerina.swagger.tooling.datamodel;

import com.google.gson.JsonObject;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.parser.BallerinaLexer;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.semantics.SemanticAnalyzer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Basic classes which exposes ballerina language object model over REST service.
 */
@Path("/ballerina")
public class BLangFileRestService {

    private static final Logger logger = LoggerFactory.getLogger(BLangFileRestService.class);

    @GET
    @Path("/model")
    @Produces("application/json")
    public Response getBallerinaJsonDataModelGivenLocation(@QueryParam("location") String location) {
        try {
            InputStream stream = new FileInputStream(new File(location));
            String response = parseJsonDataModel(stream);
            return Response.ok(response, MediaType.APPLICATION_JSON).build();
        } catch (IOException ex) {
            logger.error("error: failed to generate JSON data model for ballerina file", ex.getMessage());
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", "Error Generating JSON Model");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity)
                    .header("Access-Control-Allow-Origin", '*')
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }

    @POST
    @Path("/model/content")
    @Consumes("application/json")
    @Produces("application/json")
    public Response getBallerinaJsonDataModelGivenContent(BFileContent content) {
        try {
            InputStream stream = new ByteArrayInputStream(content.getContent().getBytes(StandardCharsets.UTF_8));
            String response = parseJsonDataModel(stream);
            return Response.ok(response, MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", '*').build();
        } catch (Throwable e) {
            logger.error("error: failed to generate JSON data model for ballerina file", e.getMessage());
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", "Error Generating JSON Model");
            return Response.status(Response.Status.BAD_REQUEST).entity(entity)
                           .header("Access-Control-Allow-Origin", '*')
                           .type(MediaType.APPLICATION_JSON).build();
        }
    }

    @OPTIONS
    @Path("/model/content")
    @Consumes("application/json")
    @Produces("application/json")
    public Response options() {
        return Response.ok()
                       .header("Access-Control-Allow-Origin", "*")
                       .header("Access-Control-Allow-Credentials", "true")
                       .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                       .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                       .build();
    }

    private String parseJsonDataModel(InputStream stream) throws IOException {

        ANTLRInputStream antlrInputStream = new ANTLRInputStream(stream);
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);

        BLangModelBuilder modelBuilder = new BLangModelBuilder();
        BLangAntlr4Listener langModelBuilder = new BLangAntlr4Listener(modelBuilder);

        ballerinaParser.addParseListener(langModelBuilder);
        ballerinaParser.compilationUnit();

        BallerinaFile bFile = modelBuilder.build();

        SymScope globalScope = GlobalScopeHolder.getInstance().getScope();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bFile, globalScope);
        // TODO: There is a bug in SemanticAnalyzer of the Ballerina engine. Temporary avoiding Semantic Analyzing due to that issue.
        //bFile.accept(semanticAnalyzer);

        JsonObject response = new JsonObject();
        BLangJSONModelBuilder jsonModelBuilder = new BLangJSONModelBuilder(response);
        bFile.accept(jsonModelBuilder);

        return response.toString();
    }

}
