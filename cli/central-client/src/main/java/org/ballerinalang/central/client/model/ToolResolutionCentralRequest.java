/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.central.client.model;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Tool resolution request model used by tool dependency resolver in central client.
 *
 * @since 2201.9.0
 */
public class ToolResolutionCentralRequest {

    List<Tool> tools;

    /**
     * Tool resolution request tool model.
     */
    static class Tool {
        private String id;

        @JsonAdapter(EmptyStringTypeAdapter.class)
        private String version;
        Mode mode;

        public Tool(String id, String version, Mode mode) {
            this.id = id;
            this.version = version;
            this.mode = mode;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }
    }

    /**
     * Tool resolution response mode.
     */
    public enum Mode {
        @SerializedName("soft")
        SOFT("soft"),

        @SerializedName("medium")
        MEDIUM("medium"),

        @SerializedName("hard")
        HARD("hard");

        private final String text;

        Mode(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }
    }

    public ToolResolutionCentralRequest() {
        this.tools = new ArrayList<>();
    }

    public void addTool(String id, String version, Mode mode) {
        // The version is encoded to avoid issue handling the dash in pre-release version tag
        tools.add(new Tool(id, URLEncoder.encode(version, StandardCharsets.UTF_8), mode));
    }

    static class EmptyStringTypeAdapter
            extends TypeAdapter<String> {

        private EmptyStringTypeAdapter() {
        }

        @Override
        public void write(final JsonWriter jsonWriter, @Nullable final String s)
                throws IOException {
            if (s == null || s.isEmpty()) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(s);
            }
        }

        @Override
        @Nonnull
        @SuppressWarnings("EnumSwitchStatementWhichMissesCases")
        public String read(final JsonReader jsonReader)
                throws IOException {
            final JsonToken token = jsonReader.peek();
            return switch (token) {
                case NULL -> "";
                case STRING -> jsonReader.nextString();
                default -> throw new IllegalStateException("Unexpected token: " + token);
            };
        }
    }
}
