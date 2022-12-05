/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.architecturemodelgenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utils for component generation testings.
 *
 * @since 2201.3.1
 */
public class TestUtils {
    public static Project loadBuildProject(Path projectPath, boolean isSingleFileProject) {
        BuildOptions buildOptions = BuildOptions.builder().setOffline(true).setConfigSchemaGen(true).build();
        if (isSingleFileProject) {
            return SingleFileProject.load(projectPath, buildOptions);
        } else {
            return BuildProject.load(projectPath, buildOptions);
        }
    }

    public static ComponentModel getComponentFromGivenJsonFile(Path expectedFilePath) throws IOException {
        Stream<String> lines = Files.lines(expectedFilePath);
        String content = lines.collect(Collectors.joining(System.lineSeparator()));
        lines.close();
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.fromJson(content, ComponentModel.class);
    }
}
