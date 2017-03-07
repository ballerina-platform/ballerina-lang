package org.ballerinalang.composer.service.workspace.swagger;

import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.CompilationUnit;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.builder.BLangModelBuilder;
import org.ballerinalang.util.parser.BallerinaLexer;
import org.ballerinalang.util.parser.BallerinaParser;
import org.ballerinalang.util.parser.antlr4.BLangAntlr4Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SwaggerServiceMapperTest {
    private static final Logger logger = LoggerFactory.getLogger(SwaggerServiceMapperTest.class);
    @Test
    public void testBallerinaToSwaggerConversion() {
        String ballerinaServiceDefinition = "import ballerina.net.http;\n" +
                "\n" +
                "@BasePath (\"/echo\")\n" +
                "service echo {\n" +
                "\n" +
                "    @http:POST\n" +
                "    resource echo (message m) {\n" +
                "        http:convertToResponse(m);\n" +
                "        reply m;\n" +
                "    }\n" +
                "}";
        InputStream stream = new ByteArrayInputStream(ballerinaServiceDefinition.getBytes(StandardCharsets.UTF_8));
        ANTLRInputStream antlrInputStream = null;
        try {
            antlrInputStream = new ANTLRInputStream(stream);
        } catch (IOException e) {
            logger.error("Error occurred while creating antler input stream.", e);
        }
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);
        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
    
        BLangPackage bLangPackage = new BLangPackage(GlobalScope.getInstance());
        BLangPackage.PackageBuilder packageBuilder = new BLangPackage.PackageBuilder(bLangPackage);
        BLangModelBuilder modelBuilder = new BLangModelBuilder(packageBuilder, "");
    
        BLangAntlr4Listener langModelBuilder = new BLangAntlr4Listener(modelBuilder);
        ballerinaParser.addParseListener(langModelBuilder);
        ballerinaParser.compilationUnit();
        BallerinaFile bFile = modelBuilder.build();
        CompilationUnit[] compilationUnits = bFile.getCompilationUnits();
        Swagger swaggerDefinition = new Swagger();
        for (CompilationUnit compilationUnit : compilationUnits) {
            if (compilationUnit instanceof Service) {
                //TODO this need to improve iterate through multiple services and generate single swagger file.
                SwaggerServiceMapper swaggerServiceMapper = new SwaggerServiceMapper();
                //TODO mapper type need to set according to expected type.
                //swaggerServiceMapper.setObjectMapper(io.swagger.util.Yaml.mapper());
                swaggerDefinition = swaggerServiceMapper.convertServiceToSwagger((Service) compilationUnit);
                break;
            }
        }
        //TODO add complete logic to test all attributes present in swagger object
        //Assert.assertEquals(swaggerDefinition.getBasePath().toString(), "/echo");
        //Assert.assertEquals(swaggerDefinition.getBasePath().toString(), "/echo");
    
    }


    @Test
    public void testSwaggerToBallerinaConversion() {
        Swagger swaggerDefinition = new Swagger();
        final Info info = new Info()
                .version("1.0.0")
                .title("Swagger Petstore");

        final Contact contact = new Contact()
                .name("Swagger API Team")
                .email("foo@bar.baz")
                .url("http://swagger.io");

        info.setContact(contact);
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "value");
        info.setVendorExtension("x-test2", map);
        info.setVendorExtension("x-test", "value");

        final Swagger swagger = new Swagger()
                .info(info)
                .host("petstore.swagger.io")
                .scheme(Scheme.HTTP)
                .consumes("application/json")
                .produces("application/json");

        final Operation get = new Operation()
                .produces("application/json")
                .summary("finds pets in the system")
                .description("a longer description")
                .tag("Pet Operations")
                .operationId("get pet by id")
                .deprecated(true);

        get.parameter(new QueryParameter()
                              .name("tags")
                              .description("tags to filter by")
                              .required(false)
                              .property(new StringProperty())
        );

        get.parameter(new PathParameter()
                              .name("petId")
                              .description("pet to fetch")
                              .property(new LongProperty())
        );

        final Response response = new Response()
                .description("pets returned")
                .schema(new RefProperty().asDefault("Person"))
                .example("application/json", "fun!");

        final Response errorResponse = new Response()
                .description("error response")
                .schema(new RefProperty().asDefault("Error"));

        get.response(200, response)
                .defaultResponse(errorResponse);

        final Operation post = new Operation()
                .summary("adds a new pet")
                .description("you can add a new pet this way")
                .tag("Pet Operations")
                .operationId("add pet")
                .defaultResponse(errorResponse)
                .parameter(new BodyParameter()
                                   .description("the pet to add")
                                   .schema(new RefModel().asDefault("Person")));

        swagger.path("/pets", new Path().get(get).post(post));
        //TODO get ballerina service and test it for all swagger attributes.
        //Service ballrinaService = new SwaggerServiceMapper(swagger).getBallerinaService();
        //TODO add complete logic to test all attributes present in swagger object
        //Assert.assertEquals(swaggerDefinition.getBasePath().toString(), "/echo");
        //Assert.assertEquals(swaggerDefinition.getBasePath().toString(), "/echo");

    }
}
