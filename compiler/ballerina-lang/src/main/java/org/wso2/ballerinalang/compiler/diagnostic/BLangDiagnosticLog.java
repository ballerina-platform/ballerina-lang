/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.diagnostic;

import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.diagnostic.properties.BCollectionProperty;
import org.wso2.ballerinalang.compiler.diagnostic.properties.BNumericProperty;
import org.wso2.ballerinalang.compiler.diagnostic.properties.BStringProperty;
import org.wso2.ballerinalang.compiler.diagnostic.properties.BSymbolicProperty;
import org.wso2.ballerinalang.compiler.diagnostic.properties.NonCatProperty;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Logger class for logging various compiler diagnostics.
 * 
 * @since 2.0.0
 */
public class BLangDiagnosticLog implements DiagnosticLog {

    private static final CompilerContext.Key<BLangDiagnosticLog> DIAGNOSTIC_LOG_KEY = new CompilerContext.Key<>();
    private static final String ERROR_PREFIX = "error";
    private static final String WARNING_PREFIX = "warning";
    private static final String NOTE_PREFIX = "note";
    private static final String HINT_PREFIX = "hint";
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("compiler", Locale.getDefault());

    private int errorCount = 0;
    private PackageCache packageCache;
    private TypesFactory typesFactory;
    private SymbolFactory symbolFactory;
    private PackageID currentPackageId;
    private boolean isMute = false;

    private BLangDiagnosticLog(CompilerContext context) {
        context.put(DIAGNOSTIC_LOG_KEY, this);
        this.packageCache = PackageCache.getInstance(context);
        this.typesFactory = TypesFactory.getInstance(context);
        this.symbolFactory = SymbolFactory.getInstance(context);
    }

    public static BLangDiagnosticLog getInstance(CompilerContext context) {
        BLangDiagnosticLog dLogger = context.get(DIAGNOSTIC_LOG_KEY);
        if (dLogger == null) {
            dLogger = new BLangDiagnosticLog(context);
        }

        return dLogger;
    }

    public void setCurrentPackageId(PackageID packageID) {
        this.currentPackageId = packageID;
    }

    /**
     * Log an error.
     *
     * @param location Location of the error in the source code.
     * @param code Error code
     * @param args Parameters associated with the error
     */
    public void error(Location location, DiagnosticCode code, Object... args) {
        String msg = formatMessage(ERROR_PREFIX, code, args);
        reportDiagnostic((ModuleDescriptor) null, code, location, msg, DiagnosticSeverity.ERROR, args);
    }

    /**
     * Log a warning.
     *
     * @param location Location of the warning in the source code.
     * @param code Error code
     * @param args Parameters associated with the error
     */
    public void warning(Location location, DiagnosticCode code, Object... args) {
        String msg = formatMessage(WARNING_PREFIX, code, args);
        reportDiagnostic((ModuleDescriptor) null, code, location, msg, DiagnosticSeverity.WARNING, args);
    }

    /**
     * Log an info.
     *
     * @param location Location of the info in the source code.
     * @param code Error code
     * @param args Parameters associated with the info
     */
    public void note(Location location, DiagnosticCode code, Object... args) {
        String msg = formatMessage(NOTE_PREFIX, code, args);
        reportDiagnostic((ModuleDescriptor) null, code, location, msg, DiagnosticSeverity.INFO, args);
    }

    /**
     * Log a hint.
     *
     * @param location Location of the hint in the source code.
     * @param code     Hint code
     * @param args     Parameters associated with the hint
     */
    public void hint(Location location, DiagnosticCode code, Object... args) {
        String msg = formatMessage(HINT_PREFIX, code, args);
        reportDiagnostic((ModuleDescriptor) null, code, location, msg, DiagnosticSeverity.HINT, args);
    }

    /**
     * Get the number of error logged in this logger.
     *
     * @return Number of errors logged.
     */
    public int errorCount() {
        return this.errorCount;
    }

    /**
     * Set the error count.
     * 
     * @param errorCount Error count
     */
    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    /**
     * Reset error count.
     */
    public void resetErrorCount() {
        this.errorCount = 0;
    }

    /**
     * Mute the logger. This will stop reporting the diagnostic.
     * However it will continue to keep track of the number of errors.
     */
    public void mute() {
        this.isMute = true;
    }

    /**
     * Unmute the logger. This will start reporting the diagnostic.
     */
    public void unmute() {
        this.isMute = false;
    }

    @Override
    @Deprecated
    public void logDiagnostic(DiagnosticSeverity severity, Location location, CharSequence message) {
        logDiagnostic(severity, (ModuleDescriptor) null, location, message);
    }

    @Override
    @Deprecated
    public void logDiagnostic(DiagnosticSeverity severity, PackageID pkgId, Location location, CharSequence message) {
        reportDiagnostic(pkgId, null, location, message.toString(), severity, new Object[] {});
    }

    @Override
    public void logDiagnostic(DiagnosticSeverity severity,
                       ModuleDescriptor moduleDescriptor,
                       Location location,
                       CharSequence message) {
        reportDiagnostic(moduleDescriptor, null, location, message.toString(), severity, new Object[] {});
    }

    /**
     * Report a diagnostic for a given package.
     * 
     * @param pkgId Package ID of the diagnostic associated with
     * @param diagnostic the diagnostic to be logged
     */
    public void logDiagnostic(PackageID pkgId, Diagnostic diagnostic) {
        if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
            this.errorCount++;
        }

        storeDiagnosticInModule(pkgId, diagnostic);
    }

    // private helper methods

    private String formatMessage(String prefix, DiagnosticCode code, Object[] args) {
        String msgKey = MESSAGES.getString(prefix + "." + code.messageKey());
        return MessageFormat.format(msgKey, args);
    }

    private void reportDiagnostic(ModuleDescriptor moduleDescriptor, DiagnosticCode diagnosticCode, Location location,
                                  String msg, DiagnosticSeverity severity, Object[] args) {
        PackageID pkgId = null;
        if (moduleDescriptor != null) {
            pkgId = new PackageID(new Name(moduleDescriptor.org().value()),
                                  new Name(moduleDescriptor.name().toString()),
                                  new Name(moduleDescriptor.version().toString()));
        }
        reportDiagnostic(pkgId, diagnosticCode, location, msg, severity, args);
    }

    private void reportDiagnostic(PackageID packageID, DiagnosticCode diagnosticCode, Location location,
                                  String msg, DiagnosticSeverity severity, Object[] args) {
        if (severity == DiagnosticSeverity.ERROR) {
            this.errorCount++;
        }

        if (this.isMute) {
            return;
        }

        DiagnosticInfo diagInfo;
        if (diagnosticCode != null) {
            diagInfo = new DiagnosticInfo(diagnosticCode.diagnosticId(), diagnosticCode.messageKey(),
                                                         diagnosticCode.severity());
        } else {
            diagInfo = new DiagnosticInfo(null, msg, severity);
        }

        List<DiagnosticProperty<?>> argList = convertDiagArgsToProps(args);
        BLangDiagnostic diagnostic = new BLangDiagnostic(location, msg, diagInfo, diagnosticCode, argList);

        if (packageID != null) {
            storeDiagnosticInModule(packageID, diagnostic);
        } else {
            storeDiagnosticInModule(currentPackageId, diagnostic);
        }
    }

    private List<DiagnosticProperty<?>> convertDiagArgsToProps(Object[] args) {
        List<DiagnosticProperty<?>> diagArgs = new ArrayList<>();
        for (Object arg : args) {
            DiagnosticProperty<?> dArg;
            if (arg instanceof BType bType) {
                TypeSymbol tsybol = typesFactory.getTypeDescriptor(bType);
                dArg = new BSymbolicProperty(tsybol);
            } else if (arg instanceof BSymbol symbolArg) {
                Symbol symbol = symbolFactory.getBCompiledSymbol(symbolArg, symbolArg.getOriginalName().getValue());
                dArg = new BSymbolicProperty(symbol);
            } else if (arg instanceof String s) {
                dArg = new BStringProperty(s);
            } else if (arg instanceof Number n) {
                dArg = new BNumericProperty(n);
            } else if (arg instanceof Collection<?> collection) {
                Collection<DiagnosticProperty<?>> diagProperties
                        = convertDiagArgsToProps(collection.toArray());
                dArg = new BCollectionProperty(diagProperties);
            } else {
                dArg = new NonCatProperty(arg);
            }
            diagArgs.add(dArg);
        }
        return diagArgs;
    }

    private void storeDiagnosticInModule(PackageID pkgId, Diagnostic diagnostic) {
        BLangPackage pkgNode = this.packageCache.get(pkgId);
        pkgNode.addDiagnostic(diagnostic);
    }
}
