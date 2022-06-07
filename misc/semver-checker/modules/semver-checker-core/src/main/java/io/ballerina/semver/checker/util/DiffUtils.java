/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semver.checker.util;

import io.ballerina.projects.SemanticVersion;
import io.ballerina.semver.checker.diff.Diff;
import io.ballerina.semver.checker.diff.FunctionDiff;
import io.ballerina.semver.checker.diff.ModuleDiff;
import io.ballerina.semver.checker.diff.NodeDiff;
import io.ballerina.semver.checker.diff.NodeListDiff;
import io.ballerina.semver.checker.diff.PackageDiff;
import io.ballerina.semver.checker.diff.SemverImpact;

import static io.ballerina.semver.checker.util.SemverUtils.calculateSuggestedVersion;

/**
 * Diff model related utilities.
 *
 * @since 2201.2.0
 */
public class DiffUtils {

    public static final String DIFF_ATTR_KIND = "kind";
    public static final String DIFF_ATTR_TYPE = "type";
    public static final String DIFF_ATTR_MESSAGE = "message";
    public static final String DIFF_ATTR_VERSION_IMPACT = "versionImpact";
    public static final String DIFF_ATTR_CHILDREN = "childDiffs";

    /**
     * Returns the summary of changes in string format based on the current version, last published version and the set
     * of detected changes.
     *
     * @param packageDiff     source code changes of the package instance
     * @param localVersion    current package version
     * @param previousVersion last published package version
     * @return the suggested version in string format
     */
    public static String getDiffSummary(PackageDiff packageDiff, SemanticVersion localVersion,
                                        SemanticVersion previousVersion) {
        StringBuilder sb = new StringBuilder();
        if (packageDiff == null) {
            sb.append("no changes detected").append(System.lineSeparator());
        } else {
            String title = String.format(" Comparing version '%s'(local) with version '%s'(central) ", localVersion,
                    previousVersion);
            sb.append(System.lineSeparator());
            sb.append("=".repeat(title.length())).append(System.lineSeparator());
            sb.append(title).append(System.lineSeparator());
            sb.append("=".repeat(title.length())).append(System.lineSeparator());
            sb.append(packageDiff.getAsString());
        }

        return sb.toString();
    }

    /**
     * Returns the suggested version in string format based on the current version, last published version and the set
     * of detected changes.
     *
     * @param packageDiff     source code changes of the package instance
     * @param localVersion    current package version
     * @param previousVersion last published package version
     * @return the suggested version in string format
     */
    public static String getVersionSuggestion(PackageDiff packageDiff, SemanticVersion localVersion,
                                              SemanticVersion previousVersion) {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append("current version: ").append(localVersion).append(System.lineSeparator());
        sb.append("compatibility impact (compared with the release version '").append(previousVersion).append("'): ");
        if (packageDiff == null) {
            sb.append("no changes detected").append(System.lineSeparator());
        } else {
            switch (packageDiff.getVersionImpact()) {
                case MAJOR:
                    sb.append("backward-incompatible changes detected").append(System.lineSeparator());
                    break;
                case MINOR:
                    sb.append("patch-incompatible changes detected").append(System.lineSeparator());
                    break;
                case PATCH:
                    sb.append("patch-compatible changes detected").append(System.lineSeparator());
                    break;
                case AMBIGUOUS:
                    sb.append("one or more changes detected with ambiguous level of impact. the developer is expected" +
                            " to manually review the changes below and choose an appropriate version");
                    sb.append(System.lineSeparator());
                    packageDiff.getChildDiffs(SemverImpact.AMBIGUOUS)
                            .forEach(diff -> sb.append(diff.getAsString()));
                    break;
                case UNKNOWN:
                default:
                    sb.append("one or more changes detected with unknown level of impact. the developer is expected " +
                            "to manually review the changes below and choose an appropriate version");
                    sb.append(System.lineSeparator());
                    packageDiff.getChildDiffs(SemverImpact.UNKNOWN);
                    break;
            }
        }
        sb.append("suggested version: ").append(calculateSuggestedVersion(previousVersion, packageDiff));
        return sb.toString();
    }

    /**
     * Coverts a given diff instance into a human-readable string.
     *
     * @param diff Diff instance
     */
    public static String stringifyDiff(Diff diff) {
        StringBuilder sb = new StringBuilder();
        sb.append(getDiffIndentation(diff))
                .append(getDiffSign(diff))
                .append(" ");

        if ((diff instanceof NodeDiff) && ((NodeDiff<?>) diff).getMessage().isPresent()) {
            sb.append(((NodeDiff<?>) diff).getMessage().get());
        } else if ((diff instanceof NodeListDiff) && ((NodeListDiff<?>) diff).getMessage().isPresent()) {
            sb.append(((NodeListDiff<?>) diff).getMessage().get());
        } else {
            sb.append(getDiffTypeName(diff))
                    .append(" '")
                    .append(getDiffName(diff))
                    .append("' is ")
                    .append(getDiffVerb(diff));
        }

        sb.append(" [")
                .append("version impact: ")
                .append(diff.getVersionImpact())
                .append("]")
                .append(System.lineSeparator());

        return sb.toString();
    }

    /**
     * Retrieves package name of the given {@link PackageDiff} instance.
     *
     * @param packageDiff PackageDiff instance
     */
    private static String getPackageName(PackageDiff packageDiff) {
        switch (packageDiff.getType()) {
            case NEW:
                return packageDiff.getNewPackage().orElseThrow().packageName().value();
            case REMOVED:
                return packageDiff.getOldPackage().orElseThrow().packageName().value();
            case MODIFIED:
            case UNKNOWN:
            default:
                if (packageDiff.getNewPackage().isPresent()) {
                    return packageDiff.getNewPackage().orElseThrow().packageName().value();
                } else if (packageDiff.getOldPackage().isPresent()) {
                    return packageDiff.getOldPackage().orElseThrow().packageName().value();
                } else {
                    return "unknown";
                }
        }
    }

    /**
     * Retrieves module name of the given {@link ModuleDiff} instance.
     *
     * @param moduleDiff ModuleDiff instance
     */
    private static String getModuleName(ModuleDiff moduleDiff) {
        switch (moduleDiff.getType()) {
            case NEW:
                return moduleDiff.getNewModule().orElseThrow().moduleName().toString();
            case REMOVED:
                return moduleDiff.getOldModule().orElseThrow().moduleName().toString();
            case MODIFIED:
            case UNKNOWN:
            default:
                if (moduleDiff.getNewModule().isPresent()) {
                    return moduleDiff.getNewModule().orElseThrow().moduleName().toString();
                } else if (moduleDiff.getOldModule().isPresent()) {
                    return moduleDiff.getOldModule().orElseThrow().moduleName().toString();
                } else {
                    return "unknown";
                }
        }
    }

    /**
     * Retrieves function name of the given {@link FunctionDiff} instance.
     *
     * @param functionDiff FunctionDiff instance
     */
    private static String getFunctionName(FunctionDiff functionDiff) {
        switch (functionDiff.getType()) {
            case NEW:
                return functionDiff.getNewNode().orElseThrow().functionName().text();
            case REMOVED:
                return functionDiff.getOldNode().orElseThrow().functionName().text();
            case MODIFIED:
            case UNKNOWN:
            default:
                if (functionDiff.getNewNode().isPresent()) {
                    return functionDiff.getNewNode().orElseThrow().functionName().text();
                } else if (functionDiff.getOldNode().isPresent()) {
                    return functionDiff.getOldNode().orElseThrow().functionName().text();
                } else {
                    return "unknown";
                }
        }
    }

    /**
     * Returns the string sign to represent a given {@link Diff}.
     *
     * @param diff diff type
     */
    private static String getDiffSign(Diff diff) {
        switch (diff.getType()) {
            case NEW:
                return "[++]";
            case REMOVED:
                return "[--]";
            case MODIFIED:
                return "[+-]";
            case UNKNOWN:
            default:
                return "[??]";
        }
    }

    private static String getDiffVerb(Diff diff) {
        switch (diff.getType()) {
            case NEW:
                return "added";
            case REMOVED:
                return "removed";
            case MODIFIED:
                return "modified";
            case UNKNOWN:
            default:
                return "?";
        }
    }

    private static String getDiffName(Diff diff) {
        if (diff instanceof PackageDiff) {
            return getPackageName((PackageDiff) diff);
        } else if (diff instanceof ModuleDiff) {
            return getModuleName((ModuleDiff) diff);
        } else if (diff instanceof FunctionDiff) {
            return getFunctionName((FunctionDiff) diff);
        } else {
            return "unknown";
        }
    }

    public static String getDiffTypeName(Diff diff) {
        if (diff instanceof PackageDiff) {
            return "package";
        } else if (diff instanceof ModuleDiff) {
            return "module";
        } else if (diff instanceof FunctionDiff) {
            return "function";
        } else {
            return "unknown";
        }
    }

    private static String getDiffIndentation(Diff diff) {
        if (diff instanceof PackageDiff) {
            return " ".repeat(0);
        } else if (diff instanceof ModuleDiff) {
            return " ".repeat(2);
        } else if (diff instanceof FunctionDiff) {
            return " ".repeat(4);
        } else {
            return " ".repeat(6);
        }
    }
}
