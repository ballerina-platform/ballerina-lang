# Ballerina Container Support
Container support for Ballerina provides the implementation for packaging Ballerina programs with Docker using `ballerina docker` command.

This is available as part of the Ballerina Tools distribution.

## Building From Source
> Docker is required to build and enable Ballerina Container Support. To install Docker, follow the instructions on the [Docker Engine Installation Guide](https://docs.docker.com/engine/installation/). Make sure `docker` commands can be run without root/admin privileges.

Navigate to the source root and execute the following command.

```bash
mvn clean install
```

This will create a fat JAR file with the dependencies included, inside the `target` folder.

## Usage
Add the `ballerina-container-support-0.970.0-alpha1-SNAPSHOT.jar` file to `bre/lib/` folder in the Ballerina distribution.

### `ballerina docker` Command Line Usage
```
create docker images for Ballerina program files

Usage:
ballerina docker <package-name> [--tag | -t <image-name>] [--host | -H <docker-hostURL>] --help | -h --yes | -y

Flags:
  --tag, -t     docker image name. <image-name>:<version>
  --yes, -y     assume yes for prompts
  --host, -H    docker Host. http://<ip-address>:<port>
```
The `docker` command will detect the Ballerina file type (main vs service) from the file extension provided.

To create a Docker image from a Ballerina file, simply provide the file name as an argument.

```
$ ./ballerina docker helloWorld.bal
Build docker image [helloworld:latest] in docker host [localhost]? (y/n): y

Docker image helloworld:latest successfully built.

Use the following command to execute the archive in a container.
        docker run --name dirty_bubble -it helloworld:latest
```

Creating a Docker image for a Ballerina Service.

```
$ ./ballerina docker helloWorldService.balx
Build docker image [helloworldservice:latest] in docker host [localhost]? (y/n): y

Docker image helloworldservice:latest successfully built.

Use the following command to start a container.
        docker run -p 44558:9090 --name associated_bile -d helloworldservice:latest

Use the following command to inspect the logs.
        docker logs associated_bile

Use the following command to retrieve the IP address of the container
        docker inspect associated_bile | grep IPAddress

Ballerina service will be running on the following ports.
        http://localhost:44558
        http://<container-ip>:9090

Make requests using the format [curl -X <http-method> http://localhost:44558/<service-name>]
```

You can additionally provide a customized image name.

```
$ ./ballerina docker helloWorld.bal -t myhelloworld:0.1
Build docker image [myhelloworld:0.1] in docker host [localhost]? (y/n): y

Docker image myhelloworld:0.1 successfully built.

Use the following command to execute the archive in a container.
        docker run --name annual_avenue -it myhelloworld:0.1
```
If the operating system is Windows or Docker daemon is not available on localhost, specify the Docker host as follows:

```
$ ./ballerina docker helloWorld.bal -H https://<docker-host>:<docker-port>
Build docker image [helloworld:latest] in docker host [https://<docker-host>:<docker-port>]? (y/n): y

Docker image helloworld:latest successfully built.

Use the following command to execute the archive in a container.
        docker run --name foolish_bronze -it helloworld:latest
```

# Ballerina Docker Image
The Docker distribution for Ballerina is available on Docker Hub as `ballerinalang/ballerina`. To run a Ballerina package using the Ballerina Docker image, simply mount the folder containing the file to `/ballerina/files` folder inside the container.

```bash
# Create a directory and copy the packages needed to be run.
mkdir -p ~/ballerina/packages/
cp mypackage.balx ~/ballerina/packages/
chmod +r ~/ballerina/packages/mypackage.balx

# Run container with the volume mount
docker run -v ~/ballerina/packages:/ballerina/files -it ballerinalang/ballerina:0.970.0-alpha1-SNAPSHOT
```

If you are running a Ballerina Service,

```bash
cp mysvcpackage.balx ~/ballerina/packages/
chmod +r ~/ballerina/packages/mysvcpackage.balx

docker run -v /tmp/testb:/ballerina/files -p 9090:9090 -it ballerinalang/ballerina:0.970.0-alpha1-SNAPSHOT
```
## License
Ballerina Container Support is licensed under [Apache License v2](LICENSE).

