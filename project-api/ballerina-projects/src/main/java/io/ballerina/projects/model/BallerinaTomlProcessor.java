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
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.utils.ProjectUtils;
import org.ballerinalang.toml.exceptions.TomlException;

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
     * Parse `ballerina.toml` file in to `BallerinaToml` object.
     *
     * @param tomlPath path of the toml file
     * @return `BallerinaToml` object
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
     * Parse `ballerina.toml` file stream in to `BallerinaToml` object.
     *
     * @param inputStream input stream of the toml file content
     * @return `BallerinaToml` object
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
            Package pkg = ballerinaToml.getPackage();
            pkg.setOrg(toml.getString("package.org"));
            pkg.setName(toml.getString("package.name"));
            pkg.setVersion(toml.getString("package.version"));
            pkg.setLicense(toml.getList("package.license"));
            pkg.setAuthors(toml.getList("package.authors"));
            pkg.setRepository(toml.getString("package.repository"));
            pkg.setKeywords(toml.getList("package.keywords"));
            pkg.setExported(toml.getList("package.exported"));

            if (toml.contains("build-options")) {
                Toml buildOptionsTable = toml.getTable("build-options");
                BuildProject.BuildOptions buildOptions = buildOptionsTable.to(BuildProject.BuildOptions.class);
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
    public static void validateBallerinaTomlPackage(BallerinaToml ballerinaToml) throws TomlException {
        // check org exists
        String org = ballerinaToml.getPackage().getOrg();
        if (null == org || "".equals(org)) {
            throw new TomlException("invalid Ballerina.toml file: cannot find 'org' under [package]");
        }

        // check org is valid identifier
        boolean isValidOrg = ProjectUtils.validateOrgName(org);
        if (!isValidOrg) {
            throw new TomlException("invalid Ballerina.toml file: Invalid 'org' under [package]: '" + org + "' :\n"
                    + "'org' can only contain alphanumerics, underscores and periods "
                    + "and the maximum length is 256 characters");
        }

        // check name exists
        String pkg = ballerinaToml.getPackage().getName();
        if (null == pkg || "".equals(pkg)) {
            throw new TomlException("invalid Ballerina.toml file: cannot find 'name' under [package]");
        }

        // check that the package name is valid
        boolean isValidPkg = ProjectUtils.validatePkgName(pkg);
        if (!isValidPkg) {
            throw new TomlException("invalid Ballerina.toml file: Invalid 'name' under [package]: '" + pkg + "' :\n"
                    + "'name' can only contain alphanumerics, underscores and periods "
                    + "and the maximum length is 256 characters");
        }

        // check version exists
        if (null == ballerinaToml.getPackage().getVersion() || "".equals(ballerinaToml.getPackage().getVersion())) {
            throw new TomlException("invalid Ballerina.toml file: cannot find 'version' under [package]");
        }

        // check version is compatible with semver
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
        ballerinaToml.setDependencies(getDependenciesAsObjectMap(ballerinaToml.getDependencies()));
        for (Map.Entry<String, Object> dependency : ballerinaToml.getDependencies().entrySet()) {
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
