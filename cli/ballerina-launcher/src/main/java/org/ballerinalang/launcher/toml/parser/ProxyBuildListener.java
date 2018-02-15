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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.launcher.toml.parser;

import org.ballerinalang.launcher.toml.antlr4.TomlBaseListener;
import org.ballerinalang.launcher.toml.antlr4.TomlParser;
import org.ballerinalang.launcher.toml.model.Proxy;
import org.ballerinalang.launcher.toml.model.fields.ProxyField;
import org.ballerinalang.launcher.toml.model.fields.Section;
import org.ballerinalang.launcher.toml.util.SingletonStack;

import java.util.List;

/**
 * Custom listener which is extended from the Toml listener with our own custom logic
 */
public class ProxyBuildListener extends TomlBaseListener {
    private final Proxy proxy;
    private String currentHeader = null;
    private SingletonStack currentKey = new SingletonStack();

    /**
     * Constructor with the proxy object
     *
     * @param proxy proxy object
     */
    ProxyBuildListener(Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void enterKeyval(TomlParser.KeyvalContext ctx) {
        currentKey.push(ctx.key().getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void enterString(TomlParser.StringContext ctx) {
        setToProxy(ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void enterStdTable(TomlParser.StdTableContext ctx) {
        addHeader(ctx.key());
    }

    /**
     * Add table headers in the toml file
     *
     * @param keyContextList list of keys specified in the header
     */
    private void addHeader(List<TomlParser.KeyContext> keyContextList) {
        currentHeader = keyContextList.get(0).getText();
    }

    /**
     * Add the key-value pairs specified in the toml file
     *
     * @param value KeyvalContext object
     */
    private void setToProxy(String value) {
        if (currentKey.hasKey() && Section.PROXY.stringEquals(currentHeader)) {
            ProxyField proxyField = ProxyField.lookup.get(currentKey.pop());
            if (proxyField != null) {
                proxyField.setValueTo(this.proxy, value);
            }
        }
    }
}
