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
import org.ballerinalang.toml.model.Central;
import org.ballerinalang.toml.model.Proxy;
import org.ballerinalang.toml.model.Settings;
import org.ballerinalang.toml.model.fields.CentralField;
import org.ballerinalang.toml.model.fields.ProxyField;
import org.ballerinalang.toml.model.fields.SettingHeaders;
import org.ballerinalang.toml.util.SingletonStack;

/**
 * Custom listener which is extended from the Toml listener with our own custom logic.
 *
 * @since 0.964
 */
public class SettingsBuildListener extends TomlBaseListener {
    private final Settings settings;
    private final Proxy proxy = new Proxy();
    private final Central central = new Central();
    private final SingletonStack<String> currentKey = new SingletonStack<>();
    private String currentHeader = null;

    /**
     * Cosntructor with the settings object.
     *
     * @param settings object
     */
    SettingsBuildListener(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void enterKeyVal(TomlParser.KeyValContext ctx) {
        currentKey.push(ctx.key().getText());
    }

    @Override
    public void enterBasicStringValue(TomlParser.BasicStringValueContext ctx) {
        setToManifest(ctx.getText());
    }

    @Override
    public void exitKeyVal(TomlParser.KeyValContext ctx) {
        setSettingObj();
    }

    @Override
    public void enterStdTable(TomlParser.StdTableContext ctx) {
        addHeader(ctx.key().getText());
    }

    /**
     * Add the dependencies and patches to the manifest object.
     */
    private void setSettingObj() {
        if (SettingHeaders.CENTRAL.stringEquals(currentHeader)) {
            this.settings.setCentral(central);
        } else if (SettingHeaders.PROXY.stringEquals(currentHeader)) {
            this.settings.setProxy(proxy);
        }
    }

    /**
     * Add the key-value pairs specified in the toml file.
     *
     * @param value KeyvalContext object
     */
    private void setToManifest(String value) {
        if (currentKey.present()) {
            if (SettingHeaders.PROXY.stringEquals(currentHeader)) {
                ProxyField proxyField = ProxyField.valueOfLowerCase(currentKey.pop());
                if (proxyField != null) {
                    proxyField.setValueTo(proxy, value);
                }
            } else if (SettingHeaders.CENTRAL.stringEquals(currentHeader)) {
                CentralField centralField = CentralField.valueOfLowerCase(currentKey.pop());
                if (centralField != null) {
                    centralField.setValueTo(central, value);
                }
            }
        }
    }

    /**
     * Add table headers in the toml file.
     *
     * @param key key specified in the header
     */
    private void addHeader(String key) {
        currentHeader = key;
    }
}
