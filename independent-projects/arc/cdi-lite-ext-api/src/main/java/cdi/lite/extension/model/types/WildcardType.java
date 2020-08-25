package cdi.lite.extension.model.types;

import java.util.Optional;

/**
 * In case the wildcard type is unbounded (i.e., declared as {@code ?}), both {@code upperBound}
 * and {@code lowerBound} are empty.
 */
public interface WildcardType extends Type {
    Optional<Type> upperBound();

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
