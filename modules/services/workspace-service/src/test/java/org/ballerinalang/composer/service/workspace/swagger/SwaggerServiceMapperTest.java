/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.composer.service.workspace.swagger;

import com.google.gson.JsonParser;
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
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Test classes for swagger services.
 */
public class SwaggerServiceMapperTest {
    private JsonParser parser = new JsonParser();
    
    /**
     * Data provider for swagger test cases.
     * @return List of swagger test cases.
     */
    @DataProvider(name = "SwaggerSamples")
    public static Object[][] swaggerSamples() {
        return new Object[][] { { "usecase-1", "Service1" },
                                { "usecase-2", "Service1" },
                                { "usecase-3", "Service3" },
                                { "usecase-4", "Service4" },
                                { "usecase-5", "Service5" } };
    }
    
    /**
     * Execute tests converting ballerina source to swagger definitions.
     * @param usecaseName The name of the file.
     * @param serviceName The name of the service.
     * @throws IOException When sample files cannot be read.
     */
    @Test(dataProvider = "SwaggerSamples")
    public void testBallerinaToSwaggerConversion(String usecaseName, String serviceName) throws IOException {
        String ballerinaSource = this.readFile("input/" + usecaseName + ".bal");
        String generatedSwagger = SwaggerConverterUtils.generateSwaggerDefinitions(ballerinaSource, serviceName);
    
        String expectedSwagger = readFile("output/" + usecaseName + ".json");
        Assert.assertTrue(parser.parse(generatedSwagger).equals(parser.parse(expectedSwagger)),
                "Invalid Swagger definition generated.\nExpected: " + parser.parse(expectedSwagger).toString() +
                "\nActual: " + parser.parse(generatedSwagger).toString());
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
    
    String readFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL fileResource = classLoader.getResource("samples/swagger/ballerina-to-swagger/" + fileName);
        if (null != fileResource) {
            File file = new File(fileResource.getFile());
            return FileUtils.readFileToString(file, Charset.defaultCharset());
        } else {
            throw new IOException("Unable to find file:" + " samples/swagger/ballerina-to-swagger/" + fileName);
        }
    }
}
