class MockListener {

    public function attach(service object {} s, string[]|string? name) returns error? {
        // do nothing
    }

    public function detach(service object {} s) returns error? {
        // do nothing
    }

    public function 'start() returns error? {
        // do nothing
    }

    public function gracefulStop() returns error? {
        // do nothing
    }

    public function immediateStop() returns error? {
        // do nothing
    }
}

service class DispatcherService {
   private map<SimpleHttpService> services = {};

   isolated resource function get event() returns error? {
      // Do stuff
   }

   isolated function addRef(SimpleHttpService serviceRef) returns error? {
      // add Reference to services map
   }

   isolated function removeRef() returns error? {
      // remove Reference from services map
   }
}

class CustomListener {
   private MockListener httpListener;
   private DispatcherService dispatcherService; //Service ref

   public isolated function init() returns error? {
      self.httpListener = new ();
      self.dispatcherService = new DispatcherService();
   }

    public function attach(SimpleHttpService serviceRef, () attachPoint) returns @tainted error? {
        check self.dispatcherService.addRef(serviceRef);
        check self.httpListener.attach(self.dispatcherService, attachPoint);
    }

    public function detach(service object {} s) returns error? {
        check self.dispatcherService.removeRef();
        return self.httpListener.detach(s);
    }

    public function 'start() returns error? {
        return self.httpListener.'start();
    }

    public function gracefulStop() returns @tainted error? {
        return self.httpListener.gracefulStop();
    }

    public function immediateStop() returns error? {
        return self.httpListener.immediateStop();
    }
}

public type SimpleHttpService service object {
   remote function onAppCreated();
};

listener CustomListener customListener = new ();

service SimpleHttpService on customListener {
   remote function onAppCreated() {

   }
}
