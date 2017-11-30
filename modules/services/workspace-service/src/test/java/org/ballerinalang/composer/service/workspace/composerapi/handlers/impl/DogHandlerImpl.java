
package org.ballerinalang.composer.service.workspace.composerapi.handlers.impl;

import org.ballerinalang.composer.service.workspace.composerapi.endpoint.PetStoreEp;
import org.ballerinalang.composer.service.workspace.composerapi.handlers.interfaces.DogHandlerI;
import org.ballerinalang.composer.service.workspace.composerapi.model.Dog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DogHandlerImpl implements DogHandlerI {
    private final Logger log = LoggerFactory.getLogger(PetStoreEp.class);
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
        String message = "Only " + Arrays.toString(availableBreeds) + " are available till next month";
        log.info(message);
    }
}
