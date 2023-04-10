package io.ballerina.cli.cmd.sub;

import java.util.List;

public interface SubToolCommand {
    void execute(List<String> args);
    String getName();
}
