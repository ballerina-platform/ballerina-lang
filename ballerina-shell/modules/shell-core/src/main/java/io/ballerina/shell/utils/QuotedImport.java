/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.shell.utils;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * An import that has quoted orgname and module names.
 *
 * @since 2.0.0
 */
public class QuotedImport {
    private final String orgName;
    private final List<Identifier> moduleNames;

    public QuotedImport(String orgName, List<String> moduleNames) {
        this.orgName = orgName;
        this.moduleNames = moduleNames.stream()
                .map(Identifier::new)
                .collect(Collectors.toList());
    }

    public QuotedImport(List<String> moduleNames) {
        this(null, moduleNames);
    }

    public Identifier getDefaultPrefix() {
        return moduleNames.get(moduleNames.size() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuotedImport that = (QuotedImport) o;
        return Objects.equals(orgName, that.orgName) &&
                moduleNames.equals(that.moduleNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgName, moduleNames);
    }

    @Override
    public String toString() {
        StringJoiner moduleName = new StringJoiner(".");
        moduleNames.forEach(name -> moduleName.add(name.getName()));
        if (orgName == null) {
            return moduleName.toString();
        }
        return String.format("%s/%s", orgName, moduleName);
    }
}
