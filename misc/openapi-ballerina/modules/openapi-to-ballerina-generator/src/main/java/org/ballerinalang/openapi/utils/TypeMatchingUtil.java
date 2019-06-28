package org.ballerinalang.openapi.utils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.callbacks.Callback;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.ballerinalang.openapi.typemodel.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * This Class will handle the type matching of a given OpenApi Object
 */
public class TypeMatchingUtil {

    public static OpenApiType TraveseOpenApiTypes(OpenAPI api) {
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
                path.setOperations(TraveseOperations(pathObj.readOperationsMap()));

                pathList.add(path);
            }
        }

        final Map<String, Schema> schemas = api.getComponents().getSchemas();
        final Map<String, Parameter> parameters = api.getComponents().getParameters();
        final Map<String, ApiResponse> responses = api.getComponents().getResponses();
        final Map<String, Callback> callbacks = api.getComponents().getCallbacks();
        final Map<String, Example> examples = api.getComponents().getExamples();
        final Map<String, Object> extensions = api.getComponents().getExtensions();
        final Map<String, Header> headers = api.getComponents().getHeaders();
        final Map<String, Link> links = api.getComponents().getLinks();
        final Map<String, RequestBody> requestBodies = api.getComponents().getRequestBodies();
        final Map<String, SecurityScheme> securitySchemes = api.getComponents().getSecuritySchemes();

        if (schemas != null) {
            componentType.setSchemaTypes(TraverseComponentSchema(schemas));
        }

        apiTypes.setPaths(pathList);
        apiTypes.setComponent(componentType);

        return apiTypes;

    }

    public static List<OpenApiSchemaType> TraverseComponentSchema(Map<String, Schema> schemas) {
        final Iterator<Map.Entry<String, Schema>> schemaIterator = schemas.entrySet().iterator();
        List<OpenApiSchemaType> schemaTypes = new ArrayList<>();

        while (schemaIterator.hasNext()) {
            final Map.Entry<String, Schema> schemaEntry = schemaIterator.next();
            OpenApiSchemaType schemaType = new OpenApiSchemaType();

            schemaType.setSchemaName(schemaEntry.getKey());

            final Schema schemaEntryValue = schemaEntry.getValue();

            if (schemaEntryValue.getType() != null){
                schemaType.setSchemaType(schemaEntryValue.getType());
            }

            if (schemaEntryValue instanceof ComposedSchema) {
                GetTypesFromComposedSchema((ComposedSchema) schemaEntryValue, schemaType);
            } else {
                GetTypesFromSchema(schemaEntryValue, schemaType);
            }

            schemaTypes.add(schemaType);
        }

        return schemaTypes;
    }

    public static void GetTypesFromComposedSchema(ComposedSchema schema, OpenApiSchemaType schemaType) {

        if (schema.getAllOf() != null) {
            for (Schema allOfSchema: schema.getAllOf()) {
                if (allOfSchema.getProperties() != null) {
                    schemaType.setSchemaProperties(GetSchemaPropertyTypes(allOfSchema.getProperties()));
                }
            }
        }

        if (schema.getAnyOf() != null) {
            for (Schema allOfSchema: schema.getAnyOf()) {
                if (allOfSchema.getProperties() != null) {
                    schemaType.setSchemaProperties(GetSchemaPropertyTypes(allOfSchema.getProperties()));
                }
            }
        }

        if (schema.getOneOf() != null) {
            for (Schema allOfSchema: schema.getOneOf()) {
                if (allOfSchema.getProperties() != null) {
                    schemaType.setSchemaProperties(GetSchemaPropertyTypes(allOfSchema.getProperties()));
                }
            }
        }

    }

    public static List<OpenApiPropertyType> GetSchemaPropertyTypes(Map<String, Schema> property) {

        SchemaParser schemaParser = new SchemaParser();
        Class schemaParserClass = schemaParser.getClass();
        List<OpenApiPropertyType> propertyTypes = new ArrayList<>();

        for (Object propertyObject: property.entrySet()) {
            OpenApiPropertyType propertyType = new OpenApiPropertyType();

            String propName = (String) ((Map.Entry) propertyObject).getKey();
            Class schemaTypeClass = ((Map.Entry) propertyObject).getValue().getClass();
            String schemaTypeClassName = schemaTypeClass.getName();
            String[] schemaMethodName = schemaTypeClassName.split("\\.");
            try {
                schemaParserClass.getDeclaredMethod("parse" + schemaMethodName[schemaMethodName.length-1],
                        Object.class, OpenApiPropertyType.class).invoke(schemaParserClass.newInstance(),
                        ((Map.Entry) propertyObject).getValue(), propertyType);
            } catch (IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException | InstantiationException e) {
                e.printStackTrace();
            }
            propertyType.setPropertyName(propName);
            propertyTypes.add(propertyType);
        }

        return propertyTypes;
    }

    public static void GetTypesFromSchema(Schema schema, OpenApiSchemaType schemaType) {
        if (schema.getProperties() != null) {
            schemaType.setSchemaProperties(GetSchemaPropertyTypes(schema.getProperties()));
        }

        if (schema.getType() != null) {

            switch (schema.getType()) {
                case "integer" :
                    schemaType.setSchemaType("int");
                    break;
                case "string" :
                    schemaType.setSchemaType("string");
                    break;
                case "array" :
                    schemaType.setSchemaType("array");
                    schemaType.setItemType(((ArraySchema) schema).getItems().getType());
                    break;
                default :
                    schemaType.setSchemaType(schema.getType());
                    break;
            }
        }

        if (schema.getRequired() != null) {
            schemaType.setRequired(schema.getRequired());
        }

        if (schema.get$ref() != null) {
            schemaType.set$ref(schema.get$ref());
        }
    }



    public static List<OpenApiOperationType> TraveseOperations(Map<PathItem.HttpMethod, Operation> operations) {
        Iterator<Map.Entry<PathItem.HttpMethod, Operation>> operationIterator = operations.entrySet().iterator();
        List<OpenApiOperationType> operationTypes = new ArrayList<>();

        while (operationIterator.hasNext()) {
            Map.Entry<PathItem.HttpMethod, Operation> nextOp = operationIterator.next();
            OpenApiOperationType operation = new OpenApiOperationType();
            operation.setOperationType(nextOp.getKey().toString());

            if (nextOp.getValue().getParameters() != null && !nextOp.getValue().getParameters().isEmpty()) {
                operation.setParameters(TraverseParameters(nextOp.getValue().getParameters()));
            }

            if (nextOp.getValue().getResponses() != null && !nextOp.getValue().getResponses().isEmpty()) {
                operation.setResponses(TraverseResponses(nextOp.getValue().getResponses()));
            }

            if (nextOp.getValue().getRequestBody() != null) {
                operation.setRequestBodies(TraverseRequestBodies(nextOp.getValue().getRequestBody()));

            }

            operationTypes.add(operation);
        }

        return operationTypes;
    }

    public static List<OpenApiResponseType> TraverseResponses(ApiResponses responses) {
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

    public static List<OpenApiParameterType> TraverseParameters(List<Parameter> parameters) {
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

            GetTypesFromSchema(nextParam.getSchema(), parameterSchema);

            parameter.setParamName(nextParam.getName());
            parameter.setParamType(parameterSchema);
            parameterList.add(parameter);
        }

        return parameterList;
    }

    public static OpenApiRequestBodyType TraverseRequestBodies(RequestBody requestBody) {

        OpenApiRequestBodyType requestBodyType = new OpenApiRequestBodyType();
        Map<String, OpenApiSchemaType> contentList = new HashMap<>();

        if (requestBody.getRequired() != null) {
            requestBodyType.setRequired(requestBody.getRequired());
        }

        if (requestBody.getContent() != null) {
            for (Map.Entry<String, MediaType> entry : requestBody.getContent().entrySet()) {
                OpenApiSchemaType schemaType = new OpenApiSchemaType();
                final MediaType entryValue = entry.getValue();

                if (entryValue.getSchema() != null) {
                     GetTypesFromSchema(entryValue.getSchema(), schemaType);
                }

                contentList.put(entry.getKey(), schemaType);
            }

            requestBodyType.setContentList(contentList);
        }

        return requestBodyType;
    }

}
