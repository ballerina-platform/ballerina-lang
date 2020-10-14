package io.ballerina.projects;

import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.ModuleLoadResponse;
import io.ballerina.projects.environment.PackageResolver;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.ballerinalang.model.elements.PackageID.ANNOTATIONS;
import static org.ballerinalang.model.elements.PackageID.INTERNAL;
import static org.ballerinalang.model.elements.PackageID.JAVA;

class Bootstrap {

    private static final Bootstrap instance = new Bootstrap();
    public static Bootstrap getInstance() {
        return instance;
    }

    private boolean langLibLoaded = false;
    private List<PackageID> langLibs = Arrays.asList(
            ANNOTATIONS,
            JAVA
    );


    void loadLangLib(CompilerContext compilerContext, PackageResolver packageResolver) {
        if (!langLibLoaded) {
            langLibLoaded = true;
            // we will load any lang.lib found in cache directory

            langLibs.stream().map(packageID -> toModuleRequest(packageID))
                .forEach(lib -> loadLib(lib, compilerContext, packageResolver));
            // Update the compiler context
            loadLangLibFromCache(compilerContext);
        }
    }

    private void loadLib(ModuleLoadRequest lib, CompilerContext compilerContext, PackageResolver packageResolver) {
        Collection<ModuleLoadResponse> modules = packageResolver.loadPackages(Arrays.asList(lib));
        modules.forEach(module -> {
            Package pkg = packageResolver.getPackage(module.packageId());
            PackageCompilation compilation = pkg.getCompilation();
            if (compilation.diagnostics().size() > 0) {
                throw new RuntimeException("Error while bootstraping :" + pkg.packageId().toString());
            }
        });
    }

    ModuleLoadRequest toModuleRequest(PackageID packageID) {
        PackageName packageName = PackageName.from(packageID.name.getValue());
        ModuleName moduleName = ModuleName.from(packageName);
        SemanticVersion version = SemanticVersion.from(packageID.getPackageVersion().toString());
        return new ModuleLoadRequest(packageID.orgName.getValue(), packageName, moduleName, version);
    }


    private void loadLangLibFromCache(CompilerContext context) {
        SymbolResolver symResolver = SymbolResolver.getInstance(context);
        SymbolTable symbolTable = SymbolTable.getInstance(context);
        if (getSymbolFromCache(context, ANNOTATIONS) != null) {
            symbolTable.langAnnotationModuleSymbol = getSymbolFromCache(context, ANNOTATIONS);
            symResolver.reloadErrorAndDependentTypes();
        }
    }

    private BPackageSymbol getSymbolFromCache(CompilerContext context, PackageID packageID) {
        PackageCache pkgCache = PackageCache.getInstance(context);
        BLangPackage bLangPackage = pkgCache.get(packageID);
        if (bLangPackage != null){
            return bLangPackage.symbol;
        }
        return null;
    }
}
