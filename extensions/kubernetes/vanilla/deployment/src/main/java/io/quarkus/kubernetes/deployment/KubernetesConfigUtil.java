package io.quarkus.kubernetes.deployment;

import static io.quarkus.container.spi.ImageReference.DEFAULT_TAG;
import static io.quarkus.deployment.builditem.ApplicationInfoBuildItem.UNSET_VALUE;
import static io.quarkus.kubernetes.deployment.Constants.DEPLOY;
import static io.quarkus.kubernetes.deployment.Constants.DEPLOYMENT_TARGET;
import static io.quarkus.kubernetes.deployment.Constants.DOCKER;
import static io.quarkus.kubernetes.deployment.Constants.KNATIVE;
import static io.quarkus.kubernetes.deployment.Constants.KUBERNETES;
import static io.quarkus.kubernetes.deployment.Constants.OPENSHIFT;
import static io.quarkus.kubernetes.deployment.Constants.S2I;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import io.dekorate.utils.Strings;

public class KubernetesConfigUtil {

    private static final String DEKORATE_PREFIX = "dekorate.";
    private static final String QUARKUS_PREFIX = "quarkus.";
    private static final Pattern QUARKUS_DEPLOY_PATTERN = Pattern.compile("quarkus\\.([^\\.]+)\\.deploy");

    private static final Set<String> ALLOWED_GENERATORS = new HashSet<>(
            Arrays.asList(KUBERNETES, OPENSHIFT, KNATIVE, DOCKER, S2I));

    private static final String EXPOSE_PROPERTY_NAME = "expose";
    private static final String[] EXPOSABLE_GENERATORS = { OPENSHIFT, KUBERNETES };

    private static final Logger log = Logger.getLogger(KubernetesConfigUtil.class);

    /**
     * Get the explicit configured deployment target, if any.
     * The explicit deployment target is determined using: `quarkus.kubernetes.deployment-target=<deployment-target>`
     */
    public static Optional<String> getExplicitlyConfiguredDeploymentTarget() {
        Config config = ConfigProvider.getConfig();
        return config.getOptionalValue(DEPLOYMENT_TARGET, String.class);
    }

    /**
     * The the explicitly configured deployment target list.
     * The configured deployment targets are determined using: `quarkus.kubernetes.deployment-target=<deployment-target>`
     */
    public static List<String> getExplictilyDeploymentTargets() {
        String deploymentTargets = getExplicitlyConfiguredDeploymentTarget().orElse("");
        if (deploymentTargets.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(deploymentTargets
                .split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    /**
     * Get the user configured deployment target, if any.
     * The configured deployment target is determined using:
     * 1. the value of `quarkus.kubernetes.deployment-target=<deployment-target>`
     * 2. the presenve of `quarkus.<deployment-target>.deploy=true`
     */
    public static Optional<String> getConfiguredDeploymentTarget() {
        Config config = ConfigProvider.getConfig();
        Optional<String> indirectTarget = Stream
                .concat(System.getProperties().entrySet().stream().map(e -> String.valueOf(e.getKey()))
                        .filter(k -> k.startsWith("quarkus.")),
                        StreamSupport.stream(config.getPropertyNames().spliterator(), false))
                .map(p -> QUARKUS_DEPLOY_PATTERN.matcher(p))
                .map(m -> m.matches() ? m.group(1) : null)
                .filter(s -> s != null)
                .findFirst();

        return getExplicitlyConfiguredDeploymentTarget().or(() -> indirectTarget);
    }

    /**
     * The the configured deployment target list.
     * The configured deployment targets are determined using:
     * 1. the value of `quarkus.kubernetes.deployment-target=<deployment-target>`
     * 2. the presenve of `quarkus.<deployment-target>.deploy=true`
     */
    public static List<String> getConfiguratedDeploymentTargets() {
        String deploymentTargets = getConfiguredDeploymentTarget().orElse("");
        if (deploymentTargets.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(deploymentTargets
                .split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    /**
     * @return true if deployment is explicitly enabled using: `quarkus.<deployment target>.deploy=true`.
     */
    public static boolean isDeploymentEnabled() {
        Config config = ConfigProvider.getConfig();
        Optional<Boolean> indirectTargetEnabled = Stream
                .concat(System.getProperties().entrySet().stream().map(e -> String.valueOf(e.getKey())).filter(
                        k -> k.startsWith("quarkus.")), StreamSupport.stream(config.getPropertyNames().spliterator(), false))
                .map(p -> QUARKUS_DEPLOY_PATTERN.matcher(p))
                .filter(Matcher::matches)
                .map(m -> m.group(1))
                .map(k -> config.getOptionalValue("quarkus." + k + ".deploy", Boolean.class).orElse(false))
                .findFirst();

        // 'quarkus.kubernetes.deploy' has a default value and thus getOptionaValue will never return `Optional.empty()`
        return config.getOptionalValue(DEPLOY, Boolean.class).orElse(false) || indirectTargetEnabled.orElse(false);
    }

    /*
     * Collects configuration properties for Kubernetes. Reads all properties and
     * matches properties that match known Dekorate generators. These properties may
     * or may not be prefixed with `quarkus.` though the prefixed ones take
     * precedence.
     *
     * @return A map containing the properties.
     */
    public static Map<String, Object> toMap(PlatformConfiguration... platformConfigurations) {
        Config config = ConfigProvider.getConfig();
        Map<String, Object> result = new HashMap<>();

        // Most of quarkus prefixed properties are handled directly by the config items (KubernetesConfig, OpenshiftConfig, KnativeConfig)
        // We just need group, name & version parsed here, as we don't have decorators for these (low level properties).
        Map<String, Object> quarkusPrefixed = new HashMap<>();

        Arrays.stream(platformConfigurations).forEach(p -> {
            p.getPartOf().ifPresent(g -> quarkusPrefixed.put(DEKORATE_PREFIX + p.getConfigName() + ".part-of", g));
            p.getName().ifPresent(n -> quarkusPrefixed.put(DEKORATE_PREFIX + p.getConfigName() + ".name", n));
            p.getVersion()
                    .map(v -> v.equals(UNSET_VALUE) ? DEFAULT_TAG : v)
                    .ifPresent(v -> quarkusPrefixed.put(DEKORATE_PREFIX + p.getConfigName() + ".version", v));
        });

        Map<String, Object> unPrefixed = StreamSupport.stream(config.getPropertyNames().spliterator(), false)
                .filter(k -> ALLOWED_GENERATORS.contains(generatorName(k)))
                .filter(k -> config.getOptionalValue(k, String.class).isPresent())
                .collect(Collectors.toMap(k -> DEKORATE_PREFIX + k, k -> config.getValue(k, String.class)));

        for (String generator : ALLOWED_GENERATORS) {
            String oldKey = DEKORATE_PREFIX + generator + ".group";
            String newKey = DEKORATE_PREFIX + generator + ".part-of";
            if (unPrefixed.containsKey(oldKey)) {
                unPrefixed.put(newKey, unPrefixed.get(oldKey));
            }
        }

        handleExpose(config, unPrefixed, platformConfigurations);

        result.putAll(unPrefixed);
        result.putAll(quarkusPrefixed);
        result.putAll(toS2iProperties(quarkusPrefixed));
        return result;
    }

    @Deprecated
    private static void handleExpose(Config config, Map<String, Object> unPrefixed,
            PlatformConfiguration... platformConfigurations) {
        for (String generator : EXPOSABLE_GENERATORS) {
            boolean unprefixedExpose = config.getOptionalValue(generator + "." + EXPOSE_PROPERTY_NAME, Boolean.class)
                    .orElse(false);
            boolean prefixedExpose = config
                    .getOptionalValue(QUARKUS_PREFIX + generator + "." + EXPOSE_PROPERTY_NAME, Boolean.class)
                    .orElse(false);
            if (unprefixedExpose || prefixedExpose) {
                if (generator == KUBERNETES) {
                    log.warn("Usage of quarkus.kubernetes.expose is deprecated in favor of quarkus.kubernetes.ingress.expose");
                } else {
                    log.warn("Usage of quarkus.openshift.expose is deprecated in favor of quarkus.openshift.route.expose");
                }
                unPrefixed.put(DEKORATE_PREFIX + generator + "." + EXPOSE_PROPERTY_NAME, true);
                for (PlatformConfiguration platformConfiguration : platformConfigurations) {
                    if (platformConfiguration.getConfigName().equals(generator)) {
                        platformConfiguration.getHost()
                                .ifPresent(h -> {
                                    unPrefixed.put(DEKORATE_PREFIX + generator + ".host", h);
                                    if (generator == KUBERNETES) {
                                        log.warn(
                                                "Usage of quarkus.kubernetes.host is deprecated in favor of quarkus.kubernetes.ingress.host");
                                    } else {
                                        log.warn(
                                                "Usage of quarkus.openshift.host is deprecated in favor of quarkus.openshift.route.host");
                                    }
                                });
                        break;
                    }
                }
            }
        }
    }

    /**
     * Returns the name of the generators that can handle the specified key.
     *
     * @param key The key.
     * @return The generator name or null if the key format is unexpected.
     */
    private static String generatorName(String key) {
        if (Strings.isNullOrEmpty(key) || !key.contains(".")) {
            return null;
        }
        return key.substring(0, key.indexOf("."));
    }

    private static Map<String, Object> toS2iProperties(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        map.forEach((k, v) -> {
            if (k.contains(OPENSHIFT)) {
                result.put(k.replaceAll(OPENSHIFT, S2I), v);
            }
        });
        return result;
    }
}