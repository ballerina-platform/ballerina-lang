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
package io.ballerina.shell.service;

import io.ballerina.shell.invoker.AvailableVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible to hold all declared variables and module declarations.
 *
 * Provide new definitions if available
 */
public class MetaInfoHandler {
    private List<String> definedVars;
    private List<String> moduleDclns;

    public MetaInfoHandler() {
        this.definedVars = new ArrayList<>();
        this.moduleDclns = new ArrayList<>();
    }

    public List<String> getNewDefinedVars(List<AvailableVariable> variableList) {
        List<String> newVars = new ArrayList<>();
        for (AvailableVariable availableVar: variableList) {
            if (!definedVars.contains(availableVar.getName())) {
                newVars.add(availableVar.getName());
            }
        }
        definedVars.addAll(newVars);
        return newVars;
    }

    public List<String> getNewModuleDclns(List<String> moduleDclnStrings) {
        List<String> newDclns = new ArrayList<>();
        for (String moduleDclnString: moduleDclnStrings) {
            Pattern pattern = Pattern.compile("\\((.[a-zA-Z\\d]*)\\)");
            Matcher matcher = pattern.matcher(moduleDclnString);
            if (matcher.find() && !moduleDclns.contains(matcher.group(1))) {
                newDclns.add(matcher.group(1));
            }
        }
        moduleDclns.addAll(newDclns);
        return newDclns;
    }

    public void removeFromDefinedVars(List<String> listToRemove) {
        definedVars.removeAll(listToRemove);
    }

    public void removeFromModuleDclns(List<String> listToRemove) {
        moduleDclns.removeAll(listToRemove);
    }

    public void reset() {
        this.definedVars = new ArrayList<>();
        this.moduleDclns = new ArrayList<>();
    }
}
