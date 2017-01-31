package org.wso2.ballerina.tooling.service.workspace.swagger.impl;

import com.google.gson.JsonObject;
import io.swagger.annotations.ApiParam;
import io.swagger.models.Swagger;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.parser.BallerinaLexer;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;
import org.wso2.ballerina.tooling.service.workspace.swagger.SwaggerServiceMapper;
import org.wso2.ballerina.tooling.service.workspace.swagger.api.NotFoundException;
import org.wso2.ballerina.tooling.service.workspace.swagger.model.Service;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    private static final Logger logger = LoggerFactory.getLogger(ServicesApiServiceImpl.class);

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
    public Response servicesConvertServicePost(@ApiParam(value = "Type to be convert", required = true)
                                               @QueryParam("expectedType") String expectedType
            , @ApiParam(value = "Service definition to be convert ", required = true) Service serviceDefinition)
            throws NotFoundException {
        try {
            String response = parseSwaggerDataModel(serviceDefinition.getSwaggerDefinition()).getSwagger();
            serviceDefinition.setSwaggerDefinition(response);
            return Response.ok().entity(serviceDefinition).build();
            //return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, response)).build();
        } catch (IOException ex) {
            logger.error("Error while processing service definition at converter service" + ex.getMessage());
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", ex.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity(entity)
                    .header("Access-Control-Allow-Origin", '*')
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }


    /**
     * This method will convert ballerina definition to swagger string. Since swagger is subset of ballerina defintion
     * we can implement converter logic without data loss.
     *
     * @param ballerinaDefinition String ballerina config to be processed as ballerina service definition
     * @return swagger data model generated from ballerina definition
     * @throws IOException when input process error occur.
     */
    private Swagger parseSwaggerDataModel(String ballerinaDefinition) throws IOException {
        //TODO improve code to avoid additional object creation.
        InputStream stream = new ByteArrayInputStream(ballerinaDefinition.
                getBytes(StandardCharsets.UTF_8));
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
        Swagger swaggerDefinition = new Swagger();
        if (services.size() > 0) {
            //TODO this need to improve iterate through multiple services and generate single swagger file.
            SwaggerServiceMapper swaggerServiceMapper = new SwaggerServiceMapper();
            //TODO mapper type need to set according to expected type.
            //swaggerServiceMapper.setObjectMapper(io.swagger.util.Yaml.mapper());
            swaggerDefinition = swaggerServiceMapper.convertServiceToSwagger(services.get(0));
        }
        return swaggerDefinition;
    }

    /**
     * This method will generate ballerina string from swagger definition. Since ballerina service definition
     * is super set of swagger definition we will take both swagger and ballerina definition and merge swagger
     * changes to ballerina definition selectively to prevent data loss
     *
     * @param swaggerDefinition @String swagger definition to be processed as swagger
     * @param ballerinaDefinition @String ballerina definition to be process as ballerina definition
     * @return @String representation of converted ballerina source
     * @throws IOException when error occur while processing input swagger and ballerina definitions.
     */
    private String parseBallerinaDataModel(String swaggerDefinition, String ballerinaDefinition) throws IOException {
        Swagger swagger = parseSwaggerDataModel(swaggerDefinition);
        //TODO parse ballerina string and process it as ballerina service object and pass it to convertor.
        //TODO improve code to avoid additional object creation.
        SwaggerServiceMapper swaggerServiceMapper = new SwaggerServiceMapper();
        swaggerServiceMapper.convertSwaggerToService(swagger, null);
        return swagger.getSwagger();
    }

}

