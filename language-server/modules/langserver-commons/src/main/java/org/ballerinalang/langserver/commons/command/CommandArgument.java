package org.ballerinalang.langserver.commons.command;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.ParameterizedType;

/**
 * Class for the command argument holding argument key and argument value.
 */
public class CommandArgument {
    private final String argumentK;
    private final Object argumentV;
    private boolean isJson;
    private static final Gson GSON = new Gson();

    private CommandArgument(String argumentK, Object argumentV) {
        this.argumentK = argumentK;
        this.argumentV = argumentV;
    }

    public static CommandArgument from(String argumentK, Object argumentV) {
        CommandArgument argument = new CommandArgument(argumentK, argumentV);
        argument.isJson = false;
        return argument;
    }

    public static CommandArgument from(Object jsonObj) {
        // NOTE: we are not hard-coding any field names here
        CommandArgument commandArgument = GSON.fromJson(((JsonObject) jsonObj), CommandArgument.class);
        CommandArgument argument = new CommandArgument(commandArgument.getArgumentK(),
                                                       GSON.toJsonTree(commandArgument.getArgumentV()));
        argument.isJson = true;
        return argument;
    }

    public String getArgumentK() {
        return argumentK;
    }

    public <T> T getArgumentV() {
        if (this.isJson) {
            Class<T> persistentClass = (Class<T>)
                    ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            return GSON.fromJson((JsonElement) argumentV, persistentClass);
        }
        return (T) argumentV;
    }
}
