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
import io.ballerina.semver.checker.diff.ClassDiff;
import io.ballerina.semver.checker.diff.Diff;
import io.ballerina.semver.checker.diff.DiffKind;
import io.ballerina.semver.checker.diff.EnumDiff;
import io.ballerina.semver.checker.diff.EnumMemberDiff;
import io.ballerina.semver.checker.diff.FunctionDiff;
import io.ballerina.semver.checker.diff.ModuleConstantDiff;
import io.ballerina.semver.checker.diff.ModuleDiff;
import io.ballerina.semver.checker.diff.ModuleVarDiff;
import io.ballerina.semver.checker.diff.NodeDiff;
import io.ballerina.semver.checker.diff.NodeListDiff;
import io.ballerina.semver.checker.diff.ObjectFieldDiff;
import io.ballerina.semver.checker.diff.PackageDiff;
import io.ballerina.semver.checker.diff.SemverImpact;
import io.ballerina.semver.checker.diff.ServiceDiff;
import io.ballerina.semver.checker.diff.TypeDefinitionDiff;

import static io.ballerina.semver.checker.util.SemverUtils.calculateSuggestedVersion;

/**
 * Diff model related utilities.
 *
 * @since 2201.2.0
 */
public class DiffUtils {

    // Attributes defined for the JSON representation of diffs.
    public static final String DIFF_ATTR_KIND = "kind";
    public static final String DIFF_ATTR_TYPE = "type";
    public static final String DIFF_ATTR_MESSAGE = "message";
    public static final String DIFF_ATTR_VERSION_IMPACT = "versionImpact";
    public static final String DIFF_ATTR_CHILDREN = "childDiffs";
    private static final String UNKNOWN = "unknown";

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

        if ((diff instanceof NodeDiff<?> nodeDiff) && nodeDiff.getMessage().isPresent()) {
            sb.append(nodeDiff.getMessage().get());
        } else if ((diff instanceof NodeListDiff<?> nodeListDiff) && nodeListDiff.getMessage().isPresent()) {
            sb.append(nodeListDiff.getMessage().get());
        } else {
            sb.append(diff.getKind() != null && diff.getKind() != DiffKind.UNKNOWN ? diff.getKind().toString() :
                            getDiffTypeName(diff))
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
     * Returns whether the provided diff instance is a compound diff object, which is guaranteed to have child diffs.
     *
     * @param diff diff instance
     * @return true if the provided diff instance is a compound diff object, which is guaranteed to have child diffs
     */
    public static boolean isCompoundDiff(Diff diff) {
        return diff instanceof FunctionDiff
                || diff instanceof ServiceDiff
                || diff instanceof ModuleVarDiff
                || diff instanceof ModuleConstantDiff
                || diff instanceof ClassDiff
                || diff instanceof ObjectFieldDiff
                || diff instanceof TypeDefinitionDiff
                || diff instanceof EnumDiff
                || diff instanceof EnumMemberDiff;
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
                    return UNKNOWN;
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
                    return UNKNOWN;
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
        if (diff instanceof PackageDiff diff1) {
            return getPackageName(diff1);
        } else if (diff instanceof ModuleDiff diff1) {
            return getModuleName(diff1);
        } else if (diff instanceof FunctionDiff diff1) {
            return getFunctionName(diff1);
        } else if (diff instanceof ServiceDiff diff1) {
            return getServiceName(diff1);
        } else if (diff instanceof ModuleVarDiff diff1) {
            return getModuleVariableName(diff1);
        } else if (diff instanceof ModuleConstantDiff diff1) {
            return getModuleConstantName(diff1);
        } else if (diff instanceof ClassDiff diff1) {
            return getModuleClassName(diff1);
        } else if (diff instanceof TypeDefinitionDiff diff1) {
            return getModuleTypeDefName(diff1);
        } else if (diff instanceof EnumDiff diff1) {
            return getModuleEnumName(diff1);
        } else {
            return UNKNOWN;
        }
    }

    public static String getDiffTypeName(Diff diff) {
        // Todo: Replace remaining usages to `diff.getKind()` method
        if (diff instanceof PackageDiff) {
            return "package";
        } else if (diff instanceof ModuleDiff) {
            return "module";
        } else if (diff instanceof ServiceDiff) {
            return "service";
        } else if (diff instanceof ModuleVarDiff) {
            return "module variable";
        } else if (diff instanceof ModuleConstantDiff) {
            return "module constant";
        } else if (diff instanceof ClassDiff) {
            return "class";
        } else if (diff instanceof TypeDefinitionDiff) {
            return "type definition";
        } else if (diff instanceof EnumDiff) {
            return "enum declaration";
        } else if (diff instanceof FunctionDiff functionDiff) {
            if (functionDiff.isResource()) {
                return "resource function";
            } else if (functionDiff.isRemote()) {
                return "remote function";
            } else {
                return "function";
            }
        } else {
            return UNKNOWN;
        }
    }

    private static String getDiffIndentation(Diff diff) {
        if (diff instanceof PackageDiff) {
            return " ".repeat(0);
        } else if (diff instanceof ModuleDiff) {
            return " ".repeat(2);
        } else if (diff instanceof ServiceDiff) {
            return " ".repeat(4);
        } else if (diff instanceof ModuleVarDiff) {
            return " ".repeat(4);
        } else if (diff instanceof ModuleConstantDiff) {
            return " ".repeat(4);
        } else if (diff instanceof ClassDiff) {
            return " ".repeat(4);
        } else if (diff instanceof TypeDefinitionDiff) {
            return " ".repeat(4);
        } else if (diff instanceof EnumDiff) {
            return " ".repeat(4);
        } else if (diff instanceof EnumMemberDiff) {
            return " ".repeat(6);
        } else if (diff instanceof ObjectFieldDiff) {
            return " ".repeat(6);
        } else if (diff instanceof FunctionDiff functionDiff) {
            if (functionDiff.isResource()) {
                return " ".repeat(6);
            } else if (functionDiff.isRemote()) {
                return " ".repeat(6);
            } else {
                return " ".repeat(4);
            }
        } else {
            return " ".repeat(6);
        }
    }

    /**
     * Retrieves function name of the given {@link FunctionDiff} instance.
     *
     * @param functionDiff FunctionDiff instance
     */
    private static String getFunctionName(FunctionDiff functionDiff) {
        if (functionDiff.getNewNode().isPresent()) {
            return SyntaxTreeUtils.getFunctionIdentifier(functionDiff.getNewNode().get());
        } else if (functionDiff.getOldNode().isPresent()) {
            return SyntaxTreeUtils.getFunctionIdentifier(functionDiff.getOldNode().get());
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Retrieves service name from the given {@link ServiceDiff} instance.
     *
     * @param serviceDiff ServiceDiff instance
     */
    private static String getServiceName(ServiceDiff serviceDiff) {
        if (serviceDiff.getNewNode().isPresent()) {
            return SyntaxTreeUtils.getServiceIdentifier(serviceDiff.getNewNode().get()).orElse(UNKNOWN);
        } else if (serviceDiff.getOldNode().isPresent()) {
            return SyntaxTreeUtils.getServiceIdentifier(serviceDiff.getOldNode().get()).orElse(UNKNOWN);
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Retrieves name of the given {@link ModuleVarDiff} instance.
     *
     * @param moduleVarDiff FunctionDiff instance
     */
    private static String getModuleVariableName(ModuleVarDiff moduleVarDiff) {
        if (moduleVarDiff.getNewNode().isPresent()) {
            return SyntaxTreeUtils.getModuleVarIdentifier(moduleVarDiff.getNewNode().get());
        } else if (moduleVarDiff.getOldNode().isPresent()) {
            return SyntaxTreeUtils.getModuleVarIdentifier(moduleVarDiff.getOldNode().get());
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Retrieves name of the given {@link ModuleConstantDiff} instance.
     *
     * @param moduleConstantDiff FunctionDiff instance
     */
    private static String getModuleConstantName(ModuleConstantDiff moduleConstantDiff) {
        if (moduleConstantDiff.getNewNode().isPresent()) {
            return SyntaxTreeUtils.getConstIdentifier(moduleConstantDiff.getNewNode().get());
        } else if (moduleConstantDiff.getOldNode().isPresent()) {
            return SyntaxTreeUtils.getConstIdentifier(moduleConstantDiff.getOldNode().get());
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Retrieves name of the given {@link ClassDiff} instance.
     *
     * @param classDiff class diff instance
     */
    private static String getModuleClassName(ClassDiff classDiff) {
        if (classDiff.getNewNode().isPresent()) {
            return SyntaxTreeUtils.getClassIdentifier(classDiff.getNewNode().get());
        } else if (classDiff.getOldNode().isPresent()) {
            return SyntaxTreeUtils.getClassIdentifier(classDiff.getOldNode().get());
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Retrieves name of the given {@link TypeDefinitionDiff} instance.
     *
     * @param typeDefinitionDiff type definition diff instance
     */
    private static String getModuleTypeDefName(TypeDefinitionDiff typeDefinitionDiff) {
        if (typeDefinitionDiff.getNewNode().isPresent()) {
            return SyntaxTreeUtils.getTypeDefIdentifier(typeDefinitionDiff.getNewNode().get());
        } else if (typeDefinitionDiff.getOldNode().isPresent()) {
            return SyntaxTreeUtils.getTypeDefIdentifier(typeDefinitionDiff.getOldNode().get());
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Retrieves name of the given {@link EnumDiff} instance.
     *
     * @param enumDiff enum declaration diff instance
     */
    private static String getModuleEnumName(EnumDiff enumDiff) {
        if (enumDiff.getNewNode().isPresent()) {
            return SyntaxTreeUtils.getEnumIdentifier(enumDiff.getNewNode().get());
        } else if (enumDiff.getOldNode().isPresent()) {
            return SyntaxTreeUtils.getEnumIdentifier(enumDiff.getOldNode().get());
        } else {
            return UNKNOWN;
        }
    }
}
