/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.toml.validator;

import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.toml.validator.schema.AbstractSchema;
import io.ballerina.toml.validator.schema.SchemaDeserializer;
import io.ballerina.toml.validator.schema.Type;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.Map;

/**
 * Contains utilities required for Toml schema validator.
 *
 * @since 2.0.0
 */
public class ValidationUtil {

    public static String getTypeErrorMessage(AbstractSchema schema, TomlType found, String key) {
        Map<String, String> message = schema.message();
        String typeCustomMessage = message.get(SchemaDeserializer.TYPE);
        if (typeCustomMessage == null) {
            return String.format("incompatible type for key '%s': expected '%s', found '%s'", key, schema.type(),
                    tomlTypeValueToSchemaType(found));
        }
        return typeCustomMessage;
    }

    public static Type tomlTypeValueToSchemaType(TomlType tomlType) {
        switch (tomlType) {
            case STRING:
                return Type.STRING;
            case DOUBLE:
            case INTEGER:
                return Type.NUMBER;
            case BOOLEAN:
                return Type.BOOLEAN;
            case ARRAY:
            case TABLE_ARRAY:
                return Type.ARRAY;
            case TABLE:
            case INLINE_TABLE:
                return Type.OBJECT;
            default:
                throw new IllegalArgumentException("Toml type is a invalid value");
        }
    }

    public static TomlDiagnostic getTomlDiagnostic(TomlNodeLocation location, String code, String template,
                                                   DiagnosticSeverity severity, String message) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(code, template, severity);
        return new TomlDiagnostic(location, diagnosticInfo, message);
    }
}
