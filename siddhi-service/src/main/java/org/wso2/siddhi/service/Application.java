package org.wso2.siddhi.service;

import org.wso2.msf4j.MicroservicesRunner;

/**
 * Application class.
 */
public class Application {

    public static void main(String[] args) {

        new MicroservicesRunner().deploy(new SiddhiService()).start();
    }
}
