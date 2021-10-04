package org.ballerinalang.debugadapter.evaluation;

import io.ballerina.compiler.api.symbols.ModuleSymbol;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.MODULE_NAME_SEPARATOR_REGEX;

/**
 * A helper class to hold resolved imports information during evaluation.
 *
 * @since 2.0.0.
 */
public class BImport {

    private final String orgName;
    private final String moduleName;
    private final String alias;
    private ModuleSymbol resolvedModuleSymbol;

    public BImport(String orgName, String moduleName, String alias) {
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.alias = alias;
    }

    public String orgName() {
        return orgName;
    }

    public String packageName() {
        return moduleName.split(MODULE_NAME_SEPARATOR_REGEX)[0];
    }

    public String moduleName() {
        return moduleName;
    }

    public String alias() {
        return alias;
    }

    public ModuleSymbol getResolvedSymbol() {
        return resolvedModuleSymbol;
    }

    public void setResolvedSymbol(ModuleSymbol moduleSymbol) {
        resolvedModuleSymbol = moduleSymbol;
    }
}
