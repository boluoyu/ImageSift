FROM debian:jessie

RUN echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list
RUN echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886

RUN echo "deb http://ppa.launchpad.net/cwchien/gradle/ubuntu trusty main" | tee /etc/apt/sources.list.d/cwchien-gradle.list
RUN echo "deb-src http://ppa.launchpad.net/cwchien/gradle/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/cwchien-gradle.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 9D06AF36

RUN apt-get update --fix-missing

RUN apt-get install -y git curl build-essential

RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java8-installer oracle-java8-set-default gradle

COPY build/libs/ImageSearchSift-0.1.0.jar .
CMD ["java", "-jar", "ImageSearchSift-0.1.0.jar"]
EXPOSE 8080