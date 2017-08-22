# ticket-service
Online seat reservation service

## Getting started
This module contaning all core operations with RESTful interface.

### Frameworks Used
* Spring Boot - 1.5.6.RELEASE
* Apache Commons Lang3 - 3.6
* Fasterxml Jackson Databind for - JSON operations for RESTful API
* JUnit
* Mockito

### Other Dependency Modules
* [ticket-service-dao](../ticket-service-dao): It has DAO classes for ticket-service
* [ticket-service-utils](../ticket-service-utils): It contains Beans and Utility classes for ticket-service & ticket-service-dao

### Environment Assumptions
* These components and service are built and tested with JDK 1.8.0_91
* It uses Maven 3.5.0
* Main module and its dependency modules can be built by simple maven commands
* Main module 'ticket-service' module contains shell scripts for effortless build, run and test. (These scripts can be only on Unix based machines)

## Cloning the repositories in your local machine
Please clone them in a single parent folder. This by assuming you have GIT CLI installed in your system.
### Cloning main repository: ticket-service
```
git clone https://github.com/ManishGhole/ticket-service.git ticket-service/
```
### Cloning DAO dependency: ticket-service-dao
```
git clone https://github.com/ManishGhole/ticket-service-dao.git ticket-service-dao/
```
### Cloning Utility classes dependency: ticket-service-utils
```
git clone https://github.com/ManishGhole/ticket-service-utils.git ticket-service-utils/
```

## Automatic Execution (Build -> Run -> Test)
This will show you, how to make use of automated scripts for build, run and test.
This can be used ONLY on Unix OS based systems, NOT on Windows based systems.
### Step 1: Building and Running the modules
Using your shell command line inteface, navigate to ticket-service repository folder. Now run below command within ticker-service folder. This command will build all three modules in a desired hierarchy.
```
$ sh build.sh
```
If you choose to build and then run immediately, then use command given below. It will build all three modules in a desired hierarchy and will bring up the RESTFful service. This will engage your shell prompt as it will be running (by NOT using nohup). For testing you will have navigate to your 
```
$ sh run.sh
```
### Step 2: Automatic Testing
Open another shell prompt and navigate to ticket-service repository folder and run below shell command. This will invoke few curl commands to make RESTful calls.
```
$ sh test.sh
```
You will see sample output like this.
```
*********************************************** SEAT HOLD REGULAR TEST *******************************************************************
Number of seats available: 
{"numSeatsAvailable":100}
************************************
Hold seats: 
{"seatHoldId":1}
************************************
Number of seats available: 
{"numSeatsAvailable":95}
************************************
Confirmed reservation: 
{"confirmationId":"J6NYPU1E-1"}
************************************
Number of seats available: 
{"numSeatsAvailable":95}
```
