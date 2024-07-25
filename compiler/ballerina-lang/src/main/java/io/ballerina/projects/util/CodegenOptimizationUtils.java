/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.projects.util;

import org.ballerinalang.model.elements.PackageID;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.ballerina.projects.util.CodegenOptimizationConstants.BALLERINAX;
import static io.ballerina.projects.util.CodegenOptimizationConstants.BALLERINA_JBALLERINA_JAVA;
import static io.ballerina.projects.util.CodegenOptimizationConstants.BALLERINA_LANG;
import static io.ballerina.projects.util.CodegenOptimizationConstants.BALLERINA_OBSERVE;
import static io.ballerina.projects.util.CodegenOptimizationConstants.DOT_DRIVER;
import static io.ballerina.projects.util.CodegenOptimizationConstants.INTEROP_DEPENDENCIES_PROPERTIES_FILE;

/**
 * Common utils for codegen optimization implementation.
 *
 * @since 2201.10.0
 */
public final class CodegenOptimizationUtils {

    private static final String ENV_OPTION_BAL_JAVA_DEBUG = "BAL_DISABLE_HARDCODED_OPTIMIZATIONS";
    private static final boolean DISABLE_HARDCODED_OPTIMIZATIONS =
            System.getenv().containsKey(ENV_OPTION_BAL_JAVA_DEBUG);
    private static final Set<String> WHITELSITED_FILE_NAMES = Set.of("types.bal", "error.bal", "stream_types.bal");
    private static final Set<String> PKGS_WITH_WHITELSITED_FILES =
            Set.of("ballerinax/mysql", "ballerina/sql", "ballerinax/persist.sql");

    // These directories are whitelisted due to usages of "sun.misc.Unsafe" class
    // TODO Find a way to whitelist only the necessary class files
    private static final Set<String> WHITELISTED_DIRECTORIES = Set.of("io/netty/util");

    /**
     * These classes are used by GraalVM when building the native-image. Since they are not connected to the root class,
     * they will be removed by the NativeDependencyOptimizer if they are not whitelisted.
     */
    private static final Set<String> GRAALVM_FEATURE_CLASSES =
            Set.of("io/ballerina/stdlib/crypto/svm/BouncyCastleFeature");
    public static final String CLASS = ".class";
    private static final String SERVICE_PROVIDER_DIRECTORY = "META-INF/services/";
    public static final String MODULE_INFO = "module-info";
    private static final String NATIVE_IMAGE_DIRECTORY = "META-INF/native-image";
    private static final String REFLECT_CONFIG_JSON = "reflect-config.json";
    private static final String JNI_CONFIG_JSON = "jni-config.json";

    private CodegenOptimizationUtils() {
    }

    public static boolean isObserveModule(String packageName) {
        return packageName.equals(BALLERINA_OBSERVE);
    }

    public static boolean isDriverModule(String packageName) {
        return packageName.startsWith(BALLERINAX) && packageName.endsWith(DOT_DRIVER);
    }

    public static boolean isLangLibModule(String packageName) {
        return packageName.startsWith(BALLERINA_LANG);
    }

    public static boolean isJBallerinaModule(PackageID packageName) {
        return isJBallerinaModule(packageName.getPackageNameWithOrg());
    }

    public static boolean isJBallerinaModule(String packageName) {
        return packageName.equals(BALLERINA_JBALLERINA_JAVA);
    }

    public static boolean isWhiteListedModule(PackageID packageID) {
        String packageName = packageID.getPackageNameWithOrg();
        return isWhiteListedModule(packageName);
    }

    public static boolean isWhiteListedModule(String packageName) {
        // Drive modules get called from java side, observe is used at compiler level and we can't analyze langlib
        // modules. So, we must always whitelist them.
        return isObserveModule(packageName) || isDriverModule(packageName) || isJBallerinaModule(packageName) ||
                isLangLibModule(packageName);
    }

    public static boolean isPackageWithWhiteListedFiles(String packageName) {
        if (DISABLE_HARDCODED_OPTIMIZATIONS) {
            return false;
        }
        return PKGS_WITH_WHITELSITED_FILES.contains(packageName);
    }

    public static boolean isWhiteListedFile(String fileName) {
        if (DISABLE_HARDCODED_OPTIMIZATIONS) {
            return false;
        }
        return WHITELSITED_FILE_NAMES.contains(fileName);
    }

    public static boolean isWhiteListedDirectory(String path) {
        return WHITELISTED_DIRECTORIES.stream().anyMatch(path::startsWith);
    }

    public static Set<String> getWhiteListedClasses() {
        return GRAALVM_FEATURE_CLASSES;
    }

    public static boolean isWhiteListedEntryName(String entryName) {
        if (entryName.equals(MODULE_INFO + CLASS)) {
            return true;
        }
        return isWhiteListedDirectory(entryName);
    }

    public static boolean isServiceProvider(String entryName) {
        return entryName.startsWith(SERVICE_PROVIDER_DIRECTORY);
    }

    public static boolean isReflectionConfig(String entryName) {
        return (entryName.endsWith(REFLECT_CONFIG_JSON) || entryName.endsWith(JNI_CONFIG_JSON)) &&
                entryName.startsWith(NATIVE_IMAGE_DIRECTORY);
    }

    public static Map<String, Set<String>> getHardcodedDependencies() {
        if (DISABLE_HARDCODED_OPTIMIZATIONS) {
            return Map.of();
        }
        Properties prop = new Properties();
        try {
            InputStream stream = CodegenOptimizationUtils.class.getClassLoader()
                    .getResourceAsStream(INTEROP_DEPENDENCIES_PROPERTIES_FILE);
            prop.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load interop-dependencies : ", e);
        }
        Pattern pattern = Pattern.compile(",");
        return prop.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> pattern.splitAsStream(e.getValue().toString())
                                .collect(Collectors.toUnmodifiableSet())
                ));
    }
}
