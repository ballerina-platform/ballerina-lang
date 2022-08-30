package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BTypedesc;

import java.util.Map;

public class RegExpAtomQuantifier implements RegExpTerm {
    private RegExpAtom reAtom;
    private RegExpQuantifier reQuantifier;

    public RegExpAtomQuantifier(RegExpAtom reAtom, RegExpQuantifier reQuantifier) {
        this.reAtom = reAtom;
        this.reQuantifier = reQuantifier;
    }

    public RegExpAtom getReAtom() {
        return this.reAtom;
    }

    public RegExpQuantifier getReQuantifier() {
        return this.reQuantifier;
    }

    public void setReAtom(RegExpAtom reAtom) {
        this.reAtom = reAtom;
    }

    public void setReQuantifier(RegExpQuantifier reQuantifier) {
        this.reQuantifier = reQuantifier;
    }

    @Override
    public String stringValue(BLink parent) {
        return this.reAtom.stringValue(parent) + this.reQuantifier.stringValue(parent);
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return stringValue(parent);
    }

    @Override
    public String informalStringValue(BLink parent) {
        return stringValue(parent);
    }

    @Override
    public Type getType() {
        return PredefinedTypes.TYPE_ANYDATA;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public BTypedesc getTypedesc() {
        throw new UnsupportedOperationException();
    }
}
