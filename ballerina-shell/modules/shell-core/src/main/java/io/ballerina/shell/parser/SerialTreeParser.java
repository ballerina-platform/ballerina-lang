/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.parser;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.shell.exceptions.TreeParserException;
import io.ballerina.shell.parser.trials.EmptyExpressionTrial;
import io.ballerina.shell.parser.trials.ExpressionListTrial;
import io.ballerina.shell.parser.trials.ExpressionTrial;
import io.ballerina.shell.parser.trials.GetErrorMessageTrial;
import io.ballerina.shell.parser.trials.InvalidMethodException;
import io.ballerina.shell.parser.trials.ModuleMemberTrial;
import io.ballerina.shell.parser.trials.ModulePartTrial;
import io.ballerina.shell.parser.trials.ParserRejectedException;
import io.ballerina.shell.parser.trials.ParserTrialFailedException;
import io.ballerina.shell.parser.trials.StatementTrial;
import io.ballerina.shell.parser.trials.TreeParserTrial;
import io.ballerina.shell.parser.trials.WorkerDeclarationTrial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Parses the source code line using a trial based method.
 * The source code is placed in several places and is attempted to parse.
 * This continues until the correct type can be determined.
 *
 * @since 2.0.0
 */
public class SerialTreeParser extends TrialTreeParser {

    private static final String COMMAND_PREFIX = "/";
    private final List<TreeParserTrial> nodeParserTrials;

    public SerialTreeParser(long timeOutDurationMs) {
        super(timeOutDurationMs);
        this.nodeParserTrials = List.of(
                new ModuleMemberTrial(this),
                new ExpressionTrial(this),
                new ExpressionListTrial(this),
                new StatementTrial(this),
                new WorkerDeclarationTrial(this),
                new EmptyExpressionTrial(this),
                new GetErrorMessageTrial(this)
        );
    }

    @Override
    public Collection<Node> parse(String source) throws TreeParserException {
        String errorMessage = "";
        for (TreeParserTrial trial : nodeParserTrials) {
            try {
                return Objects.requireNonNull(trial.parse(source), "trial returned no nodes");
            } catch (ParserTrialFailedException e) {
                errorMessage = e.getMessage();
            } catch (ParserRejectedException e) {
                errorMessage = "Invalid statement: " + e.getMessage();
                break;
            } catch (InvalidMethodException e) {
                errorMessage = e.getMessage();
                addErrorDiagnostic(errorMessage);
                throw new TreeParserException();
            } catch (Throwable e) {
                errorMessage = "Code contains syntax error(s).";
            }
        }
        if (source.startsWith(COMMAND_PREFIX)) {
            errorMessage = "Can not find the command: " + source.trim();
            addErrorDiagnostic(errorMessage);
            addErrorDiagnostic("Please use \"/help\" command to view available commands.");
        } else {
            addErrorDiagnostic(errorMessage);
            addErrorDiagnostic("Parsing aborted due to errors.");
        }
        throw new TreeParserException();
    }

    @Override
    public Collection<Node> parseDeclarations(String source) throws TreeParserException {
        try {
            ModulePartTrial modulePartTrial = new ModulePartTrial(this);
            Collection<Node> nodes = modulePartTrial.parse(source);
            List<Node> declarationNodes = new ArrayList<>();
            for (Node node : nodes) {
                ModulePartNode modulePartNode = (ModulePartNode) node;
                modulePartNode.imports().forEach(declarationNodes::add);
                modulePartNode.members().stream().filter(this::isModuleDeclarationAllowed)
                        .forEach(declarationNodes::add);
            }
            return declarationNodes;
        } catch (ParserTrialFailedException e) {
            addErrorDiagnostic(e.getMessage());
            addErrorDiagnostic("Parsing aborted because of errors.");
            throw new TreeParserException();
        }
    }

    /**
     * Whether the declaration is allowed to be parsed.
     */
    private boolean isModuleDeclarationAllowed(ModuleMemberDeclarationNode declarationNode) {
        if (declarationNode instanceof FunctionDefinitionNode functionDefinitionNode) {
            String functionName = functionDefinitionNode.functionName().text();
            if (ParserConstants.isFunctionNameRestricted(functionName)) {
                addWarnDiagnostic("Found '" + functionName + "' function in the declarations.\n" +
                        "Discarded '" + functionName + "' function without loading.");
                return false;
            }
        }
        return true;
    }
}
