package org.ballerinalang.openapi.utils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.openapi.typemodel.OpenApiComponentType;
import org.ballerinalang.openapi.typemodel.OpenApiOperationType;
import org.ballerinalang.openapi.typemodel.OpenApiParameterType;
import org.ballerinalang.openapi.typemodel.OpenApiPathType;
import org.ballerinalang.openapi.typemodel.OpenApiPropertyType;
import org.ballerinalang.openapi.typemodel.OpenApiRequestBodyType;
import org.ballerinalang.openapi.typemodel.OpenApiResponseType;
import org.ballerinalang.openapi.typemodel.OpenApiSchemaType;
import org.ballerinalang.openapi.typemodel.OpenApiType;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.ballerinalang.openapi.OpenApiMesseges.BAL_KEYWORDS;

/**
 * This Class will handle the type matching of a given OpenApi Object.
 */
public class TypeMatchingUtil {

    private static final PrintStream outStream = System.err;

    public static OpenApiType traveseOpenApiTypes(OpenAPI api) {
        Iterator<Map.Entry<String, PathItem>> pathItemIterator = api.getPaths().entrySet().iterator();
        OpenApiType apiTypes = new OpenApiType();
        OpenApiComponentType componentType = new OpenApiComponentType();
        List<OpenApiPathType> pathList = new ArrayList<>();

        //Traverse Path Items to retrieve types inside paths
        while (pathItemIterator.hasNext()) {
            Map.Entry<String, PathItem> next = pathItemIterator.next();

            PathItem pathObj = next.getValue();

            if (!pathObj.readOperations().isEmpty()) {
                OpenApiPathType path = new OpenApiPathType();

                path.setPathName(next.getKey());
                path.setOperations(traveseOperations(pathObj.readOperationsMap(), next.getKey()));

                pathList.add(path);
            }
        }

        if (api.getComponents() != null) {
            final Map<String, Schema> schemas = api.getComponents().getSchemas();

            if (schemas != null) {
                componentType.setSchemaTypes(traverseComponentSchema(schemas));
            }
        }

        apiTypes.setPaths(pathList);
        apiTypes.setComponent(componentType);

        return apiTypes;

    }

    public static List<OpenApiSchemaType> traverseComponentSchema(Map<String, Schema> schemas) {
        List<OpenApiSchemaType> schemaTypes = new ArrayList<>();

        for (Map.Entry<String, Schema> schema : schemas.entrySet()) {
            OpenApiSchemaType schemaType = new OpenApiSchemaType();
            final Schema schemaValue = schema.getValue();
            final String schemaName = schema.getKey();

            schemaType.setSchemaName(delimeterizeUnescapedIdentifires(StringUtils.capitalize(schemaName), true));

            if (schemaValue instanceof ObjectSchema) {
                if (schemaValue.getProperties() != null) {
                    Map<String, Schema> properties = schemaValue.getProperties();
                    schemaType.setSchemaProperties(getSchemaPropertyTypes(properties));
                }
            }

            if (schemaValue instanceof ComposedSchema) {
                getTypesFromComposedSchema((ComposedSchema) schemaValue, schemaType);
            }

            if (schemaValue != null) {
                getTypesFromSchema(schemaValue, schemaType);
            }

            if (schemaValue != null && schemaValue.getType() != null) {
                schemaType.setSchemaType(schemaValue.getType());
            }

            schemaTypes.add(schemaType);

        }

        return schemaTypes;
    }

    public static void getTypesFromComposedSchema(ComposedSchema schema, OpenApiSchemaType schemaType) {

        if (schema.getAllOf() != null) {
            for (Schema allOfSchema : schema.getAllOf()) {
                if (allOfSchema.getProperties() != null) {
                    schemaType.setSchemaProperties(getSchemaPropertyTypes(allOfSchema.getProperties()));
                }
            }
        }

        if (schema.getAnyOf() != null) {
            for (Schema allOfSchema : schema.getAnyOf()) {
                if (allOfSchema.getProperties() != null) {
                    schemaType.setSchemaProperties(getSchemaPropertyTypes(allOfSchema.getProperties()));
                }
            }
        }

        if (schema.getOneOf() != null) {
            for (Schema allOfSchema : schema.getOneOf()) {
                if (allOfSchema.getProperties() != null) {
                    schemaType.setSchemaProperties(getSchemaPropertyTypes(allOfSchema.getProperties()));
                }
            }
        }

    }

    public static List<OpenApiPropertyType> getSchemaPropertyTypes(Map<String, Schema> property) {

        List<OpenApiPropertyType> propertyTypes = new ArrayList<>();

        for (Object propertyObject : property.entrySet()) {
            OpenApiPropertyType propertyType = new OpenApiPropertyType();

            String propName = (String) ((Map.Entry) propertyObject).getKey();

            Schema schema = (Schema) ((Map.Entry) propertyObject).getValue();
            String type = schema.getType();

            if (type != null) {
                switch (type) {
                    case "string":
                        propertyType.setPropertyType("string");
                        break;
                    case "array":
                        if (schema instanceof  ArraySchema) {
                            ArraySchema arrayObj = (ArraySchema) schema;
                            if (arrayObj.getItems() != null) {
                                final Schema<?> items = arrayObj.getItems();
                                propertyType.setIsArray(true);
                                if (items.getType() != null) {
                                    propertyType.setPropertyType(
                                            delimeterizeUnescapedIdentifires(items.getType(), false));
                                } else if (items.get$ref() != null) {
                                    final String[] referenceType = items.get$ref().split("/");
                                    propertyType.setPropertyType(
                                            delimeterizeUnescapedIdentifires(
                                                    referenceType[referenceType.length - 1], false));
                                }
                            }
                        }
                        break;
                    case "integer":
                    case "number":
                        propertyType.setPropertyType("int");
                        break;
                    case "boolean":
                        propertyType.setPropertyType("boolean");
                        break;
                    case "object":
                        Schema schemaObj = (Schema) schema;
                        if (schemaObj.get$ref() != null && schemaObj.get$ref().startsWith("#")) {
                            String[] ref = schemaObj.get$ref().split("/");
                            propertyType.setPropertyType(
                                    delimeterizeUnescapedIdentifires(
                                            StringUtils.capitalize(ref[ref.length - 1]), false));
                        } else if (schemaObj.getAdditionalProperties() != null
                                && schemaObj.getAdditionalProperties() instanceof  Schema) {
                            Schema addSchema = (Schema) schemaObj.getAdditionalProperties();

                            if (addSchema.get$ref() != null) {
                                String[] ref = addSchema.get$ref().split("/");
                                propertyType.setPropertyType(
                                        delimeterizeUnescapedIdentifires(
                                                StringUtils.capitalize(ref[ref.length - 1]), false));
                            } else if (addSchema.getType() != null) {
                                propertyType.setPropertyType(delimeterizeUnescapedIdentifires(
                                        addSchema.getType(), false));
                            }
                        } else if (schemaObj.getType() == null || schemaObj.getType().equals("object")) {
                            propertyType.setPropertyType("any");
                        }
                        break;
                    default:
                        outStream.println("Unsupported schema type " + type);
                        break;
                }
            } else if (schema.get$ref() != null) {
                String[] ref = schema.get$ref().split("/");
                propertyType.setPropertyType(
                        delimeterizeUnescapedIdentifires(StringUtils.capitalize(ref[ref.length - 1]), false));
            }

            propertyType.setPropertyName(delimeterizeUnescapedIdentifires(propName, true));
            propertyTypes.add(propertyType);
        }

        return propertyTypes;
    }

    public static void getTypesFromSchema(Schema schema, OpenApiSchemaType schemaType) {


        if (schema.getType() != null) {

            switch (schema.getType()) {
                case "integer":
                    schemaType.setSchemaType("int");
                    break;
                case "string":
                    schemaType.setSchemaType("string");
                    break;
                case "array":
                    schemaType.setIsArray(true);
                    if (schema instanceof ArraySchema) {
                        final Schema<?> arrayItems = ((ArraySchema) schema).getItems();
                        if (arrayItems.getType() != null) {
                            schemaType.setItemType(
                                    delimeterizeUnescapedIdentifires(arrayItems.getType(), false));
                            schemaType.setItemName(arrayItems.getType().toLowerCase(Locale.ENGLISH));
                        } else if (arrayItems.get$ref() != null) {
                            final String[] ref = arrayItems.get$ref().split("/");
                            schemaType.setItemType(delimeterizeUnescapedIdentifires(
                                    StringUtils.capitalize(ref[ref.length - 1]), false));
                            schemaType.setItemName(ref[ref.length - 1].toLowerCase(Locale.ENGLISH));
                        }
                    }
                    break;
                case "object":
                    if (schema.getProperties() != null) {
                        schemaType.setInline(true);
                        schemaType.setSchemaProperties(getSchemaPropertyTypes(schema.getProperties()));
                    }
                    break;
                default:
                    schemaType.setSchemaType(delimeterizeUnescapedIdentifires(schema.getType(), false));
                    break;
            }
        } else if (schema.get$ref() != null) {
            String[] refArray = schema.get$ref().split("/");
            schemaType.setItemType(delimeterizeUnescapedIdentifires(
                    StringUtils.capitalize(refArray[refArray.length - 1]), false));
            schemaType.setUnescapedItemName(refArray[refArray.length - 1].toLowerCase(Locale.ENGLISH));
            schemaType.setItemName(delimeterizeUnescapedIdentifires(
                    refArray[refArray.length - 1].toLowerCase(Locale.ENGLISH), true));
        }

        if (schema.getRequired() != null) {
            schemaType.setRequired(schema.getRequired());
        }
    }


    public static List<OpenApiOperationType> traveseOperations(Map<PathItem.HttpMethod, Operation> operations,
                                                               String path) {
        Iterator<Map.Entry<PathItem.HttpMethod, Operation>> operationIterator = operations.entrySet().iterator();
        List<OpenApiOperationType> operationTypes = new ArrayList<>();

        while (operationIterator.hasNext()) {
            Map.Entry<PathItem.HttpMethod, Operation> nextOp = operationIterator.next();
            OpenApiOperationType operation = new OpenApiOperationType();
            operation.setOperationType(nextOp.getKey().toString());

            if (nextOp.getValue().getOperationId() != null) {
                operation.setOperationName(delimeterizeUnescapedIdentifires(nextOp.getValue().getOperationId()
                        .replace(" ", "_"), false));
            } else {
                String resName = "resource_" + nextOp.getKey().toString().toLowerCase(Locale.ENGLISH)
                        + path.replaceAll("/", "_")
                        .replaceAll("[{}]", "");
                operation.setOperationName(delimeterizeUnescapedIdentifires(resName, false));
                outStream.println("warning : `" + resName + "` is used as the resource name since the " +
                        "operation id is missing for " + path + " " + nextOp.getKey());
            }

            if (nextOp.getValue().getParameters() != null && !nextOp.getValue().getParameters().isEmpty()) {
                operation.setParameters(traverseParameters(nextOp.getValue().getParameters()));
            }

            if (nextOp.getValue().getResponses() != null && !nextOp.getValue().getResponses().isEmpty()) {
                operation.setResponses(traverseResponses(nextOp.getValue().getResponses()));
            }

            if (nextOp.getValue().getRequestBody() != null) {
                operation.setRequestBodies(traverseRequestBodies(nextOp.getValue().getRequestBody()));

            }

            operationTypes.add(operation);
        }

        return operationTypes;
    }

    public static List<OpenApiResponseType> traverseResponses(ApiResponses responses) {
        Iterator<Map.Entry<String, ApiResponse>> responseIterator = responses.entrySet().iterator();
        List<OpenApiResponseType> apiResponses = new ArrayList<>();

        while (responseIterator.hasNext()) {
            final Map.Entry<String, ApiResponse> nextResp = responseIterator.next();
            OpenApiResponseType response = new OpenApiResponseType();

            response.setResponseCode(nextResp.getKey());

            if (nextResp.getValue().getContent() != null) {

                final Iterator<Map.Entry<String, MediaType>> contentIterator = nextResp.getValue()
                        .getContent().entrySet().iterator();

                while (contentIterator.hasNext()) {
                    final Map.Entry<String, MediaType> content = contentIterator.next();
                    final Schema responseSchema = content.getValue().getSchema();

                    if (responseSchema == null) {
                        break;
                    }

                    if (responseSchema.get$ref() == null) {
                        response.setResponseType(responseSchema.getType());
                    } else {
                        response.setResponseType(responseSchema.get$ref());
                    }
                }
            }

            apiResponses.add(response);
        }

        return apiResponses;
    }

    public static List<OpenApiParameterType> traverseParameters(List<Parameter> parameters) {
        Iterator<Parameter> parameterIterator = parameters.iterator();
        List<OpenApiParameterType> parameterList = new ArrayList<>();

        while (parameterIterator.hasNext()) {
            Parameter nextParam = parameterIterator.next();
            OpenApiParameterType parameter = new OpenApiParameterType();
            OpenApiSchemaType parameterSchema = new OpenApiSchemaType();

            if (nextParam.getIn().equals("path")) {
                parameter.setPathParam(true);
            } else if (nextParam.getIn().equals("query")) {
                parameter.setQueryParam(true);
            }

            if (!parameterIterator.hasNext()) {
                parameter.setLastParameter(true);
            }

            getTypesFromSchema(nextParam.getSchema(), parameterSchema);

            parameter.setParamName(delimeterizeUnescapedIdentifires(nextParam.getName(), true));
            parameter.setParamType(parameterSchema);
            parameterList.add(parameter);
        }

        return parameterList;
    }

    public static OpenApiRequestBodyType traverseRequestBodies(RequestBody requestBody) {

        OpenApiRequestBodyType requestBodyType = new OpenApiRequestBodyType();
        List<OpenApiSchemaType> contentList = new ArrayList<>();

        if (requestBody.getRequired() != null) {
            requestBodyType.setRequired(requestBody.getRequired());
        }

        if (requestBody.getContent() != null) {
            final Map.Entry<String, MediaType> entry = requestBody.getContent().entrySet().iterator().next();
            OpenApiSchemaType schemaType = new OpenApiSchemaType();
            final MediaType entryValue = entry.getValue();
            schemaType.setSchemaType(delimeterizeUnescapedIdentifires(entry.getKey(), false));

            if (entryValue.getSchema() != null) {
                getTypesFromSchema(entryValue.getSchema(), schemaType);
            }

            contentList.add(schemaType);
            requestBodyType.setContentList(contentList);
        }

        return requestBodyType;
    }

    /**
     * This will escape special characters in ballerina identifiers.
     *
     * @param identifier - identifier string
     * @param isVar - is a variable name or type
     * @return escaped string
     */
    public static String delimeterizeUnescapedIdentifires(String identifier, boolean isVar) {
        if (identifier.matches("^[a-zA-Z0-9_]*$")
                && !BAL_KEYWORDS.stream().anyMatch(identifier::equals) && !isVar) {
            return identifier;
        }
        if (!identifier.matches("[a-zA-Z]+") || BAL_KEYWORDS.stream().anyMatch(identifier::equals)) {
            if ((isVar && BAL_KEYWORDS.stream().anyMatch(identifier::equals))
                    || !BAL_KEYWORDS.stream().anyMatch(identifier::equals)) {
                identifier = identifier.replaceAll("([\\\\?!<>*\\-=^+(){}|.$])", "\\\\$1");
                identifier = "'" + identifier;
            }
        }
        return identifier;
    }

}
