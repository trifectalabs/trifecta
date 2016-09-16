
# Condor 

![Condor](https://cloud.githubusercontent.com/assets/4472397/9979661/1cd96e00-5f2d-11e5-859c-db798a8b492b.png)

=======
General infrastructure of Trifecta. Includes configuration, deployment, and general upkeep of services.


##The Stack


####Docker
-----
Docker is a container platform that allows for shipping code in an isolated space. This makes setting up an application's runtime very easy. Trifecta services each run in their own stateless docker container, allowing for quick, independent scaling. 
    
####Mesos
-----
Apache Mesos is a distributed systems kernel, that abstracts away CPU, memory, storage and other resources from the machines it manages. It is tasking engine that enables fault-tolerance and elastic distributed systems easily. Tasks get paired with offers from the resources, such that multiple instances of services can be load balanced on a cluster of slave nodes.

Trifecta services are considered 'tasks' in Mesos. This enables fault tolerance (if one instance of a service dies, another will spawn and take its place), and for elastic scalability to meet demand (if thresholds are met, more 'tasks' of the service will be spawned and load balanced to meet the new demand)

####Marathon
-----
Marathon is a task scheduler framework for Mesos. It handles service deployment, converting the service to be run into a respective Mesos task. Marathon offers a dashboard of all the services running, and allows for manual scaling up/down, service deployment, restart and killing. 

####HAProxy
-----
HAProxy is the proxy and load balancer that routes traffic to the appropriate instances where services will live. When a new instance of a service is spun up, it must be included in the service pool to receive traffic. HAProxy rules will be updated (not done yet, but probably with [Bamboo](https://github.com/QubitProducts/bamboo)) as services are created in Marathon.




##Local Dev
The process for testing out a service you're developing or testing on your local machine is quick and easy. 

Before you can start, make sure that you have [Docker 1.9](https://www.docker.com/docker-toolbox) and Docker Compose 1.5 (should be included in Docker toolbox) installed. 

1) Start Mesos cluster on your Mac OS X machine.

Navigate to the condor/docker directory and 

	docker-compose up
	
2) Launch any services that you may need. Marathon service descriptions live in condor/marathon/apps/, and can be launched with 

	marathonctl create [service_file]
	
3) Build the Docker image for your service. *TODO:* For Play applications that extend TrifectaPlay project, this will be easy

	sbt dockerBuildAndPush
	
4) Create a Marathon service description for your service. *TODO:* Play applications should extend the TrifectaPlay project to make this a bit easier 

5) Launch your service on your local Marathon instance!

	marathonctl create [your_new_service_file]
	
6) You can scale, destroy, update, restart, etc as needed. [marathonctl doc](https://github.com/shoenig/marathonctl).


##Random Instructions
####Make a new SSL certificate
When we receive an SSL certificate from Comodo (PositiveSSL), we normally receive the domain certificate and a CA bundle. In order to ensure proper functionality across all browsers/OS/services, you need to build the correct trust chain. To do this, simply concatenate the bundled certificates after your domain cert. Finally, you also need to include the private RSA key that was used to generate the certificate (host validation). 

    cat comodo_domain.crt comodo_domain.ca-bundle domain.key > domain.chained.crt

Now use that chained certificate everywhere needed. 