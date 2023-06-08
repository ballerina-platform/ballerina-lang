/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.packaging.converters;

import org.wso2.ballerinalang.util.TomlParserUtils;

import java.net.Authenticator;
import java.net.PasswordAuthentication;


/**
 * Authenticator for the proxy server if provided.
 */
public class RemoteAuthenticator extends Authenticator {
    io.ballerina.projects.internal.model.Proxy proxy;
    public RemoteAuthenticator() {
        proxy = TomlParserUtils.readSettings().getProxy();
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return (new PasswordAuthentication(this.proxy.username(), this.proxy.password().toCharArray()));
    }
}

