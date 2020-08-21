package cdi.lite.extension.model.types;

import java.util.Optional;

/**
 * In case the wildcard type is unbounded (i.e., declared as {@code ?}), it is treated as if
 * it had an upper bound of {@code java.lang.Object}.
 * TODO this seems to be a Jandex decision, not sure we want to follow it
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
