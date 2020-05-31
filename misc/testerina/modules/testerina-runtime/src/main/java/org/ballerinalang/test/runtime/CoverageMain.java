/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.runtime;

import com.google.gson.Gson;
import org.ballerinalang.test.runtime.entity.CoverageReport;
import org.ballerinalang.test.runtime.entity.ModuleCoverage;
import org.ballerinalang.test.runtime.util.TesterinaConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main class to init the test coverage generation.
 */
public class CoverageMain {

    public static void main(String[] args) throws Exception {
        Path targetDir = Paths.get(args[1]);;
        Path testJarPath = Paths.get(args[2]);
        String orgName = args[3];
        String moduleName = args[4];
        String version = args[5];

        CoverageReport coverageReport = new CoverageReport(testJarPath, targetDir, orgName, moduleName, version);
        coverageReport.generateReport();
        Path jsonTmpSummaryPath = Paths.get(args[0], TesterinaConstants.COVERAGE_FILE);
        writeCoverageToJsonFile(ModuleCoverage.getInstance(), jsonTmpSummaryPath);
    }

    private static void writeCoverageToJsonFile(ModuleCoverage moduleCoverage, Path tmpJsonPath) throws IOException {
        File jsonFile = new File(tmpJsonPath.toString());
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            String json = gson.toJson(moduleCoverage);
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        }
    }
}
