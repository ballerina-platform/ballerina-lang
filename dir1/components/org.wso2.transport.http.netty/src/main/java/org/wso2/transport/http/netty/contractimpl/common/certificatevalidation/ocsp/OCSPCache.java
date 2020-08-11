/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.ocsp;

import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.MBeanRegistrar;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.CertificateVerificationException;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.cache.CacheController;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.cache.CacheManager;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.cache.ManageableCache;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.cache.ManageableCacheValue;

import java.math.BigInteger;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a cache to store OSCP responses against Certificate Serial Number since an OCSP response depends on
 * the certificate. This is a singleton since more than one cache of this kind should not be allowed. This cache
 * can be shared by many transports which need SSL validation through OCSP.
 */
public class OCSPCache implements ManageableCache {

    private static volatile OCSPCache cache;
    private static volatile Map<BigInteger, OCSPCacheValue> hashMap = new ConcurrentHashMap<>();
    private volatile Iterator<Map.Entry<BigInteger, OCSPCacheValue>> iterator
            = hashMap.entrySet().iterator();
    private volatile CacheManager cacheManager;
    private static final Logger LOG = LoggerFactory.getLogger(OCSPCache.class);

    private OCSPCache() {
    }

    public static OCSPCache getCache() {
        //Double checked locking
        if (cache == null) {
            synchronized (OCSPCache.class) {
                if (cache == null) {
                    cache = new OCSPCache();
                }
            }
        }
        return cache;
    }

    /**
     * This lazy initializes the cache with a CacheManager. If this method is not called,
     * a cache manager will not be used.
     *
     * @param size  max size of the cache
     * @param delay defines how frequently the CacheManager will be started
     */
    public void init(int size, int delay) {
        if (cacheManager == null) {
            synchronized (OCSPCache.class) {
                if (cacheManager == null) {
                    cacheManager = new CacheManager(cache, size, delay);
                    CacheController mbean = new CacheController(cache, cacheManager);
                    MBeanRegistrar.getInstance().registerMBean(mbean, "CacheController", "OCSPCacheController");
                }
            }
        }
    }

    /**
     * This method is needed by the cache Manager to go through the cache entries to remove invalid values or
     * to remove LRU cache values if the cache has reached its max size.
     *
     * @return next cache value of the cache.
     */
    public ManageableCacheValue getNextCacheValue() {
        //Changes to the hash map are reflected on the keySet. And its iterator is weakly consistent. so will never
        //throw concurrent modification exception.
        if (iterator.hasNext()) {
            return hashMap.get(iterator.next().getKey());
        } else {
            resetIterator();
            return null;
        }
    }

    /**
     * @return the current cache size (size of the hash map)
     */
    public int getCacheSize() {
        return hashMap.size();
    }

    public void resetIterator() {
        iterator = hashMap.entrySet().iterator();
    }


    public synchronized SingleResp getCacheValue(BigInteger serialNumber) {
        OCSPCacheValue cacheValue = hashMap.get(serialNumber);
        if (cacheValue == null) {
            return null;
        }
        //If someone gets this cache value before cache manager task found it is invalid, update it and get the
        // new value.
        if (!cacheValue.isValid()) {
            cacheValue.updateCacheWithNewValue();
            OCSPCacheValue ocspCacheValue = hashMap.get(serialNumber);
            return (ocspCacheValue != null ? ocspCacheValue.getValue() : null);
        }

        return cacheValue.getValue();
    }

    public synchronized OCSPResp getOCSPCacheValue(BigInteger serialNumber) {
        OCSPCacheValue cacheValue = hashMap.get(serialNumber);
        if (cacheValue != null) {
            if (!cacheValue.isValid()) {
                cacheValue.updateCacheWithNewValue();
                OCSPCacheValue ocspCacheValue = hashMap.get(serialNumber);
                return (ocspCacheValue != null ? ocspCacheValue.getOCSPValue() : null);
            }

            return cacheValue.getOCSPValue();
        } else {
            return null;
        }
    }

    public synchronized void setCacheValue(OCSPResp ocspResp, BigInteger serialNumber, SingleResp singleResp,
            OCSPReq request, String serviceUrl) {
        OCSPCacheValue cacheValue = new OCSPCacheValue(ocspResp, serialNumber, singleResp, request, serviceUrl);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Before setting - HashMap size {}", hashMap.size());
        }
        hashMap.put(serialNumber, cacheValue);
        if (LOG.isDebugEnabled()) {
            LOG.debug("After setting - HashMap size {}", hashMap.size());
        }
    }

    public synchronized void removeCacheValue(BigInteger serialNumber) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Before removing - HashMap size {}", hashMap.size());
        }
        hashMap.remove(serialNumber);
        if (LOG.isDebugEnabled()) {
            LOG.debug("After removing - HashMap size {}", hashMap.size());
        }
    }

    /**
     * This is the wrapper class of the actual cache value which is a SingleResp.
     */
    private class OCSPCacheValue implements ManageableCacheValue {

        private BigInteger serialNumber;
        private SingleResp singleResp;
        private OCSPReq request;
        private String serviceUrl;
        private OCSPResp ocspResp;
        private long timeStamp = System.currentTimeMillis();

        public OCSPCacheValue(OCSPResp ocspResp, BigInteger serialNumber, SingleResp singleResp, OCSPReq request,
                String serviceUrl) {
            this.serialNumber = serialNumber;
            this.singleResp = singleResp;
            //request and serviceUrl are needed to update the cache with new values.
            this.request = request;
            this.serviceUrl = serviceUrl;
            this.ocspResp = ocspResp;
        }

        public BigInteger getKey() {
            return serialNumber;
        }

        public SingleResp getValue() {
            timeStamp = System.currentTimeMillis();
            return singleResp;
        }

        public OCSPResp getOCSPValue() {
            timeStamp = System.currentTimeMillis();
            return ocspResp;
        }

        /**
         * An OCSP response is valid only during it's validity period. So check whether CA's response has expired.
         */
        public boolean isValid() {
            Date now = new Date();
            Date nextUpdate = singleResp.getNextUpdate();
            return nextUpdate != null && nextUpdate.after(now);
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        /**
         * Used by cacheManager to remove invalid entries.
         */
        public void removeThisCacheValue() {
            removeCacheValue(serialNumber);
        }

        public void updateCacheWithNewValue() {
            replaceNewCacheValue();
        }

        //This has to be synchronized because several threads will try to replace cache value
        // (cacheManager and Reactor thread)
        private synchronized void replaceNewCacheValue() {
            //If someone has updated with the new value before current Thread.
            if (isValid()) {
                return;
            }

            try {
                OCSPResp response = OCSPVerifier.getOCSPResponce(serviceUrl, request);

                if (OCSPResponseStatus.SUCCESSFUL != response.getStatus()) {
                    throw new CertificateVerificationException(
                            "OCSP response status was not SUCCESSFUL. Found OCSPResponseStatus:" +
                                    response.getStatus());
                }

                BasicOCSPResp basicResponse = (BasicOCSPResp) response.getResponseObject();
                SingleResp[] responses = (basicResponse == null) ? null : basicResponse.getResponses();

                if (responses == null) {
                    throw new CertificateVerificationException("Unable to get OCSP response.");
                }

                SingleResp resp = responses[0];
                setCacheValue(response, serialNumber, resp, request, serviceUrl);

            } catch (CertificateVerificationException | OCSPException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Can not replace old CacheValue with new CacheValue. So removing ocsp cache value", e);
                }
                //If cant be replaced remove.
                removeThisCacheValue();
            }
        }
    }
}

