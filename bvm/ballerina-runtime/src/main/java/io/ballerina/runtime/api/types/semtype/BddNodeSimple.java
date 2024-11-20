package io.ballerina.runtime.api.types.semtype;

/**
 * Represent a Bdd node that contains a single atom as positive. This is used to reduce the memory overhead of
 * BddNodeImpl in representing such nodes
 *
 * @since 2201.11.0
 */
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
