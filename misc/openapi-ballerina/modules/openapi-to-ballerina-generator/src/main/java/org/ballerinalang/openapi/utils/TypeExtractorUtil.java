package org.ballerinalang.openapi.utils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.typemodel.BallerinaIOpennApiRequestBody;
import org.ballerinalang.openapi.typemodel.BallerinaOpenApiComponent;
import org.ballerinalang.openapi.typemodel.BallerinaOpenApiOperation;
import org.ballerinalang.openapi.typemodel.BallerinaOpenApiParameter;
import org.ballerinalang.openapi.typemodel.BallerinaOpenApiPath;
import org.ballerinalang.openapi.typemodel.BallerinaOpenApiSchema;
import org.ballerinalang.openapi.typemodel.BallerinaOpenApiType;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.ballerinalang.openapi.OpenApiMesseges.BAL_KEYWORDS;

/**
 * This class will extract an intermediate context object from a give OpenApi Definition object, which
 * will retrieve a compatible context object to generate the ballerina code segments.
 */
public class TypeExtractorUtil {
    private static final PrintStream outStream = System.err;

    /**
     * This method will extract Ballerina compatible Data Object containing compatible types from
     * the OpenApi Object.
     *
     * @param apiDef - OpenApi definition
     * @return - Ballerina compatible type object
     * @throws BallerinaOpenApiException - throws exception if extraction fails.
     */
    public static BallerinaOpenApiType extractOpenApiObject(OpenAPI apiDef) throws BallerinaOpenApiException {
        BallerinaOpenApiType typeDef = new BallerinaOpenApiType();

        if (apiDef.getPaths() != null) {
            typeDef.setPathList(extractOpenApiPaths(apiDef.getPaths()));
        }

        if (apiDef.getComponents() != null) {
            BallerinaOpenApiComponent component = new BallerinaOpenApiComponent();
            if (apiDef.getComponents().getSchemas() != null) {
                component.setSchemaList(extractComponentSchema(apiDef.getComponents().getSchemas()));
            }
            typeDef.setComponent(component);
        }

        return  typeDef;
    }

    /**
     * This method will iterate OpenApi paths list to extract relevant Ballerina compatible types.
     *
     * @param defPaths - OpenApi Paths list
     * @return - List of Ballerina compatible path type
     * @throws BallerinaOpenApiException - throws exception if extraction fails.
     */
    private static List<BallerinaOpenApiPath> extractOpenApiPaths(Paths defPaths) throws BallerinaOpenApiException {
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

    /**
     * This method will iterate a Operations map to extract Ballerina compatible types.
     *
     * @param operationMap - OpenApi operations map
     * @param pathName - Relative OpenApi Path name
     * @return - List of Ballerina compatible operation types
     * @throws BallerinaOpenApiException - throws exception if extraction fails.
     */
    private static List<BallerinaOpenApiOperation> extractOpenApiOperations(Map<PathItem.HttpMethod,
            Operation> operationMap, String pathName) throws BallerinaOpenApiException {
        final Iterator<Map.Entry<PathItem.HttpMethod, Operation>> opIterator = operationMap.entrySet().iterator();
        List<BallerinaOpenApiOperation> typeOpList = new ArrayList<>();

        while (opIterator.hasNext()) {
            final Map.Entry<PathItem.HttpMethod, Operation> nextOp = opIterator.next();
            final PathItem.HttpMethod opMethod = nextOp.getKey();
            final Operation opObject = nextOp.getValue();
            BallerinaOpenApiOperation operation = new BallerinaOpenApiOperation();

            operation.setOpMethod(opMethod.toString());

            if (opObject.getOperationId() == null) {
                String resName = "resource_" + nextOp.getKey().toString().toLowerCase(Locale.ENGLISH)
                        + pathName.replaceAll("/", "_")
                        .replaceAll("[{}]", "");
                operation.setOpName(resName);
                outStream.println("warning : `" + resName + "` is used as the resource name since the " +
                        "operation id is missing for " + pathName + " " + nextOp.getKey());
            } else {
                operation.setOpName(escapeIdentifier(
                        opObject.getOperationId().replace(" ", "_")));
            }

            if (opObject.getParameters() != null) {
                operation.setParameterList(extractOpenApiParameters(opObject.getParameters()));
            }

            if (opObject.getRequestBody() != null) {
                operation.setRequestBody(extractOpenApiRequestBody(opObject.getRequestBody()));
            }

            typeOpList.add(operation);
        }
        return typeOpList;
    }

    /**
     * This method will iterate a list of OpenApi parameters to extract Ballerina compatible parameter types.
     *
     * @param parameters - OpenApi parameters list
     * @return - List of Ballerina compatible Parameter types
     * @throws BallerinaOpenApiException - throws exception if extraction fails.
     */
    private static List<BallerinaOpenApiParameter> extractOpenApiParameters(List<Parameter> parameters)
            throws BallerinaOpenApiException {
        final Iterator<Parameter> paramIterator = parameters.iterator();
        List<BallerinaOpenApiParameter> paramList = new ArrayList<>();

        while (paramIterator.hasNext()) {
            final Parameter nextParam = paramIterator.next();
            BallerinaOpenApiParameter parameter = new BallerinaOpenApiParameter();

            if (nextParam.getName() != null) {
                parameter.setParamName(escapeIdentifier(nextParam.getName()));
            }

            if (nextParam.getIn() != null) {
                parameter.setPathParam(nextParam.getIn().equals("path"));
            }

            if (nextParam.get$ref() != null) {
                parameter.setRefType(extractReferenceType(nextParam.get$ref()));
            }

            if (nextParam.getSchema() != null) {
                BallerinaOpenApiSchema typeSchema = new BallerinaOpenApiSchema();
                extractOpenApiSchema(nextParam.getSchema(), typeSchema, true, null);
                parameter.setParamType(typeSchema);
            }

            paramList.add(parameter);
        }

        return paramList;
     };

    /**
     * This method will extract OpenApi Request body to Ballerina compatible type.
     *
     * @param requestBody - OpenApi request body
     * @return - Ballerina compatible request body type
     * @throws BallerinaOpenApiException - throws exception if extraction fails.
     */
    private static BallerinaIOpennApiRequestBody extractOpenApiRequestBody(RequestBody requestBody)
            throws BallerinaOpenApiException {
        BallerinaIOpennApiRequestBody requestType = new BallerinaIOpennApiRequestBody();

        if (requestBody.get$ref() != null) {
            requestType.setRefType(extractReferenceType(requestBody.get$ref()));
        } else if (requestBody.getContent() != null) {
            BallerinaOpenApiSchema requestSchema = new BallerinaOpenApiSchema();
            final MediaType request = requestBody.getContent().entrySet().iterator().next().getValue();

            if (request.getSchema() != null) {
                extractOpenApiSchema(request.getSchema() , requestSchema, true, null);
            }

            requestType.setContentType(requestSchema);
        }

        return requestType;
    }

    /**
     * This method will extract OpenApi components schema to extract ballerina compatible types.
     *
     * @param schemas - OpenApi Schema list
     * @return - List of Ballerina compatible schema types
     * @throws BallerinaOpenApiException - throws exception if extraction fails.
     */
    private static List<BallerinaOpenApiSchema> extractComponentSchema(Map<String, Schema> schemas)
            throws BallerinaOpenApiException {
        List<BallerinaOpenApiSchema> schemaList = new ArrayList<>();
        final Iterator<Map.Entry<String, Schema>> schemaIterator = schemas.entrySet().iterator();

        while (schemaIterator.hasNext()) {
            final Map.Entry<String, Schema> schemaEntry = schemaIterator.next();
            final String schemaName = schemaEntry.getKey();
            final Schema schemaObject = schemaEntry.getValue();
            BallerinaOpenApiSchema typeSchema = new BallerinaOpenApiSchema();

            typeSchema.setSchemaName(escapeIdentifier(schemaName));
            extractOpenApiSchema(schemaObject, typeSchema, false, null);

            schemaList.add(typeSchema);
        }

        return schemaList;
    }

    /**
     * Common method to extract OpenApi Schema type objects in to Ballerina type compatible schema objects.
     *
     * @param schema - OpenApi Schema
     * @param typeSchema - Ballerina Compatible type schema
     * @param isInline - Parameter to identify if the schema extracted is an inline schema or not
     * @param composedSchemaType - Parameter to identify if the current schema is sub set of a composed schema
     * @throws BallerinaOpenApiException - throws exception if extraction fails.
     */
    private static void extractOpenApiSchema(Schema schema, BallerinaOpenApiSchema typeSchema, Boolean isInline,
                                             String composedSchemaType) throws BallerinaOpenApiException {
        typeSchema.setInline(isInline);

        //Extracts Composed schema types from OpenApi Schema.
        //Only `All_OF` and `ONE_OF` types are extracted.
        if (schema instanceof ComposedSchema) {
            ComposedSchema composedSchema = (ComposedSchema) schema;
            typeSchema.setComposedSchema(true);

            if (composedSchema.getOneOf() != null) {
                final Iterator<Schema> oneOfList = composedSchema.getOneOf().iterator();
                typeSchema.setOneOf(true);
                while (oneOfList.hasNext()) {
                    final Schema nextOneOf = oneOfList.next();
                    extractOpenApiSchema(nextOneOf, typeSchema, isInline, "ONE_OF");
                }
            }

            if (composedSchema.getAllOf() != null) {
                final Iterator<Schema> allOfIterator = composedSchema.getAllOf().iterator();
                typeSchema.setAllOf(true);
                while (allOfIterator.hasNext()) {
                    final Schema allOfNext = allOfIterator.next();
                    extractOpenApiSchema(allOfNext, typeSchema, isInline, "ALL_OF");
                }
            }
        }

        if (schema.getType() != null || schema.getProperties() != null) {
            if (schema.getType() != null && (schema.getType().equals("integer") || schema.getType().equals("number"))) {
                setSchemaType(typeSchema, "int");
            } else if (schema.getType() != null && schema.getType().equals("string")) {
                setSchemaType(typeSchema, "string");
            } else if (schema.getType() != null && schema.getType().equals("boolean")) {
                setSchemaType(typeSchema, "boolean");
            } else if (schema.getType() != null && schema.getType().equals("array")) {
                if (schema instanceof ArraySchema) {
                    final ArraySchema arraySchema = (ArraySchema) schema;
                    typeSchema.setArray(true);
                    if (arraySchema.getItems() != null) {
                        BallerinaOpenApiSchema arrayItems = new BallerinaOpenApiSchema();
                        extractOpenApiSchema(arraySchema.getItems(), arrayItems, isInline, composedSchemaType);
                        typeSchema.setItemsSchema(arrayItems);
                    }
                }
            } else if ((schema.getType() != null && schema.getType().equals("object")) ||
                    schema.getProperties() != null) {
                if (schema.getProperties() != null) {
                    final Iterator<Map.Entry<String, Schema>> propertyIterator = schema.getProperties()
                            .entrySet().iterator();
                    List<BallerinaOpenApiSchema> propertyList = new ArrayList<>();

                    while (propertyIterator.hasNext()) {
                        final Map.Entry<String, Schema> nextProp = propertyIterator.next();
                        final String propName = nextProp.getKey();
                        final Schema propValue = nextProp.getValue();
                        BallerinaOpenApiSchema propertySchema = new BallerinaOpenApiSchema();

                        propertySchema.setSchemaName(escapeIdentifier(propName));
                        extractOpenApiSchema(propValue, propertySchema, isInline, composedSchemaType);

                        propertyList.add(propertySchema);
                    }

                    typeSchema.setHasChildElements(true);
                    typeSchema.setSchemaProperties(propertyList);
                }
            } else {
                outStream.println("Encountered an unsupported type. Type `any` would be used for the field.");
                setSchemaType(typeSchema, "any");
            }
        } else if (schema.get$ref() != null) {
            if (composedSchemaType != null && composedSchemaType.equals("ALL_OF")) {
                setComposedSchemaType(typeSchema, extractReferenceType(schema.get$ref()));
            } else {
                setSchemaType(typeSchema, extractReferenceType(schema.get$ref()));
            }

        } else {
            //This contains a fallback to Ballerina common type `any` if the OpenApi specification type is not defined
            // or not compatible with any of the current Ballerina types.
            outStream.println("Encountered an unsupported type. Type `any` would be used for the field.");
            setSchemaType(typeSchema, "any");
        }
    }

    /**
     * Util method to add a type to the current OpenApi composed schema type list.
     *
     * @param schema - Composed Schema object
     * @param schemaType - schema type
     */
    private static void setComposedSchemaType(BallerinaOpenApiSchema schema, String schemaType) {
        if (schema.getComposedSchemaTypes() == null) {
            List<String> typeList = new ArrayList<>();
            typeList.add(schemaType);
            schema.setComposedSchemaTypes(typeList);
        } else {
            schema.getComposedSchemaTypes().add(schemaType);
        }
    }

    /**
     * Util method to add a type to the current OpenApi schema type list.
     *
     * @param schema - Schema object
     * @param schemaType - schema type
     */
    private static void setSchemaType(BallerinaOpenApiSchema schema, String schemaType) {
        if (schema.getSchemaType() == null) {
            List<String> typeList = new ArrayList<>();
            typeList.add(schemaType);
            schema.setSchemaType(typeList);
        } else {
            schema.getSchemaType().add(schemaType);
        }
    }

    /**
     * This method will escape special characters used in method names and identifiers.
     *
     * @param identifier - identifier or method name
     * @return - escaped string
     */
    public static String escapeIdentifier(String identifier) {
        if (!identifier.matches("[a-zA-Z]+") || BAL_KEYWORDS.stream().anyMatch(identifier::equals)) {
            identifier = identifier.replaceAll("([\\\\?!<>*\\-=^+()_{}|.$])", "\\\\$1");
            identifier = "'" + identifier;
        }
        return identifier;
    }


    /**
     * This method will extract reference type by splitting the reference string.
     *
     * @param referenceVariable - Reference String
     * @return Reference variable name
     * @throws BallerinaOpenApiException - Throws an exception if the reference string is incompatible.
     *                                     Note : Current implementation will not support external links a references.
     */
    public static String extractReferenceType(String referenceVariable) throws BallerinaOpenApiException {
        if (referenceVariable.startsWith("#") && referenceVariable.contains("/")) {
            String[] refArray = referenceVariable.split("/");
            return escapeIdentifier(refArray[refArray.length - 1]);
        } else {
            throw new BallerinaOpenApiException("Invalid reference value : " + referenceVariable
                    + "\nBallerina only supports local reference values.");
        }
    }
}
