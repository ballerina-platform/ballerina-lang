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
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.fields.DependencyField;
import org.ballerinalang.toml.model.fields.ManifestHeader;
import org.ballerinalang.toml.model.fields.PackageField;
import org.ballerinalang.toml.util.SingletonStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom listener which is extended from the Toml listener with our own custom logic.
 *
 * @since 0.964
 */
public class ManifestBuildListener extends TomlBaseListener {
    private final Manifest manifest;
    private final SingletonStack<String> currentKey = new SingletonStack<>();
    private Dependency dependency;
    private String currentHeader = null;

    /**
     * Constructor with the manifest object.
     *
     * @param manifest manifest object
     */
    ManifestBuildListener(Manifest manifest) {
        this.manifest = manifest;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void enterKeyVal(TomlParser.KeyValContext ctx) {
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
    public void enterBasicStringValue(TomlParser.BasicStringValueContext ctx) {
        setToManifest(ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitKeyVal(TomlParser.KeyValContext ctx) {
        setDependencyAndPatches();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void enterArray(TomlParser.ArrayContext ctx) {
        setToManifest(ctx.arrayValues());
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
        addHeader(ctx.key().getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void enterInlineTable(TomlParser.InlineTableContext ctx) {
        setToManifest(ctx.inlineTableKeyvals());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does ncommentStartSymbol : HASH;
     * nonEol :  '\r'| '\t';
     * <p>
     * comment :  commentStartSymbol wsCommentNewline basicChar* nonEol*;othing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitInlineTable(TomlParser.InlineTableContext ctx) {
        setDependencyAndPatches();
    }

    /**
     * Add the dependencies and patches to the manifest object.
     */
    private void setDependencyAndPatches() {
        if (ManifestHeader.DEPENDENCIES.stringEquals(currentHeader)) {
            this.manifest.addDependency(dependency);
        } else if (ManifestHeader.PATCHES.stringEquals(currentHeader)) {
            this.manifest.addPatches(dependency);
        }
    }

    /**
     * Add the key-value pairs specified in the toml file.
     *
     * @param value KeyvalContext object
     */
    private void setToManifest(String value) {
        if (currentKey.present() && ManifestHeader.PROJECT.stringEquals(currentHeader)) {
            PackageField packageFieldField = PackageField.valueOfLowerCase(currentKey.pop());
            if (packageFieldField != null) {
                packageFieldField.setStringTo(this.manifest, value);
            }
        } else if (currentKey.present() && (ManifestHeader.DEPENDENCIES.stringEquals(currentHeader) ||
                ManifestHeader.PATCHES.stringEquals(currentHeader))) {
            DependencyField dependencyField = DependencyField.valueOfLowerCase(currentKey.pop());
            if (dependencyField != null) {
                dependencyField.setValueTo(dependency, value);
            }
        }
    }

    /**
     * Add array elements to manifest object.
     *
     * @param arrayValuesContext ArrayValuesContext object
     */
    private void setToManifest(TomlParser.ArrayValuesContext arrayValuesContext) {
        if (currentKey.present() && ManifestHeader.PROJECT.stringEquals(currentHeader)) {
            PackageField packageFieldField = PackageField.valueOfLowerCase(currentKey.pop());
            if (packageFieldField != null) {
                List<String> arrayElements = populateList(arrayValuesContext);
                packageFieldField.setListTo(this.manifest, arrayElements);
            }
        }
    }

    /**
     * Populate list values.
     *
     * @param arrayValuesContext array values
     * @return list of strings
     */
    private List<String> populateList(TomlParser.ArrayValuesContext arrayValuesContext) {
        List<String> arrayElements = new ArrayList<>();
        if (arrayValuesContext != null) {
            for (TomlParser.ArrayvalsNonEmptyContext valueContext : arrayValuesContext.arrayvalsNonEmpty()) {
                String value = valueContext.val().getText();
                if (valueContext.val().string() != null) {
                    value = valueContext.val().string().basicString().basicStringValue().getText();
                }
                arrayElements.add(value);
            }
        }
        return arrayElements;
    }

    /**
     * Add table headers in the toml file.
     *
     * @param key key specified in the header
     */
    private void addHeader(String key) {
        currentHeader = key;
        if (key.split("\\.").length > 1) {
            currentHeader = key.substring(0, key.indexOf("."));
            String pkgName = key.substring(key.indexOf(".") + 1);
            createDependencyObject(pkgName);
        }
    }

    /**
     * Add inline table content specified.
     *
     * @param ctx InlineTableKeyvalsContext object
     */
    private void setToManifest(TomlParser.InlineTableKeyvalsContext ctx) {
        if (currentKey.present() &&
                (ManifestHeader.DEPENDENCIES.stringEquals(currentHeader) ||
                        ManifestHeader.PATCHES.stringEquals(currentHeader))) {
            createDependencyObject(String.valueOf(currentKey.pop()));
            if (ctx != null) {
                populateDependencyField(ctx);
            }
        }
    }

    /**
     * Populate dependency fields by iterating over the context object.
     *
     * @param ctx Inline table values
     */
    private void populateDependencyField(TomlParser.InlineTableKeyvalsContext ctx) {
        for (TomlParser.InlineTableKeyvalsNonEmptyContext valueContext : ctx.inlineTableKeyvalsNonEmpty()) {
            String name = valueContext.key().getText();
            DependencyField dependencyField = DependencyField.valueOfLowerCase(name);
            if (dependencyField != null) {
                String value = valueContext.val().getText();
                if (valueContext.val().string() != null) {
                    value = valueContext.val().string().basicString().basicStringValue().getText();
                }
                dependencyField.setValueTo(dependency, value);
            }
        }
    }

    /**
     * Create dependency object and set the name.
     *
     * @param packageName pkg name of the dependency
     */
    private void createDependencyObject(String packageName) {
        dependency = new Dependency();
        DependencyField dependencyField = DependencyField.valueOfLowerCase("name");
        if (dependencyField != null) {
            dependencyField.setValueTo(dependency, packageName);
        }
    }
}
