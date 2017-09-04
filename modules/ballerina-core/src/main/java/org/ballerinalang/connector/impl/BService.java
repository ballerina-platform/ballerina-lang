package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rajith on 9/4/17.
 */
public class BService implements Service {

        private Map<String, Resource> resourceMap = new HashMap<>();
}
