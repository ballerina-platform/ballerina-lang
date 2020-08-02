package org.wso2.ballerinalang.compiler.tree.bindingpatterns;

import org.ballerinalang.model.symbols.VariableSymbol;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.bindingpattern.CaptureBindingPattern;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

public class BLangCaptureBindingPattern extends BLangBindingPattern implements CaptureBindingPattern {

    BLangIdentifier identifier;
    public BVarSymbol symbol;

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.CAPTURE_BINDING_PATTERN;
    }

    @Override
    public IdentifierNode getIdentifier() {
        return identifier;
    }

    @Override
    public void setIdentifier(IdentifierNode identifier) {
        this.identifier = (BLangIdentifier) identifier;
    }

    @Override
    public VariableSymbol getSymbol() {
        return symbol;
    }

    @Override
    public void setSymbol(VariableSymbol symbol) {
        this.symbol = (BVarSymbol) symbol;
    }
}
