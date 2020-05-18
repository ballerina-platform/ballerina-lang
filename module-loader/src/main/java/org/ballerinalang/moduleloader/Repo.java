package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.ModuleId;

import java.io.IOException;
import java.util.List;

public interface Repo {

    List<String> resolveVersions(ModuleId moduleId, String filter) throws IOException;

    boolean isModuleExists(ModuleId moduleId);
}
