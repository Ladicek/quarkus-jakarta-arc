package cdi.lite.extension.model.declarations;

// TODO Jandex doesn't seem to index package annotations
public interface PackageInfo extends DeclarationInfo {
    String name();

    // ---

    @Override
    default Kind kind() {
        return Kind.PACKAGE;
    }

    @Override
    default PackageInfo asPackage() {
        return this;
    }
}
