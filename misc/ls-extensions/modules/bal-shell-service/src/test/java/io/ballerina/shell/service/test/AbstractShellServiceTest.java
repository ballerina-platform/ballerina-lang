/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.shell.service.test;

import io.ballerina.shell.service.ShellWrapper;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Base class for shell service tests.
 *
 * @since 2201.1.1
 */

public abstract class AbstractShellServiceTest {
    protected static final String GET_RESULT = "balShell/getResult";
    protected static final String NOTEBOOK_RESTART = "balShell/restartNotebook";
    protected static final String GET_VARIABLES = "balShell/getVariableValues";
    protected static final String DELETE_DCLNS = "balShell/deleteDeclarations";
    protected static final String GET_SHELL_FILE_SOURCE = "balShell/getShellFileSource";
    protected static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    protected Endpoint serviceEndpoint;

    @BeforeClass
    public void startLanguageServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @BeforeMethod
    public void restartShell() {
        ShellWrapper.getInstance().restart();
    }
}
