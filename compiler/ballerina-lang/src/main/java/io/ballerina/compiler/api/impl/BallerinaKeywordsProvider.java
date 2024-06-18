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
package io.ballerina.compiler.api.impl;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides the set of Ballerina Reserved Keywords to be used at the symbol factory.
 *
 * @since 2.0.0
 */
public final class BallerinaKeywordsProvider {
    public static final List<String> BALLERINA_KEYWORDS;

    static {
        BALLERINA_KEYWORDS = getBallerinaKeywords();
    }

    private BallerinaKeywordsProvider() {
    }

    private static List<String> getBallerinaKeywords() {
        // NOTE: This is a temporary fix to retrieve lexer defined keywords until we come up with an appropriate API.
        // The same implementation can be found in the language server common utils.
        // Related discussion can be found in https://github.com/ballerina-platform/ballerina-lang/discussions/28827
        try {
            Class<?> aClass = Class.forName("io.ballerina.compiler.internal.parser.LexerTerminals");
            return Arrays.stream(aClass.getDeclaredFields())
                    .filter(field -> field.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                            && (field.getType() == String.class))
                    .map(field -> {
                        try {
                            return field.get(null).toString();
                        } catch (IllegalAccessException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (ClassNotFoundException e) {
            return Collections.emptyList();
        }
    }
}
