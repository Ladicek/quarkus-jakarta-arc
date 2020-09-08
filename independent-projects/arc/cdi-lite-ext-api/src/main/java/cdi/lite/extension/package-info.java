/**
 * An extension is a {@code public}, non-{@code static}, {@code void}-returning method without type parameters,
 * annotated with one of the extension annotations (see below). The method must be declared on a {@code public} class
 * without type parameters and with a {@code public} zero-parameter constructor.
 * This class should not be a CDI bean and should not be used at runtime.
 * <p>
 * Extension processing occurs in 4 phases, corresponding to 4 extension annotations:
 * <ul>
 * <li>{@link cdi.lite.extension.phases.Discovery @Discovery}</li>
 * <li>{@link cdi.lite.extension.phases.Enhancement @Enhancement}</li>
 * <li>{@link cdi.lite.extension.phases.Synthesis @Synthesis}</li>
 * <li>{@link cdi.lite.extension.phases.Validation @Validation}</li>
 * </ul>
 * <p>
 * Extensions can declare arbitrary number of parameters. Which parameters can be declared depends
 * on the particular processing phase and is documented in the corresponding extension annotation.
 * All the parameters will be provided by the container when the extension is invoked.
 * <p>
 * Extension can be assigned a priority using {@link cdi.lite.extension.ExtensionPriority @ExtensionPriority}.
 * Note that priority only affects order of extensions in a single phase.
 * <p>
 * If a class declares multiple extensions, they are all invoked on the same instance of the class.
 */
package cdi.lite.extension;