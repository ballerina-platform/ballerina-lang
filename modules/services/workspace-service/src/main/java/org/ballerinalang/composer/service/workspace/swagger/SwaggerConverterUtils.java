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

import com.google.gson.JsonObject;
import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.ClientOpts;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.Swagger20Parser;
import io.swagger.util.Json;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BLangJSONModelBuilder;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BallerinaComposerErrorStrategy;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BallerinaComposerModelBuilder;
import org.ballerinalang.composer.service.workspace.swagger.generators.BallerinaCodeGenerator;
import org.ballerinalang.model.Annotation;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.CompilationUnit;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.builder.BLangModelBuilder;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.util.parser.BallerinaLexer;
import org.ballerinalang.util.parser.BallerinaParser;
import org.ballerinalang.util.parser.BallerinaParserErrorStrategy;
import org.ballerinalang.util.parser.antlr4.BLangAntlr4Listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Swagger related utility classes.
 */

public class SwaggerConverterUtils {

    /**
     * Maximum loop count when creating temp directories.
     */
    private static final int TEMP_DIR_ATTEMPTS = 10000;

    /**
     * This method will extract service definitions from ballerina source
     *
     * @param ballerinaDefinition service definition to be process as ballerina
     * @return service list which contain all services within give ballerina source
     * @throws IOException when input stream handling error.
     */
    public static Service[] getServicesFromBallerinaDefinition(String ballerinaDefinition) throws IOException {
        BallerinaFile bFile = getBFileFromBallerinaDefinition(ballerinaDefinition);
        List<Service> services = new ArrayList<Service>();
        for (CompilationUnit compilationUnit : bFile.getCompilationUnits()) {
            Service service = compilationUnit instanceof Service ? ((Service) compilationUnit) : null;
            if (service != null) {
                services.add(service);
            }
        }
        return services.toArray(new Service[services.size()]);
    }

    /**
     * Generate ballerina fine from the String definition
     *
     * @param ballerinaDefinition ballerina string definition
     * @return ballerina file created from ballerina string definition
     * @throws IOException IO exception
     */
    public static BallerinaFile getBFileFromBallerinaDefinition(String ballerinaDefinition) throws IOException {
        //TODO this method need to replaced with the utility provided by ballerina core.
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(ballerinaDefinition);
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);
        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
        ballerinaParser.setErrorHandler(new BallerinaComposerErrorStrategy());
        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BLangPackage bLangPackage = new BLangPackage(globalScope);
        BLangPackage.PackageBuilder packageBuilder = new BLangPackage.PackageBuilder(bLangPackage);
        BallerinaComposerModelBuilder bLangModelBuilder = new BallerinaComposerModelBuilder(packageBuilder, StringUtils.EMPTY);
        BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(bLangModelBuilder);
        ballerinaParser.addParseListener(ballerinaBaseListener);
        ballerinaParser.compilationUnit();
        BallerinaFile bFile = bLangModelBuilder.build();
        return bFile;
    }

    /**
     * This method will generate Ballerina service from Swagger definition.
     *
     * @param swaggerDefinition Swagger definition
     * @return service generated from Swagger definition
     * @throws IOException IO exception
     */
    public static Service getServiceFromSwaggerDefinition(String swaggerDefinition) throws IOException {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        Swagger20Parser swagger20Parser = new Swagger20Parser();
        Swagger swagger = swagger20Parser.parse(swaggerDefinition);
        //Iterate through service annotations and add them to service
        Service.ServiceBuilder serviceBuilder = new Service.ServiceBuilder(
                new BLangPackage(GlobalScope.getInstance()));
        serviceBuilder.setName(swagger.getBasePath());
        Service service = serviceBuilder.buildService();
        CodegenConfig codegenConfig = new BallerinaCodeGenerator();
        codegenConfig.setOutputDir(createTempDir().getAbsolutePath());
        ClientOptInput clientOptInput = new ClientOptInput().opts(new ClientOpts()).swagger(swagger).
                config(codegenConfig);
        DefaultGenerator generator = new DefaultGenerator();
        generator.opts(clientOptInput);
        Map<String, List<CodegenOperation>> paths = generator.processPaths(swagger.getPaths());
        Resource[] resources1 = null;
        for (String path : paths.keySet()) {
            List<CodegenOperation> ops = paths.get(path);
            resources1 = mapSwaggerPathsToResources(ops);
        }
        List<Annotation> serviceAnnotationArrayList = new ArrayList<Annotation>();
        serviceAnnotationArrayList.add(new Annotation(null, new SymbolName("http:BasePath"),
                swagger.getBasePath(), null));
        serviceAnnotationArrayList.add(new Annotation(null, new SymbolName("http:Host"),
                swagger.getHost(), null));
        serviceAnnotationArrayList.add(new Annotation(null, new SymbolName("http:Info"),
                Json.pretty(swagger.getInfo()).toString(), null));
        service.setAnnotations(serviceAnnotationArrayList.toArray(
                new Annotation[serviceAnnotationArrayList.size()]));
        //Iterate through paths and add them as resources
        service.setResources(resources1);
        return service;
    }

    /**
     * Atomically creates a new directory somewhere beneath the system's temporary directory (as defined by the {@code
     * java.io.tmpdir} system property), and returns its name.
     *
     * @return the newly-created directory
     * @throws IllegalStateException if the directory could not be created
     */
    private static File createTempDir() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IllegalStateException(
                "Failed to create directory within "
                        + TEMP_DIR_ATTEMPTS
                        + " attempts (tried "
                        + baseName
                        + "0 to "
                        + baseName
                        + (TEMP_DIR_ATTEMPTS - 1)
                        + ')');
    }


    /**
     * This method will convert swagger path List into ballerina @Resource array
     *
     * @param pathMap Swagger @CodegenOperation list to be processed
     * @return Resource array generated from pathMap
     */
    public static Resource[] mapSwaggerPathsToResources(List<CodegenOperation> pathMap) {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow

        List<Resource> resourceList = new ArrayList<Resource>();
        for (CodegenOperation entry : pathMap) {
            String httpMethod = entry.httpMethod;
            String operationId = entry.operationId;
            Resource.ResourceBuilder resourceBuilder = new Resource.ResourceBuilder(
                    new BLangPackage(GlobalScope.getInstance()));
            resourceBuilder.setName(operationId);
            if (entry.hasConsumes) {
                resourceBuilder.addAnnotation(
                        new Annotation(null, new SymbolName("http:Consumes"),
                                entry.consumes.get(0).get("mediaType").toString(), null));
            }
            if (entry.hasProduces) {
                resourceBuilder.addAnnotation(
                        new Annotation(null, new SymbolName("http:Produces"),
                                entry.produces.get(0).get("mediaType"), null));
            }
            if (entry.summary != null) {
                resourceBuilder.addAnnotation(
                        new Annotation(null, new SymbolName("http:Summary"),
                                entry.summary.toString(), null));
            }
            if (entry.notes != null) {
                resourceBuilder.addAnnotation(
                        new Annotation(null, new SymbolName("http:Description"),
                                entry.notes.toString(), null));
            }
            if (entry.path != null && entry.path.length() > 0) {
                resourceBuilder
                        .addAnnotation(new Annotation(null, new SymbolName("http:Path"),
                                entry.path.toString(), null));
            }

            if (entry.httpMethod != null && entry.httpMethod.length() > 0) {
                resourceBuilder.addAnnotation(new Annotation(null, new SymbolName("http:" + httpMethod),
                        "", null));
            }
            //handle parameters
            if (entry.getHasQueryParams()) {
                for (CodegenParameter codegenParameter : entry.queryParams) {
                    //TODO compare and merge if existing parameter edited.
                    String variableName = (String) codegenParameter.
                            vendorExtensions.get(SwaggerBallerinaConstants.VARIABLE_UUID_NAME);
                    if((variableName == null ) || variableName.isEmpty()){
                        variableName = codegenParameter.baseName;
                    }
                    ParameterDef parameterDef = new ParameterDef(
                            new NodeLocation("<unknown>", 0), variableName,
                            new SimpleTypeName(codegenParameter.dataType), new SymbolName("m"),
                            resourceBuilder.buildResource());
                    Annotation annotation = new Annotation(null, new SymbolName("http:QueryParam"),
                            codegenParameter.baseName, null);
                    parameterDef.addAnnotation(annotation);
                    resourceBuilder.addParameter(parameterDef);
                }
            }
            if (entry.getHasPathParams()) {
                for (CodegenParameter codegenParameter : entry.pathParams) {
                    //TODO compare and merge if existing parameter edited.
                    String variableName = (String) codegenParameter.
                            vendorExtensions.get(SwaggerBallerinaConstants.VARIABLE_UUID_NAME);
                    if((variableName == null ) || variableName.isEmpty()){
                        variableName = codegenParameter.baseName;
                    }
                        ParameterDef parameterDef = new ParameterDef(
                            new NodeLocation("<unknown>", 0), variableName,
                            new SimpleTypeName(codegenParameter.dataType), new SymbolName("m"),
                            resourceBuilder.buildResource());
                    Annotation annotation = new Annotation(null, new SymbolName("http:PathParam"),
                            codegenParameter.baseName, null);
                    parameterDef.addAnnotation(annotation);
                    resourceBuilder.addParameter(parameterDef);
                }

            }
            //This resource initiation was required because resource do have both
            //annotation map and array. But there is no way to update array other than
            //constructor method.
            resourceBuilder.setName(entry.nickname);
            resourceBuilder.setName((String) entry.vendorExtensions.
                    get(SwaggerBallerinaConstants.RESOURCE_UUID_NAME));
            //Following code block will generate message input parameter definition for newly created
            //resource as -->	resource TestPost(message m) {
            //This logic can be improved to pass user defined types.
            ParameterDef parameterDef = new ParameterDef(
                    new NodeLocation("<unknown>", 0), "m", new SimpleTypeName("message"),
                    new SymbolName("m"),
                    resourceBuilder.buildResource());
            //Then add created parameter.
            resourceBuilder.addParameter(parameterDef);
            Resource resourceToBeAdd = resourceBuilder.buildResource();
            resourceList.add(resourceToBeAdd);
        }
        return resourceList.toArray(new Resource[resourceList.size()]);
    }

    /**
     * @param pathMap Swagger path map
     * @return resource array
     */
    public static Resource[] mapPathsToResources(Map<String, Path> pathMap) {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        List<Resource> resourceList = new ArrayList<Resource>();
        for (Map.Entry<String, Path> entry : pathMap.entrySet()) {
            Path path = entry.getValue();
            Resource.ResourceBuilder resourceBuilder = new Resource.ResourceBuilder(
                    new BLangPackage(GlobalScope.getInstance()));
            for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                resourceBuilder.addAnnotation(
                        new Annotation(null, new SymbolName(operationEntry.getKey().toString()), null, null));

                resourceBuilder.setSymbolName(new SymbolName(operationEntry.getKey().name()));
            }
            Resource resource = resourceBuilder.buildResource();
            resourceList.add(resource);
        }
        pathMap.forEach((pathString, pathObject) -> {

        });
        return resourceList.toArray(new Resource[resourceList.size()]);
    }

    /**
     * This method will merge swagger definition based to ballerina service.
     *
     * @param ballerinaService ballerina service
     * @param swaggerService swagger service
     * @return merged service
     */
    public static Service mergeBallerinaService(Service ballerinaService, Service swaggerService) {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        //Secondary service annotations are coming from swagger. So we need to merge and update.
        ballerinaService.setAnnotations(
                mergeAnnotations(ballerinaService.getAnnotations(), swaggerService.getAnnotations()));
        List<Resource> resourceList = new ArrayList<Resource>();
        for (Resource resource : swaggerService.getResources()) {
            boolean isExistingResource = false;
            for (Resource originalResource : ballerinaService.getResources()) {
                if (isResourceUUIDMatch(resource, originalResource)) {
                    isExistingResource = true;
                    //Here is a resource math. Do assignments
                    //merge annotations
                    Resource.ResourceBuilder resourceBuilder =
                            new Resource.ResourceBuilder(originalResource.getEnclosingScope());
                    resourceBuilder.setName(originalResource.getName());
                    resourceBuilder.setPkgPath(originalResource.getPackagePath());
                    resourceBuilder.setBody(originalResource.getResourceBody());
                    resourceBuilder.setNodeLocation(originalResource.getNodeLocation());
                    for (Annotation annotation :
                            mergeAnnotations(originalResource.getAnnotations(), resource.getAnnotations())) {
                        resourceBuilder.addAnnotation(annotation);
                    }
                    for (Worker worker : originalResource.getWorkers()) {
                        resourceBuilder.addWorker(worker);
                    }
                    //TODO Add swagger parameters defs and ballerina both. This need to perform as merge.
                    for (ParameterDef parameterDef : resource.getParameterDefs()) {
                        resourceBuilder.addParameter(parameterDef);
                    }
                    for (ParameterDef parameterDef : originalResource.getReturnParameters()) {
                        resourceBuilder.addReturnParameter(parameterDef);
                    }
                    resourceList.add(resourceBuilder.buildResource());
                }
            }
            if (!isExistingResource) {
                resourceList.add(resource);
                //This is completely new resource
            }
        }
        ballerinaService.setResources(mergeResources(resourceList, ballerinaService.getResources()));
        //Following have to do because we cannot assign service name directly when builder pattern used.
        Service.ServiceBuilder serviceBuilder = new Service.ServiceBuilder(ballerinaService.getEnclosingScope());
        serviceBuilder.setName(ballerinaService.getName());
        for (Annotation annotation : ballerinaService.getAnnotations()) {
            serviceBuilder.addAnnotation(annotation);
        }
        for (Resource resource : ballerinaService.getResources()) {
            serviceBuilder.addResource(resource);
        }
        for (VariableDefStmt variableDefStmt : ballerinaService.getVariableDefStmts()) {
            serviceBuilder.addVariableDef(variableDefStmt);
        }
        return serviceBuilder.buildService();
    }

    /**
     * This method will merge annotations from two different services or resources.
     *
     * @param annotations annotations array
     * @param annotationsToMerge annotations array to merge
     * @return merged annotations
     */
    public static Annotation[] mergeAnnotations(Annotation[] annotations, Annotation[] annotationsToMerge) {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        if (annotations == null) {
            return clone(annotationsToMerge);
        } else if (annotationsToMerge == null) {
            return clone(annotations);
        } else {
            //update annotations
            Map<String, Annotation> annotationMap = new ConcurrentHashMap<>();
            for (Annotation originalAnnotation : annotations) {
                //Add original annotations
                if (!originalAnnotation.getName().matches(SwaggerBallerinaConstants.HTTP_VERB_MATCHING_PATTERN)) {
                    annotationMap.put(originalAnnotation.getName(), originalAnnotation);
                }
            }
            for (Annotation annotationToMerge : annotationsToMerge) {
                //merge annotations
                annotationMap.put(annotationToMerge.getName(), annotationToMerge);
            }
            return annotationMap.values().toArray(new Annotation[annotationMap.size()]);
        }
    }

    /**
     * Clone annotations array
     *
     * @param annotations annotation array
     * @return cloned annotation array
     */
    private static Annotation[] clone(Annotation[] annotations) {
        return annotations == null ? null : (Annotation[]) annotations.clone();
    }

    /**
     * Check if 2 resources are having same UUID.
     *
     * @param swaggerResource Swagger resource
     * @param ballerinaResource Ballerina resources
     * @return whether UUIDs for Swagger and Ballerina resources are matching
     */
    public static boolean isResourceUUIDMatch(Resource swaggerResource, Resource ballerinaResource) {
        String path = "/";
        String verb = "";
        for (Annotation annotation : ballerinaResource.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase("http:Path")) {
                path = annotation.getValue();
            } else if (annotation.getName().matches(SwaggerBallerinaConstants.HTTP_VERB_MATCHING_PATTERN)) {
                verb = annotation.getName();
            }
        }
        return swaggerResource.getName().equalsIgnoreCase(generateServiceUUID(path, verb));
    }


    /**
     * This will generate UUID specific to given resource.
     *
     * @param path path
     * @param verb verb
     * @return generated UUID
     */
    public static String generateServiceUUID(String path, String verb) {
        String tmpPath = path;
        tmpPath = tmpPath.replaceAll("\\{", "");
        tmpPath = tmpPath.replaceAll("\\}", "");
        String[] parts = (tmpPath + "/" + verb).split("/");
        StringBuilder builder = new StringBuilder();
        if ("/".equals(tmpPath)) {
            // must be root tmpPath
            builder.append("root");
        }
        for (String part : parts) {
            if (part.length() > 0) {
                if (builder.toString().length() == 0) {
                    part = Character.toLowerCase(part.charAt(0)) + part.substring(1);
                } else {
                    part = capitalize(part);
                }
                builder.append(part);
            }
        }
        return builder.toString().replaceAll("[^a-zA-Z0-9_]", "");
    }

    public static String capitalize(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            char firstChar = str.charAt(0);
            return Character.isTitleCase(firstChar) ? str : (new StringBuilder(strLen)).
                    append(Character.toTitleCase(firstChar)).append(str.substring(1)).toString();
        } else {
            return str;
        }
    }

    /**
     * Remove duplicate resources and merge them.
     *
     * @param resourceList resources list
     * @param resources resources array
     * @return merged resources array without duplicates
     */
    public static Resource[] mergeResources(List<Resource> resourceList, Resource[] resources) {
        for (int i = 0; i < resources.length; i++) {
            Resource resource = resources[i];
            boolean isMatched = false;
            for (Resource resourceFromList : resourceList) {
                if (resourceFromList.getSymbolName().getName().equalsIgnoreCase(
                        resource.getSymbolName().getName())) {
                    isMatched = true;
                    //match means its there in list
                }
            }
            if (!isMatched) {
                //If this is complete new resource then add it to another list.
                resourceList.add(resource);
            }

        }
        return resourceList.toArray(new Resource[resourceList.size()]);
    }

    /**
     * This method will convert ballerina definition to swagger string. Since swagger is subset of ballerina definition
     * we can implement converter logic without data loss.
     *
     * @param ballerinaDefinition String ballerina config to be processed as ballerina service definition
     * @return swagger data model generated from ballerina definition
     * @throws IOException when input process error occur.
     */
    public static String generateSwaggerDataModel(String ballerinaDefinition) throws IOException {
        //TODO improve code to avoid additional object creation.
        org.ballerinalang.model.Service[] services = SwaggerConverterUtils.
                getServicesFromBallerinaDefinition(ballerinaDefinition);
        String swaggerDefinition = "";
        if (services.length > 0) {
            //TODO this need to improve iterate through multiple services and generate single swagger file.
            SwaggerServiceMapper swaggerServiceMapper = new SwaggerServiceMapper();
            //TODO mapper type need to set according to expected type.
            //swaggerServiceMapper.setObjectMapper(io.swagger.util.Yaml.mapper());
            swaggerDefinition = swaggerServiceMapper.
                    generateSwaggerString(swaggerServiceMapper.convertServiceToSwagger(services[0]));
        }
        return swaggerDefinition;
    }

    /**
     * This method will generate ballerina string from swagger definition. Since ballerina service definition is super
     * set of swagger definition we will take both swagger and ballerina definition and merge swagger changes to
     * ballerina definition selectively to prevent data loss
     *
     * @param swaggerDefinition  swagger definition to be processed as swagger
     * @param ballerinaDefinition  ballerina definition to be process as ballerina definition
     * @return String representation of converted ballerina source
     * @throws IOException when error occur while processing input swagger and ballerina definitions.
     */
    public static String generateBallerinaDataModel(String swaggerDefinition, String ballerinaDefinition)
            throws IOException {
        BallerinaFile ballerinaFile = SwaggerConverterUtils.getBFileFromBallerinaDefinition(ballerinaDefinition);
        //Always assume we have only one resource in bfile.
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        org.ballerinalang.model.Service swaggerService = SwaggerConverterUtils.
                getServiceFromSwaggerDefinition(swaggerDefinition);
        org.ballerinalang.model.Service ballerinaService = SwaggerConverterUtils.
                getServicesFromBallerinaDefinition(ballerinaDefinition)[0];
        String serviceName = swaggerService.getSymbolName().getName();
        /*for (org.ballerinalang.model.Service currentService : ballerinaFile.getServices()) {
            if (currentService.getSymbolName().getName().equalsIgnoreCase(serviceName)) {
                ballerinaService = currentService;
            }
        }*/
        //Compare ballerina service and swagger service and then substitute values. Then we should get ballerina
        //JSON representation and send back to client.
        //for the moment we directly add swagger service to ballerina service.
        //Replace first service of the ballerina file.
        for (int i = 0; i < ballerinaFile.getCompilationUnits().length; i++) {
            CompilationUnit compilationUnit = ballerinaFile.getCompilationUnits()[i];
            if (compilationUnit instanceof org.ballerinalang.model.Service) {
                ballerinaFile.getCompilationUnits()[i] = SwaggerConverterUtils.
                        mergeBallerinaService(ballerinaService, swaggerService);
                break;
            }

        }
        //Now we have to convert ballerina file to JSON object model composer require.
        JsonObject response = new JsonObject();
        BLangJSONModelBuilder jsonModelBuilder = new BLangJSONModelBuilder(response);
        ballerinaFile.accept(jsonModelBuilder);
        return response.toString();
    }
}
