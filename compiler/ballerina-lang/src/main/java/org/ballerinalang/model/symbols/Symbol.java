/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.symbols;

import org.ballerinalang.model.Name;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.types.Type;
import org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition;

import java.util.List;
import java.util.Set;

/**
 * {@code Symbol} represents a Ballerina program symbol such as a variable, function,
 * connector, action, or package.
 *
 * @since 0.94
 */
public interface Symbol {

    Name getName();

    SymbolKind getKind();

    Type getType();

    Set<Flag> getFlags();

    Symbol getEnclosingSymbol();

    List<? extends Symbol> getEnclosedSymbols();

    DiagnosticPosition getPosition();

    SymbolOrigin getOrigin();
}
