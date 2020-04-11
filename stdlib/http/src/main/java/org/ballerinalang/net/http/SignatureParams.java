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
import static org.ballerinalang.net.http.compiler.ResourceValidator.COMPULSORY_PARAM_COUNT;

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
    private static final String PARAM = "$param$";

    SignatureParams(List<BType> paramTypes, AttachedFunction resource) {
        this.signatureParamTypes = paramTypes;
        List<String> paramAnnotationKeys = resource.getParamAnnotationKeys();
        // params annotated
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
                case PROTOCOL_PACKAGE_HTTP + ":" + ANN_NAME_BODY_PARAM:
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
     * Gets the map of query params for given raw query string.
     *
     * @return a map of query params
     */
    MapValue<String, Object> getQueryParams(Object rawQueryString) {
        BMapType mapType = new BMapType(new BArrayType(BTypes.typeString));
        MapValue<String, Object> queryParams = new MapValueImpl<>(mapType);

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
}
