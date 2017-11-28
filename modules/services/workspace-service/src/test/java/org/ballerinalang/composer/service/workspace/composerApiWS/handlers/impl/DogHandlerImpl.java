
package org.ballerinalang.composer.service.workspace.composerApiWS.handlers.impl;

import org.ballerinalang.composer.service.workspace.composerApiWS.endpoint.PetStoreEp;
import org.ballerinalang.composer.service.workspace.composerApiWS.handlers.interfaces.DogHandlerI;
import org.ballerinalang.composer.service.workspace.composerApiWS.model.Dog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DogHandlerImpl implements DogHandlerI {
    private final Logger LOGGER = LoggerFactory.getLogger(PetStoreEp.class);
    List<String> dogsInStore = new ArrayList<>();

    /**
     * Add dogs
     * @param dog
     * @return
     */
    public CompletableFuture<List<String>> addDogs(Dog dog) {
        dogsInStore.add("Rottweiler");
        dogsInStore.add("Labrador");
        dogsInStore.add(dog.getBreed());
        return CompletableFuture.completedFuture(dogsInStore);
    }

    @Override
    public void notifyAvailabilityOfDogs(String [] availableBreeds) {
        String message = "Only " +Arrays.toString(availableBreeds) + " are available till next month";
        LOGGER.info(message);
    }
}
