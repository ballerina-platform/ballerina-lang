/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.commons;

import org.eclipse.lsp4j.SemanticTokenModifiers;
import org.eclipse.lsp4j.SemanticTokenTypes;

/**
 * Represents the semantic tokens APIs related context.
 *
 * @since 2.0.0
 */
public interface SemanticTokensContext extends DocumentServiceContext {

    /**
     * Semantic token types supported by the Ballerina Language Server.
     */
    enum TokenTypes {
        NAMESPACE(0, SemanticTokenTypes.Namespace), TYPE(1, SemanticTokenTypes.Type), CLASS(2,
                SemanticTokenTypes.Class), ENUM(3, SemanticTokenTypes.Enum), INTERFACE(4,
                SemanticTokenTypes.Interface), STRUCT(5, SemanticTokenTypes.Struct), TYPE_PARAMETER(6,
                SemanticTokenTypes.TypeParameter), PARAMETER(7, SemanticTokenTypes.Parameter), VARIABLE(8,
                SemanticTokenTypes.Variable), PROPERTY(9, SemanticTokenTypes.Property), ENUM_MEMBER(10,
                SemanticTokenTypes.EnumMember), FUNCTION(11, SemanticTokenTypes.Function), METHOD(12,
                SemanticTokenTypes.Method);

        private final int id;
        private final String value;

        TokenTypes(int id, String value) {
            this.id = id;
            this.value = value;
        }

        public int getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Semantic token type modifiers supported by the Ballerina Language Server.
     */
    enum TokenTypeModifiers {
        DECLARATION(1, SemanticTokenModifiers.Declaration), READONLY(1 << 1, SemanticTokenModifiers.Readonly),
        DOCUMENTATION(1 << 2, SemanticTokenModifiers.Documentation);

        private final int id;
        private final String value;

        TokenTypeModifiers(int id, String value) {
            this.id = id;
            this.value = value;
        }

        public int getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }
}
