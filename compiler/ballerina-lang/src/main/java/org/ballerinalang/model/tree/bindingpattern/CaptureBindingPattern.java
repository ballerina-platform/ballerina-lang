package org.ballerinalang.model.tree.bindingpattern;

import org.ballerinalang.model.symbols.Symbol;
import org.ballerinalang.model.symbols.VariableSymbol;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.expressions.ExpressionNode;

public interface CaptureBindingPattern extends Node {

    IdentifierNode getIdentifier();

    void setIdentifier(IdentifierNode variableName);

    VariableSymbol getSymbol();

    void setSymbol(VariableSymbol symbol);

}
