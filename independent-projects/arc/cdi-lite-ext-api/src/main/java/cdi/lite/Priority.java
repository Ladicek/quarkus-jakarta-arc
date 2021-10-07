package cdi.lite;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// TODO temporary, until we can use jakarta.annotation.Priority
@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {
    int value();
}
