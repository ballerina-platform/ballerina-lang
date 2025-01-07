package io.ballerina.runtime.api.types.semtype;

/**
 * Internal node of a BDD, which represents a disjunction of conjunctions of atoms.
 *
 * @since 2201.11.0
 */
public abstract sealed class BddNode extends Bdd permits BddNodeImpl, BddNodeSimple {

    private volatile Integer hashCode = null;

    protected BddNode(boolean all, boolean nothing) {
        super(all, nothing);
    }

    public static BddNode bddAtom(Atom atom) {
        return new BddNodeSimple(atom);
    }

    public boolean isSimple() {
        return this instanceof BddNodeSimple;
    }

    public abstract Atom atom();

    public abstract Bdd left();

    public abstract Bdd middle();

    public abstract Bdd right();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BddNode other)) {
            return false;
        }
        return atom().equals(other.atom()) && left().equals(other.left()) && middle().equals(other.middle()) &&
                right().equals(other.right());
    }

    @Override
    public int hashCode() {
        Integer result = hashCode;
        if (result == null) {
            // No need to synchronize this since {@code computeHashCode} is idempotent
            hashCode = result = computeHashCode();
        }
        return result;
    }

    private int computeHashCode() {
        int result = atom().hashCode();
        result = 31 * result + left().hashCode();
        result = 31 * result + middle().hashCode();
        result = 31 * result + right().hashCode();
        return result;
    }

    @Override
    public boolean posMaybeEmpty() {
        return middle().posMaybeEmpty() || right().posMaybeEmpty();
    }
}
