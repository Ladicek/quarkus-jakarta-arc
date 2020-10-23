package cdi.lite.extension;

/**
 * Build compatible extensions are service providers for this interface, as defined in {@link java.util.ServiceLoader}.
 * This means: they are classes that implement this interface, provide a {@code META-INF/services} file,
 * and satisfy all other service provider constraints. Additionally, extensions should not be CDI beans
 * and should not be used at application runtime.
 * <p>
 * Extensions can define arbitrary {@code public}, non-{@code static}, {@code void}-returning methods
 * without type parameters, annotated with one of the extension annotations.
 * <p>
 * Extension processing occurs in 4 phases, corresponding to 4 extension annotations:
 * <ul>
 * <li>{@link cdi.lite.extension.phases.Discovery @Discovery}</li>
 * <li>{@link cdi.lite.extension.phases.Enhancement @Enhancement}</li>
 * <li>{@link cdi.lite.extension.phases.Synthesis @Synthesis}</li>
 * <li>{@link cdi.lite.extension.phases.Validation @Validation}</li>
 * </ul>
 * <p>
 * These methods can declare arbitrary number of parameters. Which parameters can be declared depends
 * on the particular processing phase and is documented in the corresponding extension annotation.
 * All the parameters will be provided by the container when the extension is invoked.
 * <p>
 * Extension can be assigned a priority using {@link cdi.lite.extension.ExtensionPriority @ExtensionPriority}.
 * Note that priority only affects order of extensions in a single phase.
 * <p>
 * If the extension declares multiple methods, they are all invoked on the same instance of the class.
 */
public interface BuildCompatibleExtension {
}
