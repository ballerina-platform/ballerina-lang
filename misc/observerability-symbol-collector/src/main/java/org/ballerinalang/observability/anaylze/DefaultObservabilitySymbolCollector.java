/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.observability.anaylze;

import com.google.gson.JsonElement;
import org.ballerinalang.langserver.compiler.common.modal.SymbolMetaInfo;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.extensions.VisibleEndpointVisitor;
import org.ballerinalang.observability.anaylze.model.CUnitASTHolder;
import org.ballerinalang.observability.anaylze.model.PkgASTHolder;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectoryManager;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of ObserverbilitySymbolCollector.
 *
 * @since 2.0.0
 */
public class DefaultObservabilitySymbolCollector implements ObservabilitySymbolCollector {

    private static final String NAME = "name";
    private static final String ORG_NAME = "orgName";
    private static final String PKG_VERSION = "pkgVersion";
    private static final String BALLERINA_VERSION = "ballerinaVersion";
    private static final String COMPILATION_UNITS = "compilationUnits";
    private static final String SRC = "src";
    private static final String KEY_URI = "uri";
    private static final String AST = "ast";
    private static final String JSON = ".json";
    public static final String PROGRAM_HASH_KEY = "PROGRAM_HASH";
    public static final String AST_META_FILENAME = "meta.properties";

    private CompilerContext compilerContext;

    private DiagnosticLog diagnosticLog;

    @Override
    public void init(CompilerContext context) {
        compilerContext = context;
        diagnosticLog = BLangDiagnosticLog.getInstance(context);
    }

    @Override
    public void process(BLangPackage module) {
        PkgASTHolder pkgASTHolder = getModuleASTHolder(module);
        JsonASTHolder.getInstance().addAST(module.packageID.name.getValue(), pkgASTHolder);
    }

    @Override
    public void writeCollectedSymbols(BLangPackage module, Path destination)
            throws IOException, NoSuchAlgorithmException {
        Path targetDirPath = destination.resolve(AST);
        if (Files.notExists(targetDirPath)) {
            Files.createDirectory(targetDirPath);
        }

        Set<String> userPackages = getUserPackages();
        Map<String, PkgASTHolder> userPackageASTMap = new HashMap<>();
        for (Map.Entry<String, PkgASTHolder> entry : JsonASTHolder.getInstance().getASTMap().entrySet()) {
            if (userPackages.contains(entry.getKey())) {
                userPackageASTMap.put(entry.getKey(), entry.getValue());
            }
        }

        String astDataString = generateCanonicalJsonString(userPackageASTMap);
        Files.write(targetDirPath.resolve(AST + JSON), astDataString.getBytes(StandardCharsets.UTF_8));

        Properties props = new Properties();
        props.setProperty(PROGRAM_HASH_KEY, getAstHash(astDataString));
        try (OutputStream outputStream = Files.newOutputStream(targetDirPath.resolve(AST_META_FILENAME))) {
            props.store(outputStream, null);
        }
    }

    private String getAstHash(String astDataString) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(astDataString.getBytes(StandardCharsets.UTF_8));

        return bytesToHex(encodedHash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private Set<String> getUserPackages() {
        SourceDirectoryManager sourceDirectoryManager = SourceDirectoryManager.getInstance(compilerContext);
        return sourceDirectoryManager.listSourceFilesAndPackages()
                .map(x -> x.getName().getValue())
                .collect(Collectors.toSet());
    }

    private PkgASTHolder getModuleASTHolder(BLangPackage module) {
        PkgASTHolder pkgASTHolder = new PkgASTHolder();
        pkgASTHolder.setName(module.packageID.name.getValue());
        pkgASTHolder.setOrgName(module.packageID.getOrgName().getValue());
        pkgASTHolder.setVersion(module.packageID.getPackageVersion().getValue());
        Map<BLangNode, List<SymbolMetaInfo>> visibleEPsByNode = getVisibleEndpoints(module);

        List<BLangCompilationUnit> compilationUnits = module.getCompilationUnits();
        for (BLangCompilationUnit cUnit : compilationUnits) {
            CUnitASTHolder jsonCUnit = getCUnitASTHolder(visibleEPsByNode, cUnit);
            pkgASTHolder.addCompilationUnit(cUnit.name, jsonCUnit);
        }
        return pkgASTHolder;
    }

    private Map<BLangNode, List<SymbolMetaInfo>> getVisibleEndpoints(BLangPackage packageNode) {
        Map<BLangNode, List<SymbolMetaInfo>> visibleEPsByNode;
        VisibleEndpointVisitor visibleEndpointVisitor = new VisibleEndpointVisitor(compilerContext);
        visibleEndpointVisitor.visit(packageNode);
        visibleEPsByNode = visibleEndpointVisitor.getVisibleEPsByNode();
        return visibleEPsByNode;
    }

    private CUnitASTHolder getCUnitASTHolder(Map<BLangNode, List<SymbolMetaInfo>> visibleEPsByNode,
                                             BLangCompilationUnit cUnit) {
        CUnitASTHolder cUnitASTHolder = new CUnitASTHolder();
        cUnitASTHolder.setName(cUnit.name);

        SourceDirectory sourceDirectory = compilerContext.get(SourceDirectory.class);
        Path sourceRoot = sourceDirectory.getPath();
        String uri = sourceRoot.resolve(
                Paths.get(SRC, cUnit.getPosition().getSource().cUnitName,
                        cUnit.getPosition().getSource().cUnitName)).toUri().toString();
        cUnitASTHolder.setUri(uri);
        try {
            JsonElement jsonAST = TextDocumentFormatUtil.generateJSON(cUnit, new HashMap<>(), visibleEPsByNode);
            cUnitASTHolder.setAst(jsonAST);
        } catch (JSONGenerationException e) {
            diagnosticLog.logDiagnostic(Diagnostic.Kind.WARNING, cUnit.getPosition(),
                    "Error while generating json AST for " + cUnit.name + ". " + e.getMessage());
        }
        return cUnitASTHolder;
    }

    private String generateCanonicalJsonString(Map<String, PkgASTHolder> packageMap) throws IOException {
        final String ballerinaVersion = RepoUtils.getBallerinaVersion();
        StringBuilder jsonStringBuilder = new StringBuilder().append("{");
        String[] packageNames = packageMap.keySet().toArray(new String[0]);
        Arrays.sort(packageNames);
        for (int i = 0, packageNamesLength = packageNames.length; i < packageNamesLength; i++) {
            String packageName = packageNames[i];
            PkgASTHolder pkgASTHolder = packageMap.get(packageName);

            if (i != 0) {
                jsonStringBuilder.append(",");
            }

            jsonStringBuilder.append("\"").append(packageName).append("\":{\"")
                             .append(NAME).append("\":\"").append(pkgASTHolder.getName()).append("\",\"")
                             .append(ORG_NAME).append("\":\"").append(pkgASTHolder.getOrgName()).append("\",\"")
                             .append(PKG_VERSION).append("\":\"").append(pkgASTHolder.getVersion()).append("\",\"")
                             .append(BALLERINA_VERSION).append("\":\"").append(ballerinaVersion).append("\",\"")
                             .append(COMPILATION_UNITS).append("\":").append("{");

            String[] cUnitNames = pkgASTHolder.getCompilationUnits().keySet().toArray(new String[0]);
            Arrays.sort(cUnitNames);
            for (int j = 0, cUnitNamesLength = cUnitNames.length; j < cUnitNamesLength; j++) {
                String cUnitName = cUnitNames[j];
                CUnitASTHolder cUnitASTHolder = pkgASTHolder.getCompilationUnits().get(cUnitName);
                String astDataString = JsonCanonicalizer.getEncodedString(cUnitASTHolder.getAst().toString());

                if (j != 0) {
                    jsonStringBuilder.append(",");
                }
                jsonStringBuilder.append("\"").append(cUnitName).append("\":{\"")
                        .append(NAME).append("\":\"").append(cUnitASTHolder.getName()).append("\",\"")
                        .append(KEY_URI).append("\":\"").append(cUnitASTHolder.getUri()).append("\",\"")
                        .append(AST).append("\":").append(astDataString)
                        .append("}");
            }
            jsonStringBuilder.append("}}");
        }
        return jsonStringBuilder.append("}").toString();
    }
}
