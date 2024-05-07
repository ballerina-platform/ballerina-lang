package org.wso2.ballerinalang.compiler.bir.codegen.bytecodeoptimizer;

import java.util.Set;

/**
 * Java record to store class level native optimization report data in json format.
 *
 * @since 2201.9.0
 */
public record NativeDependencyOptimizationReport(Set<String> startPointClasses, Set<String> usedExternalClasses,
                                                 Set<String> usedClasses, Set<String> unusedClasses) {

}
