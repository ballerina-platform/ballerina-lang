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
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.spi.EmbeddedExecutor;
import org.ballerinalang.util.EmbeddedExecutorError;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

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
    public Optional<EmbeddedExecutorError> executeFunction(String programArg, String... args) {
        URL resource = BVMEmbeddedExecutor.class.getClassLoader().getResource("META-INF/ballerina/" + programArg);
        if (resource == null) {
            throw new BLangCompilerException("missing internal modules when executing");
        }
        
        try {
            URI balxResource = resource.toURI();
            BValue returns = ExecutorUtils.executeFunction(balxResource, args);
            // Check if the return is an error
            if (returns instanceof BError) {
                BError bError = (BError) returns;
                return Optional.of(createEmbeddedExecutorError(bError));
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
     * @param bError The error from the execution.
     * @return Created embedded executor error.
     */
    private EmbeddedExecutorError createEmbeddedExecutorError(BError bError) {
        EmbeddedExecutorError error = new EmbeddedExecutorError();
        error.setMessage(bError.getReason());
        BError cause = bError.cause;
        if (cause != null) {
            error.setCause(createEmbeddedExecutorError(cause));
        }
        return error;
    }
}
