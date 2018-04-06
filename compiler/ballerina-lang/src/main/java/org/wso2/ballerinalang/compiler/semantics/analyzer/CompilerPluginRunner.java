/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.compiler.plugins.CompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportEndpointTypes;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;

/**
 * Invoke {@link CompilerPlugin} plugins.
 * <p>
 * This class visit All the package-level nodes.
 *
 * @since 0.962.0
 */
public class CompilerPluginRunner extends BLangNodeVisitor {
    private static final CompilerContext.Key<CompilerPluginRunner> COMPILER_PLUGIN_RUNNER_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private PackageCache packageCache;
    private SymbolResolver symResolver;
    private Names names;
    private BLangDiagnosticLog dlog;

    private DiagnosticPos defaultPos;
    private List<CompilerPlugin> pluginList;
    private Map<DefinitionID, List<CompilerPlugin>> processorMap;
    private Map<DefinitionID, List<CompilerPlugin>> endpointProcessorMap;


    public static CompilerPluginRunner getInstance(CompilerContext context) {
        CompilerPluginRunner annotationProcessor = context.get(COMPILER_PLUGIN_RUNNER_KEY);
        if (annotationProcessor == null) {
            annotationProcessor = new CompilerPluginRunner(context);
        }

        return annotationProcessor;
    }

    private CompilerPluginRunner(CompilerContext context) {
        context.put(COMPILER_PLUGIN_RUNNER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.packageCache = PackageCache.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.names = Names.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);

        this.pluginList = new ArrayList<>();
        this.processorMap = new HashMap<>();
        this.endpointProcessorMap = new HashMap<>();
    }

    public BLangPackage runPlugins(BLangPackage pkgNode) {
        this.defaultPos = pkgNode.pos;
        loadPlugins();
        pkgNode.accept(this);
        return pkgNode;
    }

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.COMPILER_PLUGIN)) {
            return;
        }

        pluginList.forEach(plugin -> plugin.process(pkgNode));

        // Visit all the imported packages
        pkgNode.imports.forEach(importPkg -> importPkg.accept(this));

        // Then visit each top-level element sorted using the compilation unit
        pkgNode.topLevelNodes.forEach(topLevelNode -> ((BLangNode) topLevelNode).accept(this));

        pkgNode.completedPhases.add(CompilerPhase.COMPILER_PLUGIN);
    }

    public void visit(BLangAnnotation annotationNode) {
        List<BLangAnnotationAttachment> attachmentList = annotationNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(annotationNode, list));
    }

    public void visit(BLangConnector connectorNode) {
        List<BLangAnnotationAttachment> attachmentList = connectorNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(connectorNode, list));
        connectorNode.actions.forEach(action -> action.accept(this));
        connectorNode.endpoints.forEach(endpoint -> endpoint.accept(this));
    }

    public void visit(BLangEnum enumNode) {
        List<BLangAnnotationAttachment> attachmentList = enumNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(enumNode, list));
    }

    public void visit(BLangFunction funcNode) {
        List<BLangAnnotationAttachment> attachmentList = funcNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(funcNode, list));
        funcNode.endpoints.forEach(endpoint -> endpoint.accept(this));
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(pkgSymbol);
        if (pkgEnv == null) {
            return;
        }

        pkgEnv.node.accept(this);
    }

    public void visit(BLangPackageDeclaration pkgDclNode) {
    }

    public void visit(BLangService serviceNode) {
        List<BLangAnnotationAttachment> attachmentList = serviceNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(serviceNode, list));
        notifyEndpointProcessors(serviceNode.endpointType, attachmentList,
                (processor, list) -> processor.process(serviceNode, list));
        serviceNode.resources.forEach(resource -> resource.accept(this));
        serviceNode.endpoints.forEach(endpoint -> endpoint.accept(this));
    }

    public void visit(BLangStruct structNode) {
        List<BLangAnnotationAttachment> attachmentList = structNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(structNode, list));
    }

    public void visit(BLangObject objectNode) {
        List<BLangAnnotationAttachment> attachmentList = objectNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(objectNode, list));
    }

    public void visit(BLangRecord recordNode) {
        List<BLangAnnotationAttachment> attachmentList = recordNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(recordNode, list));
    }

    public void visit(BLangTypeDefinition typeDefinition) {
        //TODO
    }

    public void visit(BLangVariable varNode) {
        List<BLangAnnotationAttachment> attachmentList = varNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(varNode, list));
    }

    public void visit(BLangXMLNS xmlnsNode) {
    }

    public void visit(BLangTransformer transformerNode) {
        List<BLangAnnotationAttachment> attachmentList = transformerNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(transformerNode, list));
    }

    public void visit(BLangResource resourceNode) {
        List<BLangAnnotationAttachment> attachmentList = resourceNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(resourceNode, list));
        resourceNode.endpoints.forEach(endpoint -> endpoint.accept(this));
    }

    public void visit(BLangAction actionNode) {
        List<BLangAnnotationAttachment> attachmentList = actionNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(actionNode, list));
        actionNode.endpoints.forEach(endpoint -> endpoint.accept(this));
    }

    public void visit(BLangEndpoint endpointNode) {
        List<BLangAnnotationAttachment> attachmentList = endpointNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(endpointNode, list));
        notifyEndpointProcessors(endpointNode.symbol.type, attachmentList,
                (processor, list) -> processor.process(endpointNode, list));
    }

    public void visit(BLangForever foreverStatement) {
        /* ignore */
    }

    // private methods

    private void loadPlugins() {
        ServiceLoader<CompilerPlugin> pluginLoader = ServiceLoader.load(CompilerPlugin.class);
        pluginLoader.forEach(this::initPlugin);
    }

    private void initPlugin(CompilerPlugin plugin) {
        // Cache the plugin implementation class
        pluginList.add(plugin);

        handleAnnotationProcesses(plugin);
        handleEndpointProcesses(plugin);

        plugin.init(dlog);
    }

    private void handleAnnotationProcesses(CompilerPlugin plugin) {
        // Get the list of packages of annotations that this particular compiler plugin is interested in.
        SupportedAnnotationPackages supportedAnnotationPackages =
                plugin.getClass().getAnnotation(SupportedAnnotationPackages.class);
        if (supportedAnnotationPackages == null) {
            return;
        }

        String[] annotationPkgs = supportedAnnotationPackages.value();
        if (annotationPkgs.length == 0) {
            return;
        }

        for (String annPackage : annotationPkgs) {
            // Check whether each annotation type definition is available in the AST.
            List<BAnnotationSymbol> annotationSymbols = getAnnotationSymbols(annPackage);
            annotationSymbols.forEach(annSymbol -> {
                DefinitionID definitionID = new DefinitionID(annSymbol.pkgID.name.value, annSymbol.name.value);
                List<CompilerPlugin> processorList = processorMap.computeIfAbsent(
                        definitionID, k -> new ArrayList<>());
                processorList.add(plugin);
            });
        }
    }

    private List<BAnnotationSymbol> getAnnotationSymbols(String annPackage) {
        List<BAnnotationSymbol> annotationSymbols = new ArrayList<>();

        BLangPackage pkgNode = this.packageCache.get(annPackage);
        if (pkgNode == null) {
            return annotationSymbols;
        }
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(pkgNode.symbol);
        if (pkgEnv != null) {
            for (BLangAnnotation annotationNode : pkgEnv.enclPkg.annotations) {
                annotationSymbols.add((BAnnotationSymbol) annotationNode.symbol);
            }
        }
        return annotationSymbols;
    }

    private void notifyProcessors(List<BLangAnnotationAttachment> attachments,
                                  BiConsumer<CompilerPlugin, List<AnnotationAttachmentNode>> notifier) {

        Map<CompilerPlugin, List<AnnotationAttachmentNode>> attachmentMap = new HashMap<>();

        for (BLangAnnotationAttachment attachment : attachments) {
            DefinitionID aID = new DefinitionID(attachment.annotationSymbol.pkgID.getName().value,
                    attachment.annotationName.value);
            if (!processorMap.containsKey(aID)) {
                continue;
            }

            List<CompilerPlugin> procList = processorMap.get(aID);
            procList.forEach(proc -> {
                List<AnnotationAttachmentNode> attachmentNodes =
                        attachmentMap.computeIfAbsent(proc, k -> new ArrayList<>());
                attachmentNodes.add(attachment);
            });
        }


        for (CompilerPlugin processor : attachmentMap.keySet()) {
            notifier.accept(processor, Collections.unmodifiableList(attachmentMap.get(processor)));
        }
    }

    private void handleEndpointProcesses(CompilerPlugin plugin) {
        // Get the list of endpoint of that this particular compiler plugin is interested in.
        SupportEndpointTypes supportEndpointTypes = plugin.getClass().getAnnotation(SupportEndpointTypes.class);
        if (supportEndpointTypes == null) {
            return;
        }
        final SupportEndpointTypes.EndpointType[] endpointTypes = supportEndpointTypes.value();
        if (endpointTypes.length == 0) {
            return;
        }
        DefinitionID[] definitions = Arrays.stream(endpointTypes)
                .map(endpointType -> new DefinitionID(endpointType.packageName(), endpointType.name()))
                .toArray(DefinitionID[]::new);
        for (DefinitionID definitionID : definitions) {
            if (isValidEndpoints(definitionID, plugin)) {
                List<CompilerPlugin> processorList = endpointProcessorMap.computeIfAbsent(
                        definitionID, k -> new ArrayList<>());
                processorList.add(plugin);
            }
        }
    }

    private boolean isValidEndpoints(DefinitionID endpoint, CompilerPlugin plugin) {
        PackageID pkdID = new PackageID(Names.ANON_ORG, names.fromString(endpoint.pkgName), Names.EMPTY);
        BLangPackage pkgNode = this.packageCache.get(pkdID);
        if (pkgNode == null) {
            return false;
        }
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(pkgNode.symbol);
        final BSymbol bSymbol = symResolver.lookupSymbol(pkgEnv, names.fromString(endpoint.name), SymTag.VARIABLE_NAME);
        return bSymbol != symTable.notFoundSymbol;
    }

    private void notifyEndpointProcessors(BType endpointType, List<BLangAnnotationAttachment> attachments,
                                          BiConsumer<CompilerPlugin, List<AnnotationAttachmentNode>> notifier) {
        DefinitionID endpointID = new DefinitionID(endpointType.tsymbol.pkgID.name.value,
                endpointType.tsymbol.name.value);
        final List<CompilerPlugin> compilerPlugins = endpointProcessorMap.get(endpointID);
        if (compilerPlugins == null) {
            return;
        }
        compilerPlugins.forEach(proc -> notifier.accept(proc, Collections.unmodifiableList(attachments)));
    }

    /**
     * This class is gives a convenient way to represent both package name and the name of a definition.
     * (i.e annotation, endpoint, struct, etc.)
     *
     * @since 0.962.0
     */
    private static class DefinitionID {
        String pkgName;
        String name;

        DefinitionID(String pkgName, String name) {
            this.pkgName = pkgName;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }

            DefinitionID that = (DefinitionID) o;
            return Objects.equals(pkgName, that.pkgName) &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pkgName, name);
        }
    }
}
