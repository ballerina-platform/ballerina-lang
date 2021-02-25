package org.ballerinalang.langserver.commons.command;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class for the command argument holding argument key and argument value.
 */
public class CommandArgument {
    private final String key;
    private final Object value;
    private static final Gson GSON = new Gson();

    private CommandArgument(String argumentK, Object value) {
        this.key = argumentK;
        this.value = value;
    }

    public static CommandArgument from(String argumentK, Object argumentV) {
        return new CommandArgument(argumentK, argumentV);
    }

    public static CommandArgument from(Object jsonObj) {
        // NOTE: we are not hard-coding any field names here
        CommandArgument commandArgument = GSON.fromJson(((JsonObject) jsonObj), CommandArgument.class);
        return new CommandArgument(commandArgument.key(),
                                   GSON.toJsonTree(commandArgument.value()));
    }

    public String key() {
        return key;
    }

    public <T> T value() {
        return (T) value;
    }

    public <T> T valueAs(Class<T> typeClass) {
        return GSON.fromJson((JsonElement) value, typeClass);
    }
}
