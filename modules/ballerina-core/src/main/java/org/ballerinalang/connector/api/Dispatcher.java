package org.ballerinalang.connector.api;

import org.ballerinalang.model.values.BValue;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Created by rajith on 9/4/17.
 */
public interface Dispatcher {
    void setRegistry(Registry registry);
    String getProtocolPackage();

    Resource findResource();

    BValue[] createParameters();

    //temp
    CarbonCallback getCallback();
    //temp
    CarbonMessage getCarbonMsg();
}
