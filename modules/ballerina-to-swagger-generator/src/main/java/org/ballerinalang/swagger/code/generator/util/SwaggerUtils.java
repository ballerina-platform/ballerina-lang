package org.ballerinalang.swagger.code.generator.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import org.ballerinalang.swagger.code.generator.exception.SwaggerGenException;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeKeyValuePair;
import org.ballerinalang.util.codegen.AnnAttributeValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for swagger generation.
 */
public class SwaggerUtils {
    
    /**
     * Converts the annotation attributes to a map of a given annotation attachment.
     * @param annAttachmentInfo The annotation attachment.
     * @return A map of annotation attribute values mapped with keys.
     */
    public static Map<String, AnnAttributeValue> convertToAttributeMap(AnnAttachmentInfo annAttachmentInfo) {
        return Arrays.stream(annAttachmentInfo.getAttributeKeyValuePairs())
                .collect(Collectors.toMap(
                        AnnAttributeKeyValuePair::getAttributeName, AnnAttributeKeyValuePair::getAttributeValue));
    }
    
    /**
     * @param swagger Swagger definition
     * @return String representation of current service object.
     */
    public static String generateSwaggerString(Swagger swagger) throws SwaggerGenException {
        try {
            return Json.mapper().writeValueAsString(swagger);
        } catch (JsonProcessingException e) {
            throw new SwaggerGenException("Error while generating Swagger definition.");
        }
    }
}
