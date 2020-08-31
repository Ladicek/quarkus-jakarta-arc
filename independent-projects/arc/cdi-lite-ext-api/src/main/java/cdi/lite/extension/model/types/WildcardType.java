package cdi.lite.extension.model.types;

import java.util.Optional;

/**
 * In case the wildcard type is unbounded (i.e., declared as {@code ?}), both {@code upperBound}
 * and {@code lowerBound} are empty.
 */
public interface WildcardType extends Type {
    /**
     * Present when the wildcard type has a form of {@code ? extends Number}.
     * The upper bound in this case is {@code Number}.
     * Otherwise empty.
     */
    Optional<Type> upperBound();

    /**
     * Present when the wildcard type has a form of {@code ? super Number}.
     * The lower bound in this case is {@code Number}.
     * Otherwise empty.
     */
    Optional<Type> lowerBound();

    // ---

    @Override
    default Kind kind() {
        return Kind.WILDCARD_TYPE;
    }

    @Override
    default WildcardType asWildcardType() {
        return this;
    }
}
