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
package io.ballerina.projects.plugins.codeaction;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A command argument for the code action. Will be supplied to the code action when it's being executed.
 *
 * @since 2.0.0
 */
public class CodeActionArgument {

    private static final Gson GSON = new Gson();

    private String key;
    private Object value;

    private CodeActionArgument(String argumentK, Object value) {
        this.key = argumentK;
        this.value = value;
    }

    public static CodeActionArgument from(String argumentK, Object argumentV) {
        return new CodeActionArgument(argumentK, argumentV);
    }

    public static CodeActionArgument from(Object jsonObj) {
        // NOTE: we are not hard-coding any field names here
        CodeActionArgument argument = GSON.fromJson(((JsonObject) jsonObj), CodeActionArgument.class);
        return new CodeActionArgument(argument.key(),
                GSON.toJsonTree(argument.value()));
    }

    public String key() {
        return key;
    }

    public <T> T value() {
        return (T) value;
    }

    public <T> T valueAs(Class<T> typeClass) {
        return GSON.fromJson((JsonElement) value, typeClass);
    }
}
