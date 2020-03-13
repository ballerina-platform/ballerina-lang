/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http;

import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.net.uri.URIUtil;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_BODY_PARAM;
import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_PATH_PARAM;
import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_QUERY_PARAM;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.compiler.ResourceSignatureValidator.COMPULSORY_PARAM_COUNT;

/**
 * This class holds the resource signature parameters.
 *
 * @since 0.963.0
 */
public class SignatureParams {

    private final List<BType> signatureParamTypes;
    private Map<String, Integer> pathParamOrder = new HashMap<>();
    private Map<String, Integer> queryParamOrder = new HashMap<>();
    private String bodyParamName = null;
    private int bodyParamOrderIndex = -1;
    private static final String PARAM = "$param$"; //TODO move this
    private MapValue<String, Object> queryParams;

    SignatureParams(List<BType> paramTypes, AttachedFunction resource) {
        this.signatureParamTypes = paramTypes;
        List<String> paramAnnotationKeys = resource.getParamAnnotationKeys(); //TODO make sure all params are annotated
        int paramIndex = COMPULSORY_PARAM_COUNT;
        for (String paramName : paramAnnotationKeys) {
            String paramType = ((MapValue) resource.getAnnotation(paramName)).getKeys()[0].toString();
            paramName = paramName.substring(PARAM.length() + 1);
            switch (paramType) {
                case PROTOCOL_PACKAGE_HTTP + ":" + ANN_NAME_PATH_PARAM:
                    pathParamOrder.put(paramName, paramIndex++);
                    break;
                case PROTOCOL_PACKAGE_HTTP + ":" + ANN_NAME_QUERY_PARAM:
                    queryParamOrder.put(paramName, paramIndex++);
                    break;
                case PROTOCOL_PACKAGE_HTTP + ":" + ANN_NAME_BODY_PARAM: //TODO validate compile time to have single
                    // BodyParam annotation
                    this.bodyParamName = paramName;
                    this.bodyParamOrderIndex = paramIndex++;
                    break;
                default:
                    throw HttpUtil.createHttpError("invalid resource signature parameter annotation",
                                                   HttpErrorType.GENERIC_LISTENER_ERROR);
            }
        }
    }

    /**
     * Validate path, query and body param types.
     */
    void validate() {
        validatePathParam(pathParamOrder, signatureParamTypes);
        validateQueryParam(queryParamOrder, signatureParamTypes);
        validateBodyParam(bodyParamOrderIndex, signatureParamTypes);
    }

    private void validatePathParam(Map<String, Integer> pathParamOrder, List<BType> signatureParamTypes) {
        for (Map.Entry<String, Integer> entry : pathParamOrder.entrySet()) {
            BType paramType = signatureParamTypes.get(entry.getValue());
            int varTag = paramType.getTag();
            if (varTag != TypeTags.STRING_TAG && varTag != TypeTags.INT_TAG && varTag != TypeTags.BOOLEAN_TAG &&
                    varTag != TypeTags.FLOAT_TAG) {
                throw HttpUtil.createHttpError("incompatible path parameter type : expected `string`, `int`, " +
                                                       "`boolean`, `float`", HttpErrorType.GENERIC_LISTENER_ERROR);
            }
        }
    }

    private void validateQueryParam(Map<String, Integer> queryParamOrder, List<BType> signatureParamTypes) {
        for (Map.Entry<String, Integer> entry : queryParamOrder.entrySet()) {
            BType paramType = signatureParamTypes.get(entry.getValue());
            int varTag = paramType.getTag();
            if (varTag != TypeTags.STRING_TAG && (varTag != TypeTags.ARRAY_TAG ||
                    ((BArrayType) paramType).getElementType().getTag() != TypeTags.STRING_TAG)) {
                throw HttpUtil.createHttpError("incompatible query parameter type : expected `string`, `string[]`",
                                               HttpErrorType.GENERIC_LISTENER_ERROR);
            }
        }
    }

    private void validateBodyParam(int bodyParamOrderIndex, List<BType> signatureParamTypes) {
        if (bodyParamOrderIndex == -1) {
            return;
        }
        BType paramType = signatureParamTypes.get(bodyParamOrderIndex);
        int type = paramType.getTag();
        if (type != TypeTags.RECORD_TYPE_TAG && type != TypeTags.JSON_TAG && type != TypeTags.XML_TAG &&
                type != TypeTags.STRING_TAG && (type != TypeTags.ARRAY_TAG || !validArrayType(paramType))) {
            throw HttpUtil.createHttpError("incompatible entity-body type : " + paramType.getName(),
                                           HttpErrorType.GENERIC_LISTENER_ERROR);
        }
    }

    /**
     * Check the validity of array type in data binding scenario.
     *
     * @param entityBodyParamType Represents resource parameter details
     * @return a boolean indicating the validity of the array type
     */
    private boolean validArrayType(BType entityBodyParamType) {
        return ((BArrayType) entityBodyParamType).getElementType().getTag() == TypeTags.BYTE_TAG ||
                ((BArrayType) entityBodyParamType).getElementType().getTag() == TypeTags.RECORD_TYPE_TAG;
    }

    /**
     * Gets the list of signature parameter types.
     *
     * @return a list of param type
     */
    List<BType> getSignatureParamTypes() {
        return signatureParamTypes;
    }

    /**
     * Gets the map of path parameter order details.
     *
     * @return a map of param details
     */
    Map<String, Integer> getPathParamOrder() {
        return this.pathParamOrder;
    }

    /**
     * Gets the map of query parameter order details.
     *
     * @return a map of param details
     */
    Map<String, Integer> getQueryParamOrder() {
        return this.queryParamOrder;
    }

    /**
     * Gets the availability of body parameter.
     *
     * @return true of body param is stated
     */
    boolean isBodyParamAvailable() {
        return this.bodyParamName != null;
    }

    /**
     * Gets the body parameter index.
     *
     * @return the index of param
     */
    int getBodyParamOrderIndex() {
        return this.bodyParamOrderIndex;
    }

    /**
     * Gets the map of query params for given raw query string.
     *
     * @return a map of query params
     */
    MapValue<String, Object> getQueryParams(Object rawQueryString) {
        if (queryParams != null) {
            return queryParams;
        }
        BMapType mapType = new BMapType(new BArrayType(BTypes.typeString));
        queryParams = new MapValueImpl<>(mapType);

        if (rawQueryString != null) {
            try {
                URIUtil.populateQueryParamMap((String) rawQueryString, queryParams);
            } catch (UnsupportedEncodingException e) {
                throw HttpUtil.createHttpError("error while retrieving query param from message: " + e.getMessage(),
                                               HttpErrorType.GENERIC_LISTENER_ERROR);
            }
        }
        return queryParams;
    }
}
