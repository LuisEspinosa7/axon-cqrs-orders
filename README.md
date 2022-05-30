# DISTRIBUTED TRANSACTION EXAMPLE USING AXON AND CQRS - Orders MicroService

This a small distributed transaction example, developed using AXON (Evenet-source framework) and the CQRS (Command Query Resposability Segregation) 
which is an approach to design Microservices nowadays. It consists of an API GATEWAY (for communication), a SERVICE DISCOVERY (Eureka), 
USER Microservice (provides user info), PAYMENT Microservice (simulates a payment one), PRODUCTS Microservice (Creates and reserve products) and finally 
the ORDERS Microservice which contains the SAGA for managing the whole transaction of PLACING AND ORDER. It was developed by Luis Espinosa Llanos 
and it was used the following technologies and tools: 

<table style="width:100%">
  <tr>
    <td>
  	Core	
    </td>
    <td>
  	Java 11, Spring Boot 2, Data JPA, Hibernate, Loombok, Axon (Event store), Spring Cloud.
    </td>
  </tr>
  <tr>
    <td>
  	Design Patterns	
    </td>
    <td>
  	Api Gateway, Service Discovery, CQRS, Event Sourcing, 
    </td>
  </tr>
  <tr>
    <td>
  	Architectural Styles
    </td>
    <td>
  	MicroServices and RESTful
    </td>
  </tr>
  <tr>
    <td>
  	Database	
    </td>
    <td>
  	H2 (In Memory)
    </td>
  </tr>
  <tr>
    <td>
  	Server	
    </td>
    <td>
  	Apache Tomcat Embebido (Spring Boot)
    </td>
  </tr>
  <tr>
    <td>
  	IDE	
    </td>
    <td>
  	Spring Tool Suite
    </td>
  </tr>
  <tr>
    <td>
  	Executable	
    </td>
    <td>
  	Jar
    </td>
  </tr>
</table>

## Video
A video exposing the functionality of the proyect in local environment on a Desktop screen.

NOT YET.

## Pictures
Some pictures of the project on a local environment respectively:

NOT YET.

## Installation

Each proyect should be installed using the following command:
```bash
mvn clean install
```

## Usage
In the target folder you will find the Jar archive, of course the configuration for Login purposes was externalized, this is to say that
arguments have to be passed through command line, so please use the following command according to your values:

```bash
java -jar EACH_PROJECT.jar
```

## Contributing
This proyect is quite simple, and is part of my personal portfolio, so it is not intended to receive contributions.


## License
It is free.
