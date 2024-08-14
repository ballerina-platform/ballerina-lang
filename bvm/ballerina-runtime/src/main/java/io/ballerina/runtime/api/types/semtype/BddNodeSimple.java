package io.ballerina.runtime.api.types.semtype;

final class BddNodeSimple extends BddNode {

    private final Atom atom;

    BddNodeSimple(Atom atom) {
        super(false, false);
        this.atom = atom;
    }

    @Override
    public Atom atom() {
        return atom;
    }

    @Override
    public Bdd left() {
        return BddAllOrNothing.ALL;
    }

    @Override
    public Bdd middle() {
        return BddAllOrNothing.NOTHING;
    }

    @Override
    public Bdd right() {
        return BddAllOrNothing.NOTHING;
    }
}
