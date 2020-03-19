package org.ballerinalang.observability.anaylze;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.compiler.common.modal.SymbolMetaInfo;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.extensions.VisibleEndpointVisitor;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of ObserverbilitySymbolCollector.
 */
public class DefaultObservabilitySymbolCollector implements ObservabilitySymbolCollector {
    private final PrintStream console = System.out;;
    private static final String NAME = "name";
    private static final String ORG_NAME = "orgName";
    private static final String PKG_VERSION = "pkgVersion";
    private static final String COMPILATION_UNITS = "compilationUnits";
    private static final String SRC = "src";
    private static final String KEY_URI = "uri";
    private static final String AST = "ast";

    private CompilerContext compilerContext;

    private DiagnosticLog diagnosticLog;

    @Override
    public void init(CompilerContext context) {
        compilerContext = context;
        diagnosticLog = BLangDiagnosticLogHelper.getInstance(context);
        console.println("DefaultObservabilitySymbolCollector initialized");
    }

    @Override
    public void process(BLangPackage module) {
        console.println("DefaultObservabilitySymbolCollector process called");
        JsonObject moduleJson = getModuleJson(module);
        JsonASTHolder.getInstance().addAST(module.packageID.name.getValue(), moduleJson);
    }

    private JsonObject getModuleJson(BLangPackage module) {
        JsonObject moduleJson = new JsonObject();
        moduleJson.addProperty(NAME, module.packageID.name.getValue());
        moduleJson.addProperty(ORG_NAME, module.packageID.getOrgName().getValue());
        moduleJson.addProperty(PKG_VERSION, module.packageID.getPackageVersion().getValue());
        Map<BLangNode, List<SymbolMetaInfo>> visibleEPsByNode = getVisibleEndpoints(module);

        JsonObject jsonCUnits = new JsonObject();
        List<BLangCompilationUnit> compilationUnits = module.getCompilationUnits();
        for (BLangCompilationUnit cUnit: compilationUnits) {
            JsonObject jsonCUnit = getCUnitJson(visibleEPsByNode, cUnit);
            jsonCUnits.add(cUnit.name, jsonCUnit);
        }

        moduleJson.add(COMPILATION_UNITS, jsonCUnits);
        return moduleJson;
    }

    private Map<BLangNode, List<SymbolMetaInfo>> getVisibleEndpoints(BLangPackage packageNode) {
        Map<BLangNode, List<SymbolMetaInfo>> visibleEPsByNode;
        VisibleEndpointVisitor visibleEndpointVisitor = new VisibleEndpointVisitor(compilerContext);
        visibleEndpointVisitor.visit(packageNode);
        visibleEPsByNode = visibleEndpointVisitor.getVisibleEPsByNode();
        return visibleEPsByNode;
    }

    private JsonObject getCUnitJson(Map<BLangNode, List<SymbolMetaInfo>> visibleEPsByNode, BLangCompilationUnit cUnit) {
        JsonObject jsonCUnit = new JsonObject();
        jsonCUnit.addProperty(NAME, cUnit.name);

        SourceDirectory sourceDirectory = compilerContext.get(SourceDirectory.class);
        Path sourceRoot = sourceDirectory.getPath();
        String uri = sourceRoot.resolve(
                Paths.get(SRC, cUnit.getPosition().getSource().cUnitName,
                        cUnit.getPosition().getSource().cUnitName)).toUri().toString();
        jsonCUnit.addProperty(KEY_URI, uri);
        try {
            JsonElement jsonAST = TextDocumentFormatUtil.generateJSON(cUnit, new HashMap<>(), visibleEPsByNode);
            jsonCUnit.add(AST, jsonAST);
        } catch (JSONGenerationException e) {
            diagnosticLog.logDiagnostic(Diagnostic.Kind.WARNING, cUnit.getPosition(),
                    "Error while generating json AST for " + cUnit.name + ". " + e.getMessage());
        }
        return jsonCUnit;
    }
}
