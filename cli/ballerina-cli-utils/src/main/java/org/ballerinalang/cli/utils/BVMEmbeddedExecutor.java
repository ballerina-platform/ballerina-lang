/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.cli.utils;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.spi.EmbeddedExecutor;
import org.ballerinalang.util.EmbeddedExecutorError;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import static org.ballerinalang.util.BLangConstants.COLON;
import static org.ballerinalang.util.BLangConstants.MAIN_FUNCTION_NAME;

/**
 * This represents the Ballerina module provider.
 *
 * @since 0.964
 */
@JavaSPIService("org.ballerinalang.spi.EmbeddedExecutor")
public class BVMEmbeddedExecutor implements EmbeddedExecutor {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<EmbeddedExecutorError> executeFunction(String programArg, String functionName, String... args) {
        if (functionName == null) {
            functionName = MAIN_FUNCTION_NAME;
        }
        String balxPath = programArg;
        
        if (programArg.contains(COLON)) {
            String[] programArgConstituents = programArg.split(COLON);
            functionName = programArgConstituents[programArgConstituents.length - 1];
            if (functionName.isEmpty() || programArg.endsWith(COLON)) {
                throw new BLangCompilerException("usage error: expected function name after final ':'");
            }
            balxPath = programArg.replace(COLON.concat(functionName), "");
        }
        
        URL resource = BVMEmbeddedExecutor.class.getClassLoader().getResource("META-INF/ballerina/" + balxPath);
        if (resource == null) {
            throw new BLangCompilerException("missing internal modules when executing");
        }
        
        try {
            URI balxResource = resource.toURI();
            BValue[] returns = ExecutorUtils.executeFunction(balxResource, functionName, args);
            if (returns.length == 1 && returns[0] instanceof BMap) {
                BMap<String, BValue> errorRecord = (BMap<String, BValue>) returns[0];
                return Optional.of(createEmbeddedExecutorError(errorRecord));
            } else {
                return Optional.empty();
            }
        } catch (URISyntaxException e) {
            throw new BLangCompilerException("error reading balx path in executor.");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void executeService(String balxPath) {
        URL resource = BVMEmbeddedExecutor.class.getClassLoader().getResource("META-INF/ballerina/" + balxPath);
        if (resource == null) {
            throw new BLangCompilerException("missing internal modules when executing");
        }
        
        try {
            URI balxResource = resource.toURI();
            ExecutorUtils.executeService(balxResource);
        } catch (URISyntaxException e) {
            throw new BLangCompilerException("error reading balx path in executor.");
        }
    }
    
    /**
     * Creates an error object for the embedded executor.
     * @param errorRecord The error record from the execution.
     * @return Created embedded executor error.
     */
    private EmbeddedExecutorError createEmbeddedExecutorError(BMap<String, BValue> errorRecord) {
        EmbeddedExecutorError error = new EmbeddedExecutorError();
        error.setMessage(errorRecord.get("message").stringValue());
        if (errorRecord.get("cause") != null) {
            BMap<String, BValue> innerError = (BMap<String, BValue>) errorRecord.get("cause");
            error.setCause(createEmbeddedExecutorError(innerError));
        }
        return error;
    }
}
