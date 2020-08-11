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

package org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.crl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.MBeanRegistrar;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.cache.CacheController;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.cache.CacheManager;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.cache.ManageableCache;
import org.wso2.transport.http.netty.contractimpl.common.certificatevalidation.cache.ManageableCacheValue;

import java.security.cert.X509CRL;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Since a CRL maps to a CRL URL, the CRLCache should have x509CRL entries against CRL URLs.
 * This cache is a Singleton since it is shared by any transport which needs SSL certificate validation through CRL
 * verification and more than one CRLCache should not be allowed per system.
 */
public class CRLCache implements ManageableCache {

    private static volatile CRLCache cache;
    private static volatile Map<String, CRLCacheValue> hashMap = new ConcurrentHashMap<>();
    private volatile Iterator<Map.Entry<String, CRLCacheValue>> iterator = hashMap.entrySet().iterator();
    private volatile CacheManager cacheManager;
    private static CRLVerifier crlVerifier = new CRLVerifier(null);
    private static final Logger LOG = LoggerFactory.getLogger(CRLCache.class);

    private CRLCache() {
    }

    public static CRLCache getCache() {
        //Double checked locking
        if (cache == null) {
            synchronized (CRLCache.class) {
                if (cache == null) {
                    cache = new CRLCache();
                }
            }
        }
        return cache;
    }

    /**
     * This initializes the Cache with a CacheManager. If this method is called, a cache manager will not be used.
     *
     * @param size  max size of the cache
     * @param delay defines how frequently the CacheManager will be started
     */
    public void init(int size, int delay) {
        if (cacheManager == null) {
            synchronized (CRLCache.class) {
                if (cacheManager == null) {
                    cacheManager = new CacheManager(cache, size, delay);
                    CacheController mbean = new CacheController(cache, cacheManager);
                    MBeanRegistrar.getInstance().registerMBean(mbean, "CacheController", "CRLCacheController");
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
    public synchronized ManageableCacheValue getNextCacheValue() {
        //changes to the map are reflected on the keySet. And its iterator is weakly consistent. so will never
        //throw concurrent modification exception.
        if (iterator.hasNext()) {
            return hashMap.get(iterator.next().getKey());
        } else {
            resetIterator();
            return null;
        }
    }

    /**
     * To get the current cache size (size of the hash map).
     */
    public synchronized int getCacheSize() {
        return hashMap.size();
    }

    public void resetIterator() {
        iterator = hashMap.entrySet().iterator();
    }

    public synchronized X509CRL getCacheValue(String crlUrl) {
        CRLCacheValue cacheValue = hashMap.get(crlUrl);
        if (cacheValue != null) {
            //If someone gets this cache value before cache manager task found it is invalid, update it and get the
            // new value.
            if (!cacheValue.isValid()) {
                cacheValue.updateCacheWithNewValue();
                CRLCacheValue crlCacheValue = hashMap.get(crlUrl);
                return (crlCacheValue != null ? crlCacheValue.getValue() : null);
            }

            return cacheValue.getValue();
        } else {
            return null;
        }
    }

    public synchronized void setCacheValue(String crlUrl, X509CRL crl) {
        CRLCacheValue cacheValue = new CRLCacheValue(crlUrl, crl);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Before setting - HashMap size {}", hashMap.size());
        }
        hashMap.put(crlUrl, cacheValue);
        if (LOG.isDebugEnabled()) {
            LOG.debug("After setting - HashMap size {}", hashMap.size());
        }
    }

    public synchronized void removeCacheValue(String crlUrl) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Before removing - HashMap size {}", hashMap.size());
        }
        hashMap.remove(crlUrl);
        if (LOG.isDebugEnabled()) {
            LOG.debug("After removing - HashMap size {}", hashMap.size());
        }
    }

    /**
     * This is the wrapper class of the actual cache value which is a X509CRL.
     */
    private class CRLCacheValue implements ManageableCacheValue {

        private String crlUrl;
        private X509CRL crl;
        private long timeStamp = System.currentTimeMillis();

        public CRLCacheValue(String crlUrl, X509CRL crl) {
            this.crlUrl = crlUrl;
            this.crl = crl;
        }

        public String getKey() {
            return crlUrl;
        }

        public X509CRL getValue() {
            timeStamp = System.currentTimeMillis();
            return crl;
        }

        /**
         * CRL has a validity period. We can reuse a downloaded CRL within that period.
         * thisUpdate - (the time indicating that the CA knows this status is correct),
         * nextUpdate - (the time that newer information will be available,
         * implying that this information is the most accurate to date)
         */
        public boolean isValid() {
            Date today = new Date();
            Date nextUpdate = crl.getNextUpdate();
            return nextUpdate != null && nextUpdate.after(today);
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        /**
         * Used by cacheManager to remove invalid entries.
         */
        public void removeThisCacheValue() {
            removeCacheValue(crlUrl);
        }

        public void updateCacheWithNewValue() {
            replaceNewCacheValue();
        }

        private synchronized void replaceNewCacheValue() {
            //If someone has updated with the new value before current thread.
            if (isValid()) {
                return;
            }
            try {
                X509CRL x509CRL = crlVerifier.downloadCRLFromWeb(crlUrl);
                setCacheValue(crlUrl, x509CRL);
            } catch (Exception e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Cannot replace old CacheValue with new CacheValue. So remove", e);
                }
                //If cant be replaced remove.
                removeThisCacheValue();
            }
        }

    }
}

