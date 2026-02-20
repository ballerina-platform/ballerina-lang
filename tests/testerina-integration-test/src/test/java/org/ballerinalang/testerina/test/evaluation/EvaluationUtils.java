/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.testerina.test.evaluation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.testerina.test.utils.CommonUtils;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class EvaluationUtils {
    private static final Boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.getDefault())
            .contains("win");
    private static final Path EVALUATION_OUTPUTS_DIR = Path
            .of("src", "test", "resources", "evaluation-outputs");

    private EvaluationUtils() {
    }

    public static void assertOutput(String outputFileName, String output) throws IOException {
        String regex = "(?m)^\\s*[/A-Za-z].*\\R?";
        output = CommonUtils.replaceExecutionTime(output);
        output = replaceProjectPath(output);
        if (IS_WINDOWS) {
            String fileContent = Files.readString(EVALUATION_OUTPUTS_DIR.resolve("windows").resolve(outputFileName));
            Assert.assertEquals(output.replaceAll("\r\n|\r", "\n").replaceAll(regex, "")
                            .stripTrailing(),
                    fileContent.replaceAll("\r\n|\r", "\n").replaceAll(regex, "")
                            .stripTrailing());
            return;
        }
        String fileContent = Files.readString(EVALUATION_OUTPUTS_DIR.resolve("unix").resolve(outputFileName));
        Assert.assertEquals(output.stripTrailing().replaceAll(regex, ""),
                fileContent.stripTrailing().replaceAll(regex, ""));
    }

    public static void assertJsonReport(String jsonReportName, String jsonReport) throws IOException {
        if (IS_WINDOWS) {
            String fileContent = Files.readString(EVALUATION_OUTPUTS_DIR.resolve("windows").resolve(jsonReportName));
            JsonUtil.assertJsonEqualsIgnoreArrayOrder(jsonReport.replaceAll("\r\n|\r", "\n").stripTrailing(),
                    fileContent.replaceAll("\r\n|\r", "\n").stripTrailing());
            return;
        }
        String fileContent = Files.readString(EVALUATION_OUTPUTS_DIR.resolve("unix").resolve(jsonReportName));
        JsonUtil.assertJsonEqualsIgnoreArrayOrder(jsonReport.stripTrailing(), fileContent.stripTrailing());
    }

    protected static String replaceProjectPath(String content) {
        content = CommonUtils.replaceVaryingString("Generating Test Report", "evaluation-tests", content);
        return CommonUtils.replaceVaryingString("warning: Could not find the required HTML " +
                "report tools for code coverage at", "lib", content);
    }

    private static class JsonUtil {
        private static final Gson GSON = new Gson();

        public static void assertJsonEqualsIgnoreArrayOrder(String json1, String json2) {
            JsonElement e1 = normalize(JsonParser.parseString(json1));
            JsonElement e2 = normalize(JsonParser.parseString(json2));
            if (!e1.equals(e2)) {
                throw new AssertionError("JSON documents do not match.\nExpected:\n" + e1 + "\nActual:\n" + e2);
            }
        }

        private static JsonElement normalize(JsonElement element) {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                JsonObject normalized = new JsonObject();

                for (String key : obj.keySet()) {
                    normalized.add(key, normalize(obj.get(key)));
                }
                return normalized;
            }

            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                List<JsonElement> elements = new ArrayList<>();
                for (JsonElement elem : array) {
                    elements.add(normalize(elem));
                }
                elements.sort(Comparator.comparing(GSON::toJson));
                JsonArray normalizedArray = new JsonArray();
                for (JsonElement elem : elements) {
                    normalizedArray.add(elem);
                }
                return normalizedArray;
            }
            return element;
        }
    }
}
