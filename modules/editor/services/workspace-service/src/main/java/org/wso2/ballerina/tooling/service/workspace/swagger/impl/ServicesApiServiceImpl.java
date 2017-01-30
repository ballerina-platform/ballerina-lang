package org.wso2.ballerina.tooling.service.workspace.swagger.impl;

import com.google.gson.JsonObject;
import io.swagger.annotations.ApiParam;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.parser.BallerinaLexer;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;
import org.wso2.ballerina.tooling.service.workspace.swagger.SwaggerServiceMapper;
import org.wso2.ballerina.tooling.service.workspace.swagger.api.NotFoundException;
import org.wso2.ballerina.tooling.service.workspace.swagger.model.Service;


import javax.ws.rs.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Path("/services")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the services API")
/**
 * Service implementation class for convert both swagger to ballerina service definitions and
 * ballerina to swagger service definitions
 */
public class ServicesApiServiceImpl {
    @POST
    @Path("/convert-service")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value =
            "Convert swagger to ballerina service definitions",
            notes = "This operation can be used to convert service definitions between ballerina and swagger ",
            response = void.class, tags = {"swagger",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200,
                    message = "Created.  Successful response with the newly created API as entity in the body. " +
                            "Location header contains URL of newly created API. ", response = void.class)})
    public Service servicesConvertServicePost(@ApiParam(value = "Type to be convert", required = true)
                                              @QueryParam("expectedType") String expectedType
            , @ApiParam(value = "Service definition to be convert ", required = true) Service serviceDefinition
    )
            throws NotFoundException {
        try {
            InputStream stream = new ByteArrayInputStream(serviceDefinition.getBallerinaDefinition().
                    getBytes(StandardCharsets.UTF_8));
            String response = parseSwaggerDataModel(stream);
            serviceDefinition.setSwaggerDefinition(response);
            return serviceDefinition;
            //return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, response)).build();
        } catch (IOException ex) {
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", ex.toString());
            //TODO send proper error
            /*return Response.status(Response.Status.BAD_REQUEST).entity(entity)
                    .header("Access-Control-Allow-Origin", '*')
                    .type(MediaType.APPLICATION_JSON).build();*/
        } catch (Exception e) {
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", e.toString());
            //TODO send proper error
        /*            return Response.status(Response.Status.BAD_REQUEST).entity(entity)
                    .header("Access-Control-Allow-Origin", '*')
                    .type(MediaType.APPLICATION_JSON).build();
        */
        }
        return null;
    }

    /**
     *
     * @param stream stream to be processed as ballerina service definition
     * @return String swagger data model generated from ballerina definition
     * @throws IOException when input process error occur.
     */
    private String parseSwaggerDataModel(InputStream stream) throws IOException {
        //TODO improve code to avoid additional object creation.
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(stream);
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);
        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
        BLangModelBuilder modelBuilder = new BLangModelBuilder();
        BLangAntlr4Listener langModelBuilder = new BLangAntlr4Listener(modelBuilder);
        ballerinaParser.addParseListener(langModelBuilder);
        ballerinaParser.compilationUnit();
        BallerinaFile bFile = modelBuilder.build();
        List<org.wso2.ballerina.core.model.Service> services = bFile.getServices();
        String swaggerDefinition = "";
        if (services.size() > 0) {
            //TODO this need to improve iterate through multiple services and generate single swagger file.
            SwaggerServiceMapper swaggerServiceMapper = new SwaggerServiceMapper(services.get(0));
            //TODO mapper type need to set according to expected type.
            //swaggerServiceMapper.setObjectMapper(io.swagger.util.Yaml.mapper());
            swaggerDefinition = swaggerServiceMapper.generateSwaggerString();
        }
        return swaggerDefinition;
    }

}

