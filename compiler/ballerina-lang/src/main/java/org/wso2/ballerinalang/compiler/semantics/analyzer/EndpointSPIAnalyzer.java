/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConnectorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BConnectorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.util.Names.EP_SPI_GET_CONNECTOR;
import static org.wso2.ballerinalang.compiler.util.Names.EP_SPI_INIT;
import static org.wso2.ballerinalang.compiler.util.Names.EP_SPI_REGISTER;
import static org.wso2.ballerinalang.compiler.util.Names.EP_SPI_START;
import static org.wso2.ballerinalang.compiler.util.Names.EP_SPI_STOP;

/**
 * Analyzer for validating endpoint SPI.
 *
 * @since 0.965.0
 */
public class EndpointSPIAnalyzer {

    private static final List<Name> EP_SPI_FUNCTIONS =
            Lists.of(EP_SPI_INIT, EP_SPI_REGISTER, EP_SPI_GET_CONNECTOR, EP_SPI_START, EP_SPI_STOP);

    private static final CompilerContext.Key<EndpointSPIAnalyzer> ENDPOINT_SPI_ANALYZER_KEY =
            new CompilerContext.Key<>();

    public static EndpointSPIAnalyzer getInstance(CompilerContext context) {
        EndpointSPIAnalyzer spiAnalyzer = context.get(ENDPOINT_SPI_ANALYZER_KEY);
        if (spiAnalyzer == null) {
            spiAnalyzer = new EndpointSPIAnalyzer(context);
        }
        return spiAnalyzer;
    }

    private SymbolTable symTable;
    private BLangDiagnosticLog dlog;

    // Cached Valid SPIs.
    private Map<BStructSymbol, EndPoint> validSPIs = new HashMap<>();
    private Map<BStructSymbol, EndPoint> invalidSPIs = new HashMap<>();

    private EndpointSPIAnalyzer(CompilerContext context) {
        context.put(ENDPOINT_SPI_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public void resolveEndpointSymbol(DiagnosticPos pos, BEndpointVarSymbol endpointVarSymbol) {
        if (endpointVarSymbol == null) {
            dlog.error(pos, DiagnosticCode.ENDPOINT_INVALID_TYPE, "");
            return;
        }
        if (isValidEndpointType(pos, endpointVarSymbol.type)) {
            final BStructSymbol tsymbol = (BStructSymbol) endpointVarSymbol.type.tsymbol;
            final EndPoint endPoint = validSPIs.get(tsymbol);
            final BStructSymbol.BAttachedFunction getConnector = endPoint.attachedFunctionMap.get(EP_SPI_GET_CONNECTOR);
            endpointVarSymbol.attachedConnector = (BConnectorSymbol)
                    ((BConnectorType) getConnector.type.retTypes.get(0)).tsymbol;
        }
    }

    public boolean isValidEndpointType(DiagnosticPos pos, BType type) {
        if (type.tag != TypeTags.STRUCT) {
            dlog.error(pos, DiagnosticCode.ENDPOINT_STRUCT_TYPE_REQUIRED);
            return false;
        }
        return isValidateEndpointSPI(pos, (BStructSymbol) type.tsymbol);
    }

    private boolean isValidateEndpointSPI(DiagnosticPos pos, BStructSymbol structSymbol) {
        if (isProcessedValidEndpoint(structSymbol)) {
            return true;
        }
        if (isProcessedInvalidEndpoint(structSymbol)) {
            // Check for cached values, to avoid duplication of error messages.
            dlog.error(pos, DiagnosticCode.ENDPOINT_INVALID_TYPE, structSymbol);
            return false;
        }
        EndPoint ep = new EndPoint(structSymbol);
        funNameLoop:
        for (Name funName : EP_SPI_FUNCTIONS) {
            for (BStructSymbol.BAttachedFunction attachedFunc : structSymbol.attachedFuncs) {
                if (funName.equals(attachedFunc.funcName)) {
                    ep.attachedFunctionMap.put(funName, attachedFunc);
                    continue funNameLoop;
                }
            }
            dlog.error(pos, DiagnosticCode.ENDPOINT_INVALID_TYPE, structSymbol);
            invalidSPIs.put(structSymbol, ep);
            return false;
        }
        if (!isValidSPIFunctionSignatures(pos, ep)) {
            invalidSPIs.put(structSymbol, ep);
            return false;
        }
        validSPIs.put(structSymbol, ep);
        return true;
    }

    private boolean isValidSPIFunctionSignatures(DiagnosticPos pos, EndPoint ep) {
        boolean isValid = true;
        // validate init function.
        final BStructSymbol.BAttachedFunction init = ep.attachedFunctionMap.get(EP_SPI_INIT);
        if (init.type.getParameterTypes().size() != 2 || init.type.retTypes.size() != 0
                || init.type.getReceiverType() != ep.structSymbol.type
                || init.type.getParameterTypes().get(0) != symTable.stringType
                || init.type.getParameterTypes().get(1).tag != TypeTags.STRUCT) {
            isValid = false;
            dlog.error(pos, DiagnosticCode.ENDPOINT_SPI_INVALID_FUNCTION, ep.structSymbol, EP_SPI_INIT);
        }
        // validate register function
        final BStructSymbol.BAttachedFunction register = ep.attachedFunctionMap.get(EP_SPI_REGISTER);
        if (register.type.getParameterTypes().size() != 1 || register.type.retTypes.size() != 0
                || register.type.getReceiverType() != ep.structSymbol.type
                || register.type.getParameterTypes().get(0).tag != TypeTags.TYPE) {
            isValid = false;
            dlog.error(pos, DiagnosticCode.ENDPOINT_SPI_INVALID_FUNCTION, ep.structSymbol, EP_SPI_REGISTER);
        }
        // validate start function
        final BStructSymbol.BAttachedFunction start = ep.attachedFunctionMap.get(EP_SPI_START);
        if (start.type.getParameterTypes().size() != 0 || start.type.retTypes.size() != 0
                || start.type.getReceiverType() != ep.structSymbol.type) {
            isValid = false;
            dlog.error(pos, DiagnosticCode.ENDPOINT_SPI_INVALID_FUNCTION, ep.structSymbol, EP_SPI_START);
        }
        // validate stop function
        final BStructSymbol.BAttachedFunction stop = ep.attachedFunctionMap.get(EP_SPI_STOP);
        if (stop.type.getParameterTypes().size() != 0 || stop.type.retTypes.size() != 0
                || stop.type.getReceiverType() != ep.structSymbol.type) {
            isValid = false;
            dlog.error(pos, DiagnosticCode.ENDPOINT_SPI_INVALID_FUNCTION, ep.structSymbol, EP_SPI_STOP);
        }
        // validate getConnector function
        final BStructSymbol.BAttachedFunction getConnector = ep.attachedFunctionMap.get(EP_SPI_GET_CONNECTOR);
        if (getConnector.type.getParameterTypes().size() != 0 || getConnector.type.retTypes.size() != 1
                || getConnector.type.getReceiverType() != ep.structSymbol.type
                || getConnector.type.retTypes.get(0).tag != TypeTags.CONNECTOR) {
            isValid = false;
            dlog.error(pos, DiagnosticCode.ENDPOINT_SPI_INVALID_FUNCTION, ep.structSymbol, EP_SPI_GET_CONNECTOR);
        }
        return isValid;
    }

    BType getEndpointConfigType(BStructSymbol structSymbol) {
        if (!isProcessedValidEndpoint(structSymbol)) {
            return symTable.errType;
        }
        final BStructSymbol.BAttachedFunction bAttachedFunction =
                validSPIs.get(structSymbol).attachedFunctionMap.get(EP_SPI_INIT);
        return bAttachedFunction.symbol.getParameters().get(1).type;
    }

    private boolean isProcessedInvalidEndpoint(BStructSymbol structSymbol) {
        return invalidSPIs.containsKey(structSymbol);
    }

    private boolean isProcessedValidEndpoint(BStructSymbol structSymbol) {
        return validSPIs.containsKey(structSymbol);
    }

    /**
     * Temporary data holder to hold endpoint related validation.
     *
     * @since 0.965.0
     */
    private static class EndPoint {
        BStructSymbol structSymbol;
        Map<Name, BStructSymbol.BAttachedFunction> attachedFunctionMap = new HashMap<>();

        EndPoint(BStructSymbol structSymbol) {
            this.structSymbol = structSymbol;
        }
    }
}
