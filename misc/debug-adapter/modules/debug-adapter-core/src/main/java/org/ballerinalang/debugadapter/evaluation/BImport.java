package org.ballerinalang.debugadapter.evaluation;

import io.ballerina.compiler.api.symbols.ModuleSymbol;

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

    public String moduleName() {
        return moduleName;
    }

    public String alias() {
        return alias;
    }

    public ModuleSymbol getModuleSymbol() {
        return resolvedModuleSymbol;
    }

    public void setResolvedModuleSymbol(ModuleSymbol moduleSymbol) {
        resolvedModuleSymbol = moduleSymbol;
    }
}