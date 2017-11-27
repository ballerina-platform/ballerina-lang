package org.ballerinalang.composer.service.workspace.ws;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by natasha on 11/27/17.
 */
public interface ComposerApi {
    List<ComposerApiHandler> handlers = new ArrayList<>();
    public String getApiName();
}
