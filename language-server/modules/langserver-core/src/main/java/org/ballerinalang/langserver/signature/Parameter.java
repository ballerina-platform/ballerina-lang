/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.signature;

import io.ballerina.compiler.api.symbols.ParameterSymbol;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.commons.SignatureContext;

import java.util.Optional;

/**
 * Parameter model to hold the parameter information meta data.
 *
 * @since 2201.1.1
 */
public class Parameter {

    private final boolean isRestArg;
    private final boolean isOptional;
    private final ParameterSymbol parameterSymbol;
    private final SignatureContext signatureContext;

    public Parameter(ParameterSymbol parameterSymbol,
                     boolean isOptional,
                     boolean isRestArg,
                     SignatureContext signatureContext) {
        this.parameterSymbol = parameterSymbol;
        this.isOptional = isOptional;
        this.isRestArg = isRestArg;
        this.signatureContext = signatureContext;
    }

    public Optional<String> getName() {
        return (parameterSymbol.getName().isPresent() && this.isOptional)
                ? Optional.of(parameterSymbol.getName().get() + "?") : parameterSymbol.getName();
    }

    public String getType() {
        String type = NameUtil.getModifiedTypeName(this.signatureContext, parameterSymbol.typeDescriptor());
        if (this.isRestArg && !type.isEmpty()) {
            // Rest Arg type sometimes appear as array [], sometimes not eg. 'error()'
            if (type.endsWith("[]")) {
                type = type.substring(0, type.length() - 2);
            }
            type += "...";
        }

        return type;
    }

    public boolean isRestArg() {
        return isRestArg;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public ParameterSymbol getParameterSymbol() {
        return parameterSymbol;
    }

    public SignatureContext getSignatureContext() {
        return signatureContext;
    }
}
