/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model.types;

import static org.ballerinalang.model.types.TypeConstants.ANY_TNAME;
import static org.ballerinalang.model.types.TypeConstants.ANY_TSIG;
import static org.ballerinalang.model.types.TypeConstants.ARRAY_TNAME;
import static org.ballerinalang.model.types.TypeConstants.ARRAY_TSIG;
import static org.ballerinalang.model.types.TypeConstants.BOOLEAN_TNAME;
import static org.ballerinalang.model.types.TypeConstants.BOOLEAN_TSIG;
import static org.ballerinalang.model.types.TypeConstants.BYTE_TNAME;
import static org.ballerinalang.model.types.TypeConstants.BYTE_TSIG;
import static org.ballerinalang.model.types.TypeConstants.CONNECTOR_TNAME;
import static org.ballerinalang.model.types.TypeConstants.CONNECTOR_TSIG;
import static org.ballerinalang.model.types.TypeConstants.C_JSON_TNAME;
import static org.ballerinalang.model.types.TypeConstants.DECIMAL_TNAME;
import static org.ballerinalang.model.types.TypeConstants.DECIMAL_TSIG;
import static org.ballerinalang.model.types.TypeConstants.FLOAT_TNAME;
import static org.ballerinalang.model.types.TypeConstants.FLOAT_TSIG;
import static org.ballerinalang.model.types.TypeConstants.INT_TNAME;
import static org.ballerinalang.model.types.TypeConstants.INT_TSIG;
import static org.ballerinalang.model.types.TypeConstants.JSON_TNAME;
import static org.ballerinalang.model.types.TypeConstants.MAP_TNAME;
import static org.ballerinalang.model.types.TypeConstants.REFTYPE_TSIG;
import static org.ballerinalang.model.types.TypeConstants.STREAM_TNAME;
import static org.ballerinalang.model.types.TypeConstants.STRING_TNAME;
import static org.ballerinalang.model.types.TypeConstants.STRING_TSIG;
import static org.ballerinalang.model.types.TypeConstants.STRUCT_TNAME;
import static org.ballerinalang.model.types.TypeConstants.STRUCT_TSIG;
import static org.ballerinalang.model.types.TypeConstants.TABLE_TNAME;
import static org.ballerinalang.model.types.TypeConstants.VOID_TNAME;
import static org.ballerinalang.model.types.TypeConstants.VOID_TSIG;
import static org.ballerinalang.model.types.TypeConstants.XML_TNAME;

/**
 * {@code TypeEnum} represents all the types names in Ballerina.
 *
 * @since 0.8.0
 */
public enum TypeEnum {
    INT(INT_TNAME, INT_TSIG),
    BYTE(BYTE_TNAME, BYTE_TSIG),
    FLOAT(FLOAT_TNAME, FLOAT_TSIG),
    DECIMAL(DECIMAL_TNAME, DECIMAL_TSIG),
    STRING(STRING_TNAME, STRING_TSIG),
    BOOLEAN(BOOLEAN_TNAME, BOOLEAN_TSIG),
    XML(XML_TNAME, REFTYPE_TSIG),
    JSON(JSON_TNAME, REFTYPE_TSIG),
    CJSON(C_JSON_TNAME, REFTYPE_TSIG),
    MAP(MAP_TNAME, REFTYPE_TSIG),
    TABLE(TABLE_TNAME, REFTYPE_TSIG),
    VOID(VOID_TNAME, VOID_TSIG),
    STRUCT(STRUCT_TNAME, STRUCT_TSIG),
    STREAM(STREAM_TNAME, REFTYPE_TSIG),
    ANY(ANY_TNAME, ANY_TSIG),
    ARRAY(ARRAY_TNAME, ARRAY_TSIG),
    CONNECTOR(CONNECTOR_TNAME, CONNECTOR_TSIG),
    EMPTY("", VOID_TSIG);

    private String name;
    private String sig;

    TypeEnum(String name, String sig) {
        this.name = name;
        this.sig = sig;
    }

    public String getName() {
        return name;
    }

    public String getSig() {
        return sig;
    }
}
