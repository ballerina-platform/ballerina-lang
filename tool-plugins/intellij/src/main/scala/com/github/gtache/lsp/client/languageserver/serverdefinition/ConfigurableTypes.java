package com.github.gtache.lsp.client.languageserver.serverdefinition;

/**
 * Java doesn't like Objects in Trait apparently
 * This represents the known types of UserConfigurableServerDefinition
 */
public enum ConfigurableTypes {
    ARTIFACT(ArtifactLanguageServerDefinition$.MODULE$.getPresentableTyp()), EXE(ExeLanguageServerDefinition$.MODULE$.getPresentableTyp()), RAWCOMMAND(RawCommandServerDefinition$.MODULE$.getPresentableTyp());
    private final String typ;

    ConfigurableTypes(final String typ) {
        this.typ = typ;
    }

    public String getTyp() {
        return typ;
    }
}
