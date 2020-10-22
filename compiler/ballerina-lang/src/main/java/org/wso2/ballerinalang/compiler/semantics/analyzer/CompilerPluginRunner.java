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

import io.ballerina.runtime.util.RuntimeUtils;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.compiler.plugins.CompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    private final Types types;
    private BLangDiagnosticLog dlog;

    private DiagnosticPos defaultPos;
    private CompilerContext context;
    private List<CompilerPlugin> pluginList;
    private Map<DefinitionID, Set<CompilerPlugin>> processorMap;
    private Map<CompilerPlugin, List<DefinitionID>> resourceTypeProcessorMap;
    private Map<CompilerPlugin, BType> serviceListenerMap;
    private List<CompilerPlugin> failedPlugins;


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
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.context = context;

        this.pluginList = new ArrayList<>();
        this.processorMap = new HashMap<>();
        this.resourceTypeProcessorMap = new HashMap<>();
        this.serviceListenerMap = new HashMap<>();
        this.failedPlugins = new ArrayList<>();

        ServiceLoader<CompilerPlugin> pluginLoader = ServiceLoader.load(CompilerPlugin.class);
        pluginLoader.forEach(plugin -> pluginList.add(plugin));
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
        for (CompilerPlugin compilerPlugin : pluginList) {
            executePluginSafely(pkgNode, compilerPlugin, pkgNode.packageID, compilerPlugin::pluginExecutionStarted);
        }
        for (CompilerPlugin compilerPlugin : pluginList) {
            executePluginSafely(pkgNode, compilerPlugin, pkgNode, compilerPlugin::process);
        }

        // Then visit each top-level element sorted using the compilation unit
        for (TopLevelNode topLevelNode : pkgNode.topLevelNodes) {
            ((BLangNode) topLevelNode).accept(this);
        }

        pkgNode.getTestablePkgs().forEach(testablePackage -> {
            this.defaultPos = testablePackage.pos;
            visit(testablePackage);
        });
        for (CompilerPlugin plugin : pluginList) {
            executePluginSafely(pkgNode, plugin, pkgNode.packageID, plugin::pluginExecutionCompleted);
        }
        pkgNode.completedPhases.add(CompilerPhase.COMPILER_PLUGIN);
    }

    private <T> void executePluginSafely(BLangPackage pkgNode, CompilerPlugin plugin, T arg, Consumer<T> consumer) {
        if (failedPlugins.contains(plugin)) {
            return;
        }
        try {
            consumer.accept(arg);
        } catch (Throwable e) {
            dlog.warning(pkgNode.pos, DiagnosticCode.COMPILER_PLUGIN_ERROR);
            printErrorLog(e);
            failedPlugins.add(plugin);
        }
    }

    public void visit(BLangTestablePackage testablePkgNode) {
        if (testablePkgNode.completedPhases.contains(CompilerPhase.COMPILER_PLUGIN)) {
            return;
        }

        for (CompilerPlugin plugin : pluginList) {
            executePluginSafely(testablePkgNode, plugin, testablePkgNode, plugin::process);
        }

        // Then visit each top-level element sorted using the compilation unit
        for (TopLevelNode topLevelNode : testablePkgNode.topLevelNodes) {
            ((BLangNode) topLevelNode).accept(this);
        }
        testablePkgNode.completedPhases.add(CompilerPhase.COMPILER_PLUGIN);
    }

    public void visit(BLangAnnotation annotationNode) {
        List<BLangAnnotationAttachment> attachmentList = annotationNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(annotationNode, list));
    }

    public void visit(BLangFunction funcNode) {
        List<BLangAnnotationAttachment> attachmentList = funcNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(funcNode, list));
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(pkgSymbol);
        if (pkgEnv == null) {
            return;
        }

        pkgEnv.node.accept(this);
    }

    public void visit(BLangService serviceNode) {
        List<BLangAnnotationAttachment> attachmentList = serviceNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(serviceNode, list));
        notifyServiceTypeProcessors(serviceNode, attachmentList,
                (processor, list) -> processor.process(serviceNode, list));
    }

    public void visit(BLangTypeDefinition typeDefNode) {
        List<BLangAnnotationAttachment> attachmentList = typeDefNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(typeDefNode, list));
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        List<BLangAnnotationAttachment> attachmentList = classDefinition.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(classDefinition, list));
    }

    public void visit(BLangSimpleVariable varNode) {
        List<BLangAnnotationAttachment> attachmentList = varNode.getAnnotationAttachments();
        notifyProcessors(attachmentList, (processor, list) -> processor.process(varNode, list));
    }

    public void visit(BLangXMLNS xmlnsNode) {
    }

    public void visit(BLangConstant constant) {
        /* ignore */
    }

    public void visit(BLangLetExpression letExpression) {
    }

    // private methods

    private void loadPlugins() {
        pluginList.forEach(this::initPlugin);
    }

    private void initPlugin(CompilerPlugin plugin) {
        handleAnnotationProcesses(plugin);
        handleServiceTypeProcesses(plugin);
        plugin.setCompilerContext(context);
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
                Set<CompilerPlugin> processors = processorMap.computeIfAbsent(definitionID, k -> new HashSet<>());
                processors.add(plugin);
            });
        }
    }

    private List<BAnnotationSymbol> getAnnotationSymbols(String annPackage) {
        List<BAnnotationSymbol> annotationSymbols = new ArrayList<>();
        BPackageSymbol symbol = this.packageCache.getSymbol(annPackage);
        if (symbol == null) {
            return annotationSymbols;
        }
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(symbol);
        if (pkgEnv != null) {
            symbol.scope.entries.forEach((name, scope) -> {
                if (SymTag.ANNOTATION == scope.symbol.tag) {
                    annotationSymbols.add((BAnnotationSymbol) scope.symbol);
                }
            });
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

            for (CompilerPlugin proc : processorMap.get(aID)) {
                List<AnnotationAttachmentNode> attachmentNodes =
                        attachmentMap.computeIfAbsent(proc, k -> new ArrayList<>());
                attachmentNodes.add(attachment);
            }
        }


        for (CompilerPlugin processor : attachmentMap.keySet()) {
            if (failedPlugins.contains(processor)) {
                continue;
            }

            List<AnnotationAttachmentNode> list = attachmentMap.get(processor);
            try {
                notifier.accept(processor, Collections.unmodifiableList(list));
            } catch (Throwable e) {
                dlog.warning((DiagnosticPos) list.get(0).getPosition(), DiagnosticCode.COMPILER_PLUGIN_ERROR);
                printErrorLog(e);
                failedPlugins.add(processor);
            }
        }
    }

    private void printErrorLog(Throwable e) {
        RuntimeUtils.printCrashLog(e);
    }

    private void handleServiceTypeProcesses(CompilerPlugin plugin) {
        // Get the list of endpoint of that this particular compiler plugin is interested in.
        SupportedResourceParamTypes resParamTypes = plugin.getClass()
                .getAnnotation(SupportedResourceParamTypes.class);
        if (resParamTypes == null) {
            return;
        }
        final SupportedResourceParamTypes.Type[] supportedTypes = resParamTypes.paramTypes();
        if (supportedTypes.length == 0) {
            return;
        }
        // Set Listener type.
        BType listenerType = null;
        if (!resParamTypes.expectedListenerType().name().isEmpty() && !resParamTypes.expectedListenerType()
                .packageName().isEmpty()) {
            Name listenerName = names.fromString(resParamTypes.expectedListenerType().name());
            String packageQName =
                    resParamTypes.expectedListenerType().orgName() + "/" + resParamTypes.expectedListenerType()
                            .packageName();
            BPackageSymbol symbol = this.packageCache.getSymbol(packageQName);
            if (symbol != null) {
                SymbolEnv pkgEnv = symTable.pkgEnvMap.get(symbol);
                BSymbol listenerSymbol = symResolver.lookupSymbolInMainSpace(pkgEnv, listenerName);
                if ((listenerSymbol.tag & SymTag.OBJECT) == SymTag.OBJECT) {
                    listenerType = listenerSymbol.type;
                }
            }
        }

        List<DefinitionID> definitions = Arrays.stream(supportedTypes)
                .map(type -> new DefinitionID(type.packageName(), type.name())).collect(Collectors.toList());
        resourceTypeProcessorMap.put(plugin, definitions);
        serviceListenerMap.put(plugin, listenerType);
    }

    private void notifyServiceTypeProcessors(BLangService serviceNode, List<BLangAnnotationAttachment> attachments,
            BiConsumer<CompilerPlugin, List<AnnotationAttachmentNode>> notifier) {

        for (CompilerPlugin plugin : resourceTypeProcessorMap.keySet()) {
            boolean isCurrentPluginProcessed = false;
            BType listenerType;
            if ((listenerType = serviceListenerMap.get(plugin)) != null) {
                for (BLangExpression expr : serviceNode.getAttachedExprs()) {
                    if (!types.isSameType(expr.type, listenerType)) {
                        continue;
                    }
                    isCurrentPluginProcessed = true;
                    invokeServiceProcessor(serviceNode, attachments, notifier, plugin);
                    break;
                }
            }
            if (isCurrentPluginProcessed) {
                continue;
            }
            // Now look for resource parameters.
            for (DefinitionID definitionID : resourceTypeProcessorMap.get(plugin)) {
                for (FunctionNode function : serviceNode.getResources()) {
                    final BLangFunction resourceNode = (BLangFunction) function;
                    if (resourceNode.symbol.params.stream().filter(varSym -> varSym.type.tsymbol != null)
                            .map(varSym -> varSym.type.tsymbol).noneMatch(
                                    tsym -> definitionID.name.equals(tsym.name.value) && definitionID.pkgName
                                            .equals(tsym.pkgID.name.value))) {
                        continue;
                    }
                    isCurrentPluginProcessed = true;
                    invokeServiceProcessor(serviceNode, attachments, notifier, plugin);
                    break;
                }
                // We have to invoke the plugin one time per service, if only there is one matching service.
                if (isCurrentPluginProcessed) {
                    break;
                }
            }
        }
    }

    private void invokeServiceProcessor(BLangService serviceNode, List<BLangAnnotationAttachment> attachments,
            BiConsumer<CompilerPlugin, List<AnnotationAttachmentNode>> notifier, CompilerPlugin plugin) {
        notifier.accept(plugin, Collections.unmodifiableList(attachments));
        // Hacking till we figure out service type.
        if (serviceNode.listenerType == null) {
            serviceNode.listenerType = serviceListenerMap.get(plugin);
        }
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

        public String getName() {
            return name;
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
