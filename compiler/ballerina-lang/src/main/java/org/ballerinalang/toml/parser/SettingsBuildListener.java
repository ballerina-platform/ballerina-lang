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
import org.ballerinalang.toml.model.Repositories;
import org.ballerinalang.toml.model.Repository;
import org.ballerinalang.toml.model.Settings;
import org.ballerinalang.toml.model.fields.CentralField;
import org.ballerinalang.toml.model.fields.ProxyField;
import org.ballerinalang.toml.model.fields.RepositoryField;
import org.ballerinalang.toml.model.fields.RepositoryHeader;
import org.ballerinalang.toml.model.fields.SettingHeaders;
import org.ballerinalang.toml.util.SingletonStack;
import org.ballerinalang.toml.util.TomlProcessor;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Custom listener which is extended from the Toml listener with our own custom logic.
 *
 * @since 0.964
 */
public class SettingsBuildListener extends TomlBaseListener {
    private final Settings settings;
    private final Proxy proxy = new Proxy();
    private final Central central = new Central();
    private final Repositories repositories = new Repositories();
    private final SingletonStack<String> currentKey = new SingletonStack<>();
    private Repository repository;
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
    public void enterArray(TomlParser.ArrayContext ctx) {
        setToManifest(ctx.arrayValues());
    }

    /**
     * Add array elements to manifest object.
     *
     * @param arrayValuesContext ArrayValuesContext object
     */
    private void setToManifest(TomlParser.ArrayValuesContext arrayValuesContext) {
        if (currentKey.present() && SettingHeaders.REPOSITORIES.stringEquals(currentHeader)) {
            RepositoryHeader packageFieldField = RepositoryHeader.valueOfLowerCase(currentKey.pop());
            List<String> arrayElements = Collections.singletonList("central");
            if (packageFieldField != null) {
                arrayElements = TomlProcessor.populateList(arrayValuesContext);
                packageFieldField.setListTo(repositories, arrayElements);
            } else {
                repositories.setRepoOrder(arrayElements);
            }
        }
    }

    @Override
    public void enterStdTable(TomlParser.StdTableContext ctx) {
        addHeader(TomlProcessor.getTableHeading(ctx));
    }

    /**
     * Add the dependencies and patches to the manifest object.
     */
    private void setSettingObj() {
        if (SettingHeaders.CENTRAL.stringEquals(currentHeader)) {
            this.settings.setCentral(central);
        } else if (SettingHeaders.PROXY.stringEquals(currentHeader)) {
            this.settings.setProxy(proxy);
        } else if (SettingHeaders.REPOSITORIES.stringEquals(currentHeader)) {
            this.repositories.addRepositories(repository);
            this.settings.setRepos(repositories);
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
            } else if (SettingHeaders.REPOSITORIES.stringEquals(currentHeader)) {
                RepositoryField centralField = RepositoryField.valueOfLowerCase(currentKey.pop());
                if (centralField != null) {
                    centralField.setValueTo(repository, value);
                }
            }
        }
    }

    /**
     * Add table headers in the toml file.
     *
     * @param key key specified in the header
     */
    private void addHeader(List<String> key) {
        currentHeader = key.get(0);
        if (key.size() > 1) {
            StringJoiner joiner = new StringJoiner(".");
            for (int i = 1; i < key.size(); i++) {
                joiner.add(key.get(i));
            }
            createRepositoryObject(joiner.toString());
        }
    }

    /**
     * Create dependency object and set the name.
     *
     * @param packageName pkg name of the dependency
     */
    private void createRepositoryObject(String packageName) {
        repository = new Repository();
        RepositoryField repositoryField = RepositoryField.NAME;
        repositoryField.setValueTo(repository, packageName);
    }
}
