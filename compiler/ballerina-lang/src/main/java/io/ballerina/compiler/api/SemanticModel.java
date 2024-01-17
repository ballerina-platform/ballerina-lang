/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.api;

import io.ballerina.compiler.api.symbols.DiagnosticState;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

import java.util.List;
import java.util.Optional;

/**
 * Represents an Abstract semantic model.
 *
 * @since 2.0.0
 */
public interface SemanticModel {

    /**
     * Retrieves a single instance of the Types API.
     *
     * @return {@link Types} instance of a given semantic context
     */
    Types types();

    /**
     * Lookup the visible symbols at the given location.
     *
     * @param sourceFile The source file document in which to look up the position
     * @param position   text position in the source
     * @return {@link List} of visible symbols in the given location
     */
    List<Symbol> visibleSymbols(Document sourceFile, LinePosition position);

    /**
     * Lookup the visible symbols at the given location. This additionally takes a list of diagnostic states. These are
     * used to determine whether to include variable symbols with varying diagnostic states.
     *
     * @param sourceFile The source file document in which to look up the position
     * @param position   text position in the source
     * @param states     The allowed states of in-scope variable symbols
     * @return {@link List} of visible symbols in the given location
     */
    List<Symbol> visibleSymbols(Document sourceFile, LinePosition position, DiagnosticState... states);

    /**
     * Lookup the symbol at the given location.
     *
     * @param sourceDocument The source file document in which to look up the position
     * @param position       text position in the source
     * @return {@link Symbol} in the given location
     */
    Optional<Symbol> symbol(Document sourceDocument, LinePosition position);

    /**
     * Looks up the symbol for the specified syntax tree node. This will only return a symbol if the provided node is a
     * construct in the language for which we associate names. e.g., functions, variables, function calls, type
     * definitions etc.
     * <p>
     * One can also provide just the relevant portion of a particular construct to get the symbol. e.g., binding pattern
     * node of a variable declaration node, function name node of a function definition node.
     *
     * @param node The syntax tree node of which the symbol is required
     * @return {@link Symbol} for the given node
     */
    Optional<Symbol> symbol(Node node);

    /**
     * Retrieves the symbols of module-scoped constructs in the semantic model.
     *
     * @return A list of module-scoped symbols
     */
    List<Symbol> moduleSymbols();

    /**
     * Finds all the references of the specified symbol within the relevant scope.
     *
     * @param symbol a {@link Symbol} instance
     * @return A {@link List} of line ranges of all the references
     */
    List<Location> references(Symbol symbol);

    /**
     * If there's an identifier associated with a symbol at the specified cursor position, finds all the references of
     * the specified symbol within the relevant scope.
     *
     * @param sourceDocument The source file document in which to look up the position
     * @param position       a cursor position in the source
     * @return A {@link List} of line ranges of all the references
     */
    List<Location> references(Document sourceDocument, LinePosition position);

    /**
     * Finds all the references of the specified symbol within the relevant scope. This list excludes the reference in
     * the definition.
     *
     * @param symbol         a {@link Symbol} instance
     * @param withDefinition Whether the definition should be counted as a reference or not
     * @return A {@link List} of line ranges of all the references except the definition
     */
    List<Location> references(Symbol symbol, boolean withDefinition);

    /**
     * If there's an identifier associated with a symbol at the specified cursor position, finds all the references of
     * the specified symbol within the relevant scope. This list excludes the reference in the definition.
     *
     * @param sourceDocument The source file document in which to look up the position
     * @param position       a cursor position in the source
     * @param withDefinition Whether the definition should be counted as a reference or not
     * @return A {@link List} of line ranges of all the references except the definition
     */
    List<Location> references(Document sourceDocument, LinePosition position, boolean withDefinition);

    /**
     * Finds all the references of the specified symbol within the given target document.
     *
     * @param symbol         a {@link Symbol} instance
     * @param targetDocument The given target document in which to lok up for references
     * @param withDefinition Whether the definition should be counted as a reference or not
     * @return A {@link List} of line ranges of all the references except the definition
     */
    List<Location> references(Symbol symbol, Document targetDocument, boolean withDefinition);

    /**
     * If there's an identifier associated with a symbol at the specified cursor position, finds all the references of
     * the specified symbol within the given target document. This list excludes the reference in the definition.
     *
     * @param sourceDocument The source file document in which to look up the position
     * @param targetDocument The given target document in which to lok up for references
     * @param position       a cursor position in the source
     * @param withDefinition Whether the definition should be counted as a reference or not
     * @return A {@link List} of line ranges of all the references except the definition
     */
    List<Location> references(Document sourceDocument,
                              Document targetDocument,
                              LinePosition position,
                              boolean withDefinition);

    /**
     * Retrieves the type of the expression in the specified text range. If it's not a valid expression, returns an
     * empty {@link Optional} value!.
     *
     * @param range the text range of the expression
     * @return the type of the expression
     * @deprecated This method will be removed in a later version. Use typeOf() instead.
     */
    @Deprecated
    Optional<TypeSymbol> type(LineRange range);

    /**
     * Given a syntax tree node, returns the type of that node, if it is an expression node. For any other node, this
     * will return empty.
     *
     * @param node The expression node of which the type is needed
     * @return The type if it's a valid expression node, if not, returns empty
     * @deprecated Deprecated since this returns type for non-expression nodes as well. Use typeOf() instead.
     */
    @Deprecated
    Optional<TypeSymbol> type(Node node);

    /**
     * Retrieves the type of the node in the specified text range. The node matching the specified range should be an
     * expression. For any other kind of node, this will return empty.
     *
     * @param range the text range of the expression
     * @return the type of the expression
     */
    Optional<TypeSymbol> typeOf(LineRange range);

    /**
     * Given a syntax tree node, returns the type of that node, if it is an expression node. For any other node, this
     * will return empty.
     *
     * @param node The expression node of which the type is needed
     * @return The type if it's a valid expression node, if not, returns empty
     */
    Optional<TypeSymbol> typeOf(Node node);

    /**
     * Get the diagnostics within the given text Span.
     *
     * @param range Line range to filter the diagnostics
     * @return {@link List} of extracted diagnostics
     */
    List<Diagnostic> diagnostics(LineRange range);

    /**
     * Retrieves the diagnostics of the module.
     *
     * @return {@link List} of diagnostics for the module
     */
    List<Diagnostic> diagnostics();

    /**
     * Get the expected type for a given position.
     *
     * @param sourceDocument The source file document in which to look up the position
     * @param linePosition line position to get the expected type
     * @return the type symbol if available, if not, returns empty
     */

    Optional<TypeSymbol> expectedType(Document sourceDocument, LinePosition linePosition);
}
