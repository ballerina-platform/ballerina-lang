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
package org.ballerinalang.toml.parser;

import org.ballerinalang.toml.antlr4.TomlBaseListener;
import org.ballerinalang.toml.antlr4.TomlParser;
import org.ballerinalang.toml.model.Proxy;
import org.ballerinalang.toml.model.fields.ProxyField;
import org.ballerinalang.toml.model.fields.Section;
import org.ballerinalang.toml.util.SingletonStack;

/**
 * Custom listener which is extended from the Toml listener with our own custom logic.
 *
 * @since 0.964
 */
public class ProxyBuildListener extends TomlBaseListener {
    private final Proxy proxy;
    private final SingletonStack<String> currentKey = new SingletonStack<>();
    private String currentHeader = null;

    /**
     * Constructor with the proxy object.
     *
     * @param proxy proxy object
     */
    ProxyBuildListener(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void enterKeyVal(TomlParser.KeyValContext ctx) {
        currentKey.push(ctx.key().getText());
    }

    @Override
    public void enterBasicStringValue(TomlParser.BasicStringValueContext ctx) {
        setToProxy(ctx.getText());
    }

    @Override
    public void enterStdTable(TomlParser.StdTableContext ctx) {
        addHeader(ctx.key().getText());
    }

    /**
     * Add table headers in the toml file.
     *
     * @param key key specified in the header
     */
    private void addHeader(String key) {
        currentHeader = key;
    }

    /**
     * Add the key-value pairs specified in the toml file.
     *
     * @param value KeyvalContext object
     */
    private void setToProxy(String value) {
        if (currentKey.present() && Section.PROXY.stringEquals(currentHeader)) {
            ProxyField proxyField = ProxyField.valueOfLowerCase(currentKey.pop());
            if (proxyField != null) {
                proxyField.setValueTo(this.proxy, value);
            }
        }
    }
}
