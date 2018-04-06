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

import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.util.Names.EP_SPI_GET_CLIENT;
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
    private Map<BStructSymbol, Endpoint> validSPIs = new HashMap<>();
    private Map<BStructSymbol, Endpoint> invalidSPIs = new HashMap<>();

    private EndpointSPIAnalyzer(CompilerContext context) {
        context.put(ENDPOINT_SPI_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public void resolveEndpointSymbol(BLangEndpoint endpoint) {
        if (endpoint.symbol == null) {
            dlog.error(endpoint.pos, DiagnosticCode.ENDPOINT_INVALID_TYPE, "");
            return;
        }
        isValidEndpointType(endpoint.pos, endpoint.symbol.type);
        // Update endpoint variable symbol
        if (endpoint.symbol.type.tsymbol.kind == SymbolKind.STRUCT) {
            populateEndpointSymbol((BStructSymbol) endpoint.symbol.type.tsymbol, endpoint.symbol);
        }
    }

    public BStructType getEndpointTypeFromServiceType(DiagnosticPos pos, BType type) {
        if (type.tag != TypeTags.STRUCT) {
            dlog.error(pos, DiagnosticCode.ENDPOINT_OBJECT_TYPE_REQUIRED);
            return null;
        }
        final BStructSymbol serviceType = (BStructSymbol) type.tsymbol;
        for (BStructSymbol.BAttachedFunction attachedFunc : serviceType.attachedFuncs) {
            if (Names.EP_SERVICE_GET_ENDPOINT.equals(attachedFunc.funcName)) {
                if (attachedFunc.type.getParameterTypes().size() != 0
                        || attachedFunc.type.retType.tag != TypeTags.STRUCT) {
                    dlog.error(pos, DiagnosticCode.SERVICE_INVALID_OBJECT_TYPE, serviceType);
                    return null;
                }
                final BStructSymbol endPointType = (BStructSymbol) attachedFunc.type.retType.tsymbol;
                if (isValidEndpointSPI(pos, endPointType)) {
                    return (BStructType) attachedFunc.type.retType;
                }
                break;
            }
        }
        dlog.error(pos, DiagnosticCode.SERVICE_INVALID_OBJECT_TYPE, serviceType);
        return null;
    }

    public BStructType getClientType(BStructSymbol endPointType) {
        final Endpoint endpoint = validSPIs.get(endPointType);
        return endpoint.clientStruct;
    }

    public boolean isValidEndpointType(DiagnosticPos pos, BType type) {
        if (type.tag != TypeTags.STRUCT) {
            dlog.error(pos, DiagnosticCode.ENDPOINT_OBJECT_TYPE_REQUIRED);
            return false;
        }
        return isValidEndpointSPI(pos, (BStructSymbol) type.tsymbol);
    }

    private boolean isValidEndpointSPI(DiagnosticPos pos, BStructSymbol structSymbol) {
        if (isProcessedValidEndpoint(structSymbol)) {
            return true;
        }
        if (isProcessedInvalidEndpoint(structSymbol)) {
            // Check for cached values, to avoid duplication of endpoint related error messages.
            dlog.error(pos, DiagnosticCode.ENDPOINT_INVALID_TYPE, structSymbol);
            return false;
        }
        Endpoint ep = new Endpoint(pos, structSymbol);
        processEndpointSPIFunctions(ep);
        // Check validity of endpoint interface.
        checkValidBaseEndpointSPI(ep);
        checkValidInteractableEndpoint(ep);
        checkValidStartableEndpoint(ep);
        checkValidStoppableEndpoint(ep);
        checkValidRegistrableEndpoint(ep);

        if (invalidSPIs.containsKey(ep.structSymbol)) {
            return false;
        }
        validSPIs.put(structSymbol, ep);
        return true;
    }

    private void processEndpointSPIFunctions(Endpoint ep) {
        for (BStructSymbol.BAttachedFunction attachedFunc : ep.structSymbol.attachedFuncs) {
            if (Names.EP_SPI_INIT.equals(attachedFunc.funcName)) {
                ep.attachedFunctionMap.put(Names.EP_SPI_INIT, attachedFunc);
            } else if (Names.EP_SPI_GET_CLIENT.equals(attachedFunc.funcName)) {
                ep.attachedFunctionMap.put(Names.EP_SPI_GET_CLIENT, attachedFunc);
            } else if (Names.EP_SPI_START.equals(attachedFunc.funcName)) {
                ep.attachedFunctionMap.put(Names.EP_SPI_START, attachedFunc);
            } else if (Names.EP_SPI_STOP.equals(attachedFunc.funcName)) {
                ep.attachedFunctionMap.put(Names.EP_SPI_STOP, attachedFunc);
            } else if (Names.EP_SPI_REGISTER.equals(attachedFunc.funcName)) {
                ep.attachedFunctionMap.put(Names.EP_SPI_REGISTER, attachedFunc);
            } else if (Names.OBJECT_INIT_SUFFIX.equals(attachedFunc.funcName)) {
                ep.attachedFunctionMap.put(Names.OBJECT_INIT_SUFFIX, attachedFunc);
            }
        }
    }

    private void checkValidBaseEndpointSPI(Endpoint ep) {
        if (ep.attachedFunctionMap.containsKey(Names.OBJECT_INIT_SUFFIX)) {
            final BStructSymbol.BAttachedFunction newFunc = ep.attachedFunctionMap.get(Names.OBJECT_INIT_SUFFIX);
            if (newFunc.symbol.getParameters().size() > 0) {
                dlog.error(ep.pos, DiagnosticCode.ENDPOINT_OBJECT_NEW_HAS_PARAM);
                invalidSPIs.putIfAbsent(ep.structSymbol, ep);
            }
        }

        if (!ep.attachedFunctionMap.containsKey(Names.EP_SPI_INIT)) {
            dlog.error(ep.pos, DiagnosticCode.ENDPOINT_INVALID_TYPE_NO_FUNCTION, ep.structSymbol, Names.EP_SPI_INIT);
            invalidSPIs.putIfAbsent(ep.structSymbol, ep);
            return;
        }
        // validate init function.
        final BStructSymbol.BAttachedFunction init = ep.attachedFunctionMap.get(EP_SPI_INIT);
        if (init.type.getParameterTypes().size() != 1 || init.type.retType != symTable.nilType
                || init.type.getParameterTypes().get(0).tag != TypeTags.STRUCT
                || ((BStructSymbol) init.type.getParameterTypes().get(0).tsymbol).isObject) {
            dlog.error(ep.pos, DiagnosticCode.ENDPOINT_SPI_INVALID_FUNCTION, ep.structSymbol, EP_SPI_INIT);
            invalidSPIs.putIfAbsent(ep.structSymbol, ep);
            return;
        }
        ep.initFunction = init.symbol;
        ep.endpointConfig = (BStructType) init.type.getParameterTypes().get(0);
    }

    private void checkValidInteractableEndpoint(Endpoint ep) {
        if (!ep.attachedFunctionMap.containsKey(Names.EP_SPI_GET_CLIENT)) {
            return;
        }
        // validate getClient function
        final BStructSymbol.BAttachedFunction getClient = ep.attachedFunctionMap.get(EP_SPI_GET_CLIENT);
        if (getClient.type.getParameterTypes().size() != 0
                || getClient.type.retType.tag != TypeTags.STRUCT) {
            dlog.error(ep.pos, DiagnosticCode.ENDPOINT_SPI_INVALID_FUNCTION, ep.structSymbol, EP_SPI_GET_CLIENT);
            invalidSPIs.putIfAbsent(ep.structSymbol, ep);
            return;
        }
        ep.interactable = true;
        ep.getClientFunction = getClient.symbol;
        ep.clientStruct = (BStructType) getClient.type.retType;
    }

    private void checkValidStartableEndpoint(Endpoint ep) {
        if (!ep.attachedFunctionMap.containsKey(Names.EP_SPI_START)) {
            return;
        }
        // validate start function
        final BStructSymbol.BAttachedFunction start = ep.attachedFunctionMap.get(EP_SPI_START);
        if (start.type.getParameterTypes().size() != 0 || start.type.retType != symTable.nilType) {
            dlog.error(ep.pos, DiagnosticCode.ENDPOINT_SPI_INVALID_FUNCTION, ep.structSymbol, EP_SPI_START);
            invalidSPIs.putIfAbsent(ep.structSymbol, ep);
            return;
        }
        ep.startFunction = start.symbol;
    }

    private void checkValidStoppableEndpoint(Endpoint ep) {
        if (!ep.attachedFunctionMap.containsKey(Names.EP_SPI_STOP)) {
            return;
        }
        // validate stop function
        final BStructSymbol.BAttachedFunction stop = ep.attachedFunctionMap.get(EP_SPI_STOP);
        if (stop.type.getParameterTypes().size() != 0 || stop.type.retType != symTable.nilType) {
            dlog.error(ep.pos, DiagnosticCode.ENDPOINT_SPI_INVALID_FUNCTION, ep.structSymbol, EP_SPI_STOP);
            invalidSPIs.putIfAbsent(ep.structSymbol, ep);
            return;
        }
        ep.stopFunction = stop.symbol;
    }

    private void checkValidRegistrableEndpoint(Endpoint ep) {
        if (!ep.attachedFunctionMap.containsKey(Names.EP_SPI_REGISTER)) {
            return;
        }
        // validate register function
        final BStructSymbol.BAttachedFunction register = ep.attachedFunctionMap.get(EP_SPI_REGISTER);
        if (register.type.getParameterTypes().size() != 1 || register.type.retType != symTable.nilType
                || register.type.getParameterTypes().get(0).tag != TypeTags.TYPEDESC) {
            dlog.error(ep.pos, DiagnosticCode.ENDPOINT_SPI_INVALID_FUNCTION, ep.structSymbol, EP_SPI_REGISTER);
            invalidSPIs.putIfAbsent(ep.structSymbol, ep);
            return;
        }
        ep.registrable = true;
        ep.registerFunction = register.symbol;
    }

    BType getEndpointConfigType(BStructSymbol structSymbol) {
        if (!isProcessedValidEndpoint(structSymbol)) {
            final Endpoint endpoint = invalidSPIs.get(structSymbol);
            if (endpoint != null && endpoint.initFunction != null && endpoint.initFunction.getParameters().size() > 0) {
                return endpoint.initFunction.getParameters().get(0).type;
            }
            return symTable.errType;
        }
        final Endpoint endpoint = validSPIs.get(structSymbol);
        return endpoint.initFunction.getParameters().get(0).type;
    }

    private boolean isProcessedInvalidEndpoint(BStructSymbol structSymbol) {
        return invalidSPIs.containsKey(structSymbol);
    }

    public boolean isProcessedValidEndpoint(BStructSymbol structSymbol) {
        return validSPIs.containsKey(structSymbol);
    }

    public void populateEndpointSymbol(BStructSymbol structSymbol, BEndpointVarSymbol endpointVarSymbol) {
        Endpoint endPoint = validSPIs.get(structSymbol);
        if (endPoint == null) {
            endPoint = invalidSPIs.get(structSymbol);
        }
        if (endPoint == null) {
            return;
        }
        endpointVarSymbol.initFunction = endPoint.initFunction;

        endpointVarSymbol.interactable = endPoint.interactable;
        endpointVarSymbol.getClientFunction = endPoint.getClientFunction;
        if (endPoint.clientStruct != null && endPoint.clientStruct.tag == TypeTags.STRUCT) {
            endpointVarSymbol.clientSymbol = (BStructSymbol) endPoint.clientStruct.tsymbol;
        }
        endpointVarSymbol.startFunction = endPoint.startFunction;
        endpointVarSymbol.stopFunction = endPoint.stopFunction;

        endpointVarSymbol.registrable = endPoint.registrable;
        endpointVarSymbol.registerFunction = endPoint.registerFunction;
    }

    /**
     * Temporary data holder to hold endpoint related validation.
     *
     * @since 0.965.0
     */
    public static class Endpoint {
        final DiagnosticPos pos;
        final BStructSymbol structSymbol;
        Map<Name, BStructSymbol.BAttachedFunction> attachedFunctionMap = new HashMap<>();

        BInvokableSymbol initFunction = null;
        // Make this record Literal.
        BStructType endpointConfig = null;

        boolean interactable;
        BInvokableSymbol getClientFunction = null;
        BStructType clientStruct = null;

        BInvokableSymbol startFunction = null;
        BInvokableSymbol stopFunction = null;

        boolean registrable;
        BInvokableSymbol registerFunction = null;

        Endpoint(DiagnosticPos pos, BStructSymbol structSymbol) {
            this.pos = pos;
            this.structSymbol = structSymbol;
        }
    }
}
