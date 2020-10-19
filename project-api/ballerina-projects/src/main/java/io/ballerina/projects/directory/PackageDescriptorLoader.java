/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.directory;

import io.ballerina.projects.CompilationOptions;
import io.ballerina.projects.CompilationOptionsBuilder;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.model.BallerinaToml;
import io.ballerina.projects.model.BallerinaTomlProcessor;
import org.ballerinalang.toml.exceptions.TomlException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Creates a {@code PackageDescriptor} from a Ballerina.toml file.
 *
 * @since 2.0.0
 */
public class PackageDescriptorLoader {

    public static PackageDescriptor load(Path tomlFilePath) {
        BallerinaToml ballerinaToml;
        try {
            ballerinaToml = BallerinaTomlProcessor.parse(tomlFilePath);
            PackageName packageName = PackageName.from(ballerinaToml.getPackage().getName());
            PackageOrg packageOrg = PackageOrg.from(ballerinaToml.getPackage().getOrg());
            PackageVersion packageVersion = PackageVersion.from(ballerinaToml.getPackage().getVersion());
            return PackageDescriptor.from(packageName, packageOrg, packageVersion);
        } catch (IOException | TomlException e) {
            // TODO error handling
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static CompilationOptions createCompilationOptions() {
        // TODO Read the options from Ballerina.toml
        return new CompilationOptionsBuilder().build();
    }
}
