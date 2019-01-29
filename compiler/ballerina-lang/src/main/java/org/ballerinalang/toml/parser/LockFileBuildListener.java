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
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.fields.LockFileField;
import org.ballerinalang.toml.model.fields.LockFileHeader;
import org.ballerinalang.toml.model.fields.LockFilePackageField;
import org.ballerinalang.toml.util.SingletonStack;
import org.wso2.ballerinalang.compiler.LockFilePackage;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom listener which is extended from the Toml listener with our own custom logic.
 *
 * @since 0.964
 */
public class LockFileBuildListener extends TomlBaseListener {
    private final LockFile lockFile;
    private final SingletonStack<String> currentKey = new SingletonStack<>();
    private LockFilePackage lockFilePackage;
    private String currentHeader = null;

    /**
     * Constructor with the lockFile object.
     *
     * @param lockFile lockFile object
     */
    LockFileBuildListener(LockFile lockFile) {
        this.lockFile = lockFile;
    }

    @Override
    public void enterKeyVal(TomlParser.KeyValContext ctx) {
        currentKey.push(ctx.key().getText());
    }

    @Override
    public void enterBasicStringValue(TomlParser.BasicStringValueContext ctx) {
        setToLockFile(ctx.getText());
    }

    @Override
    public void exitKeyVal(TomlParser.KeyValContext ctx) {
        setPackage();
    }

    @Override
    public void enterArray(TomlParser.ArrayContext ctx) {
        setToLockFile(ctx.arrayValues());
    }

    @Override
    public void enterStdTable(TomlParser.StdTableContext ctx) {
        List<String> tableHeading = new ArrayList<>();
        tableHeading.add(ctx.key().getText());
        addHeader(tableHeading);
    }

    @Override
    public void enterArrayTable(TomlParser.ArrayTableContext ctx) {
        List<String> tableHeading = new ArrayList<>();
        tableHeading.add(ctx.key().getText());
        addHeader(tableHeading);
    }

    /**
     * Add the package to the lockFile object.
     */
    private void setPackage() {
        if (LockFileHeader.MODULE.stringEquals(currentHeader)) {
            this.lockFile.addPackage(lockFilePackage);
        }
    }

    /**
     * Add the key-value pairs specified in the toml file.
     *
     * @param value KeyvalContext object
     */
    private void setToLockFile(String value) {
        if (currentKey.present() && LockFileHeader.PROJECT.stringEquals(currentHeader)) {
            LockFileField packageFieldField = LockFileField.valueOfLowerCase(currentKey.pop());
            if (packageFieldField != null) {
                packageFieldField.setStringTo(this.lockFile, value);
            }
        } else if (currentKey.present() && LockFileHeader.MODULE.stringEquals(currentHeader)) {
            LockFilePackageField lockFilePackageField = LockFilePackageField.valueOfLowerCase(currentKey.pop());
            if (lockFilePackageField != null) {
                lockFilePackageField.setValueTo(lockFilePackage, value);
            }
        }
    }

    /**
     * Add array elements to lockFile object.
     *
     * @param arrayValuesContext ArrayValuesContext object
     */
    private void setToLockFile(TomlParser.ArrayValuesContext arrayValuesContext) {
        if (currentKey.present() && LockFileHeader.PROJECT.stringEquals(currentHeader)) {
            LockFileField packageFieldField = LockFileField.valueOfLowerCase(currentKey.pop());
            if (packageFieldField != null) {
                List<String> arrayElements = populateList(arrayValuesContext);
                packageFieldField.setListTo(this.lockFile, arrayElements);
            }
        } else if (currentKey.present() && LockFileHeader.MODULE.stringEquals(currentHeader)) {
            String key = currentKey.pop();
            if (LockFileHeader.IMPORTS.stringEquals(key)) {
                addImportsToPackage(arrayValuesContext, key);
            }
        }
    }

    private void addImportsToPackage(TomlParser.ArrayValuesContext arrayValuesContext, String key) {
        if (arrayValuesContext != null) {
            for (TomlParser.ArrayvalsNonEmptyContext valueContext : arrayValuesContext.arrayvalsNonEmpty()) {
                setToLockFile(valueContext.val().inlineTable().inlineTableKeyvals(), key);
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
    private void addHeader(List<String> key) {
        currentHeader = key.get(0);
        createPackageObj(currentHeader);
    }

    /**
     * Add inline table content specified.
     *
     * @param ctx InlineTableKeyvalsContext object
     */
    private void setToLockFile(TomlParser.InlineTableKeyvalsContext ctx, String key) {
        if (LockFileHeader.MODULE.stringEquals(currentHeader) && LockFileHeader.IMPORTS.stringEquals(key)) {
            if (ctx != null) {
                populatePackageField(ctx, key);
            }
        }
    }

    /**
     * Populate lockFilePackage fields by iterating over the context object.
     *
     * @param ctx Inline table values
     * @param key
     */
    private void populatePackageField(TomlParser.InlineTableKeyvalsContext ctx, String key) {
        LockFilePackage importPackage = new LockFilePackage();
        LockFilePackageField lockFilePackageField = LockFilePackageField.NAME;
        lockFilePackageField.setValueTo(importPackage, key);

        for (TomlParser.InlineTableKeyvalsNonEmptyContext valueContext : ctx.inlineTableKeyvalsNonEmpty()) {
            String name = valueContext.key().getText();
            LockFilePackageField field = LockFilePackageField.valueOfLowerCase(name);
            if (field != null) {
                String value = valueContext.val().getText();
                if (valueContext.val().string() != null) {
                    value = valueContext.val().string().basicString().basicStringValue().getText();
                }
                field.setValueTo(importPackage, value);
            }
        }

        lockFilePackage.addImport(importPackage);
    }

    /**
     * Create lockFilePackage object and set the name.
     *
     * @param packageName pkg name of the lockFilePackage
     */
    private void createPackageObj(String packageName) {
        lockFilePackage = new LockFilePackage();
        LockFilePackageField lockFilePackageField = LockFilePackageField.NAME;
        lockFilePackageField.setValueTo(lockFilePackage, packageName);
    }
}
