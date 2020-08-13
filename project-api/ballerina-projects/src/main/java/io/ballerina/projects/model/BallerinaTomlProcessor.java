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

package io.ballerina.projects.model;

import com.google.gson.JsonSyntaxException;
import com.moandjiezana.toml.Toml;
import org.ballerinalang.toml.exceptions.TomlException;
import org.ballerinalang.toml.model.BuildOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Ballerina Toml Processor which processes the toml file parsed and populate the BallerinaToml POJO.
 *
 * @since 2.0.0
 */
public class BallerinaTomlProcessor {

    private BallerinaTomlProcessor() {}

    /**
     * Get the char stream of the content from file.
     *
     * @param tomlPath path of the toml file
     * @return manifest object
     * @throws IOException   exception if the file cannot be found
     * @throws TomlException exception when parsing toml
     */
    public static BallerinaToml parse(Path tomlPath) throws IOException, TomlException {
        InputStream targetStream = new FileInputStream(tomlPath.toFile());
        BallerinaToml ballerinaToml = parse(targetStream);
        validateManifestDependencies(ballerinaToml);
        return ballerinaToml;
    }

    /**
     * Get the char stream from input stream.
     *
     * @param inputStream input stream of the toml file content
     * @return manifest object
     * @throws TomlException exception when parsing toml
     */
    private static BallerinaToml parse(InputStream inputStream) throws TomlException {
        try {
            Toml toml = new Toml().read(inputStream);
            if (toml.isEmpty()) {
                throw new TomlException("invalid Ballerina.toml file: organization, name and the version of the "
                        + "package is missing. example: \n" + "[package]\n" + "org=\"my_org\"\n"
                        + "name=\"my_package\"\n" + "version=\"1.0.0\"\n");
            }

            if (null == toml.getTable("package")) {
                throw new TomlException("invalid Ballerina.toml file: cannot find [package]");
            }

            BallerinaToml ballerinaToml = toml.to(BallerinaToml.class);
            ballerinaToml.getPackage().setOrg(toml.getString("package.org"));
            ballerinaToml.getPackage().setName(toml.getString("package.name"));
            ballerinaToml.getPackage().setVersion(toml.getString("package.version"));

            if (toml.contains("build-options")) {
                Toml buildOptionsTable = toml.getTable("build-options");
                BuildOptions buildOptions = new BuildOptions();
                if (buildOptionsTable.contains("observability-included")) {
                    buildOptions.setObservabilityIncluded(buildOptionsTable.getBoolean("observability-included"));
                }
                ballerinaToml.setBuildOptions(buildOptions);
            }
            validateBallerinaTomlPackage(ballerinaToml);
            validateManifestDependencies(ballerinaToml);
            return ballerinaToml;
        } catch (IllegalStateException | JsonSyntaxException ise) {
            String tomlErrMsg = lowerCaseFirstLetter(
                    ise.getMessage().replace("java.lang.IllegalStateException: ", "").toLowerCase(Locale.getDefault()));
            throw new TomlException("invalid Ballerina.toml file: " + tomlErrMsg);
        }
    }

    /**
     * Validate the package block in BallerinaToml.
     *
     * @param ballerinaToml The BallerinaToml
     * @throws TomlException When the package block is invalid
     */
    private static void validateBallerinaTomlPackage(BallerinaToml ballerinaToml) throws TomlException {
        if (null == ballerinaToml.getPackage().getOrg() || "".equals(ballerinaToml.getPackage().getOrg())) {
            throw new TomlException("invalid Ballerina.toml file: cannot find 'org' under [package]");
        }

        if (null == ballerinaToml.getPackage().getName() || "".equals(ballerinaToml.getPackage().getName())) {
            throw new TomlException("invalid Ballerina.toml file: cannot find 'name' under [package]");
        }

        if (null == ballerinaToml.getPackage().getVersion() || "".equals(ballerinaToml.getPackage().getVersion())) {
            throw new TomlException("invalid Ballerina.toml file: cannot find 'version' under [package]");
        }

        final Pattern semVerPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
        Matcher semverMatcher = semVerPattern.matcher(ballerinaToml.getPackage().getVersion());
        if (!semverMatcher.matches()) {
            throw new TomlException("invalid Ballerina.toml file: 'version' under [package] is not semver");
        }
    }

    /**
     * Validate dependencies block in BallerinaToml.
     *
     * @param ballerinaToml The BallerinaToml
     * @throws TomlException When the dependencies block is invalid
     */
    private static void validateManifestDependencies(BallerinaToml ballerinaToml) throws TomlException {
        for (Map.Entry<String, Object> dependency : getDependenciesAsObjectMap(ballerinaToml.getDependencies())
                .entrySet()) {
            if (!(dependency.getValue() instanceof String) && !(dependency.getValue() instanceof Map)) {
                throw new TomlException(
                        "invalid Ballerina.toml file: invalid metadata found for dependency" + " [" + dependency
                                .getKey() + "]");
            }
        }
    }

    private static Map<String, Object> getDependenciesAsObjectMap(Map<String, Object> dependencies) {
        return dependencies.entrySet().stream()
                .collect(Collectors.toMap(d -> d.getKey().replaceAll("^\"|\"$", ""), Map.Entry::getValue));
    }

    /**
     * Lowercase the first letter of this string.
     *
     * @param content content
     * @return converted string
     */
    private static String lowerCaseFirstLetter(String content) {
        return content.substring(0, 1).toLowerCase(Locale.getDefault()) + content.substring(1);
    }
}
