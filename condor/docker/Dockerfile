FROM mesosphere/mesos-slave:0.25.0-0.2.70.ubuntu1404

# Install docker
RUN apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
RUN echo 'deb http://apt.dockerproject.org/repo ubuntu-trusty main' > /etc/apt/sources.list.d/docker.list
RUN apt-get update
RUN apt-get install docker-engine --yes
