/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.services.dispatchers.session;

import org.ballerinalang.bre.Context;

/**
 * PathMatcher will validate user entered path and match it with service flow
 */
public class PathMatcher {

    private static PathMatcher instance = null;


    public static PathMatcher getInstance() {
        if (instance == null) {
            PathMatcher.createNodeTree();
            instance = new PathMatcher();
        }
        return instance;
    }

    public static void createNodeTree() {

    }

    //This method will path check regex, validity and set sub tree
    public boolean pathValidityCheck(Context context, String path) {
        return true;
    }

    // This method will match req subpath with sub tree and return true if sub path contains inside the sub tree
    public boolean pathMatchWithURI(Context context, Session path) {
        return true;
    }

//    path and resource name mapping
//    context.getserviceInfo()resourceinfomap()
//        key = echo1 , value = value.attributeinfomap(AnnotationAttribute).attachmentlist = annotationatttachmentInfo key =value



}
