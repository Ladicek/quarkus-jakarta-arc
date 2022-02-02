package io.quarkus.arc.arquillian;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class DeploymentDir {
    final Path root;

    final Path appClasses;
    final Path appLibraries;
    final Path beanArchive;

    final Path generatedClasses;
    final Path generatedServiceProviders;

    DeploymentDir() throws IOException {
        this.root = Files.createTempDirectory("ArcArquillian");

        this.appClasses = Files.createDirectory(root.resolve("app"));
        this.appLibraries = Files.createDirectory(root.resolve("lib"));
        this.beanArchive = Files.createDirectory(root.resolve("beanArchive"));

        this.generatedClasses = Files.createDirectory(root.resolve("generated"));
        this.generatedServiceProviders = Files.createDirectory(root.resolve("providers"));
    }
}
