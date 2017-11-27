/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.composer.service.workspace.ws.handlers;

import org.ballerinalang.composer.service.workspace.ws.ComposerApiHandler;
import org.ballerinalang.composer.service.workspace.ws.model.Pet;

import java.util.ArrayList;
import java.util.List;

public class ShowPets implements ComposerApiHandler<Pet> {
    List<String> pets = new ArrayList<>();
    @Override
    public String getMethodName() {
        return "showPets";
    }

    @Override
    public List<String> process(Pet petObj) {
        pets.add("Rottweiler");
        pets.add("Labrador");
        pets.add(petObj.getType());
        return pets;
    }
}
