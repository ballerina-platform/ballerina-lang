type Pet record {|
    string name;
    int 'type;
|};

public function main() {
    Pet[] pets = [];
    pets.push({});

    PetStore petStore = new;
    petStore.addPets({});
    petStore.addPet({});
}

class PetStore {

    function addPets(Pet[] pets) {
        
    }

    function addPet(Pet pet) {
        
    }
}
