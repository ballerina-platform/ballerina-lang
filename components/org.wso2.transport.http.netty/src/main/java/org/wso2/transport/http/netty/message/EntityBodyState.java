package org.wso2.transport.http.netty.message;

/**
 * Entity body state.
 */
enum EntityBodyState {
    CONSUMABLE,
    CONSUMED,
    EXPECTING
}
