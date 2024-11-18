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
package org.ballerinalang.langserver.rename;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;

/**
 * An abstract implementation of any rename test.
 */
public final class RenameTestUtil {

    private RenameTestUtil() {
    }

    public static void alterExpectedUri(JsonObject expected, Path sourceRoot) throws IOException {
        if (expected == null) {
            return;
        }
        if (!expected.getAsJsonObject("changes").isEmpty()) {
            JsonObject newChanges = new JsonObject();
            for (Map.Entry<String, JsonElement> jEntry : expected.getAsJsonObject("changes").entrySet()) {
                String[] uriComponents = jEntry.getKey().replace("\"", "").split("/");
                Path expectedPath = Path.of(sourceRoot.toUri());
                for (String uriComponent : uriComponents) {
                    expectedPath = expectedPath.resolve(uriComponent);
                }
                newChanges.add(expectedPath.toFile().getCanonicalPath(), jEntry.getValue());
            }
            expected.add("changes", newChanges);
        }
        if (expected.getAsJsonArray("documentChanges") != null) {
            JsonObject newObject = new JsonObject();
            for (JsonElement entry : expected.getAsJsonArray("documentChanges")) {
                JsonObject object = new JsonObject();
                String[] uriComponents = entry.getAsJsonObject().getAsJsonObject("textDocument").get("uri")
                        .getAsString().replace("\"", "").split("/");
                Path expectedPath = Path.of(sourceRoot.toUri());
                for (String uriComponent : uriComponents) {
                    expectedPath = expectedPath.resolve(uriComponent);
                }
                JsonObject textDocument = new JsonObject();
                textDocument.add("version", entry.getAsJsonObject().getAsJsonObject("textDocument").get("version"));
                textDocument.addProperty("uri", expectedPath.toString());

                object.add("textDocument", textDocument);
                object.add("edits", entry.getAsJsonObject().getAsJsonArray("edits"));
                newObject.add(expectedPath.toString(), object);
            }
            expected.add("documentChanges", newObject);
        }
    }

    public static void alterActualUri(JsonObject actual) throws IOException {
        JsonObject newChanges = new JsonObject();
        if (actual == null) {
            return;
        }
        if (!actual.getAsJsonObject("changes").isEmpty()) {
            for (Map.Entry<String, JsonElement> jEntry : actual.getAsJsonObject("changes").entrySet()) {
                String uri = jEntry.getKey().replace("\"", "");
                String canonicalPath = new File(URI.create(uri)).getCanonicalPath();
                newChanges.add(canonicalPath, jEntry.getValue());
            }
            actual.add("changes", newChanges);
        }
        if (actual.getAsJsonArray("documentChanges") != null) {
            JsonObject newObject = new JsonObject();
            for (JsonElement entry : actual.getAsJsonArray("documentChanges")) {
                JsonObject object = new JsonObject();
                String uri = entry.getAsJsonObject().getAsJsonObject("textDocument").get("uri")
                        .getAsString().replace("\"", "");
                String canonicalPath = new File(URI.create(uri)).getCanonicalPath();

                JsonObject textDocument = new JsonObject();
                textDocument.add("version", entry.getAsJsonObject().getAsJsonObject("textDocument").get("version"));
                textDocument.addProperty("uri", canonicalPath);

                object.add("textDocument", textDocument);
                object.add("edits", entry.getAsJsonObject().getAsJsonArray("edits"));
                newObject.add(canonicalPath, object);
            }
            actual.add("documentChanges", newObject);
        }
    }
}
