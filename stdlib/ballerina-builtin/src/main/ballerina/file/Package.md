## Package overview
This package contains services that register listeners against a local folder and identify events that create, modify, and delete files.

## Samples
The sample given below shows how an endpoint is used to listen to the local folder. The `onCreate()` resource method gets invoked when a file is created inside the `target/fs` folder. Use the `onDelete()` and `onModify()` methods to listen to the delete and modify events.
```ballerina
endpoint file:Listener localFolder {
    path:"target/fs"
};
service fileSystem bind localFolder {
    onCreate (file:FileEvent m) {
    }
}
```
## Package content
