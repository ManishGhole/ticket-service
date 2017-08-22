# ticket-service - Seat reservation service.
This module contaning all core operations with RESTful interface.

### Other Dependency Modules
* [ticket-service-dao](https://github.com/ManishGhole/ticket-service-dao): It has DAO classes for ticket-service
* [ticket-service-utils](https://github.com/ManishGhole/ticket-service-utils): It contains Beans and Utility classes for ticket-service & ticket-service-dao

### Environment Assumptions
* These components and service are built and tested with JDK 1.8.0_91
* It uses Maven 3.5.0
* Main module and its dependency modules can be built by simple maven commands
* Main module 'ticket-service' module contains shell scripts for effortless build, run and test. (These scripts can be only on Unix based machines)

# Getting Started
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


# Automatic Execution (Build -> Run -> Test)
This will show you, how to make use of automated scripts for build, run and test.
This can be used ONLY on Unix OS based systems, NOT on Windows based systems.
Internally it uses shell, maven and curl commands.

## Step 1: Building all modules and running RESTful service
Using your shell command line inteface, navigate to ticket-service repository folder. Now run below command within ticker-service folder. This command will build all three modules in a desired hierarchy.
```
$ sh build.sh
```
If you choose to build and then run immediately, then use command given below. It will build all three modules in a desired hierarchy and will bring up the RESTFful service. This will engage your shell prompt as it will be running (by NOT using nohup). For testing you will have navigate to your 
```
# this will run RESTful service
$ sh run.sh
```

## Step 2: Automatic Testing
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


# Manual Execution (Build ->| Run ->| Test)
This will demonstrate manual build, run and test.
## Step 1: Build
Build below modules in a given order using below commands
### ticket-service-utils
```
# Navigate to ticket-service-utils
$ mvn clean install
```
### ticket-service-dao
```
# Navigate to ticket-service-dao
$ mvn clean install
```
### ticket-service
```
# Navigate to ticket-service
$ mvn clean package
```

## Step 2: Run RESTful service
Below command will run RESTful service
```
# Navigate to ticket-service
$ mvn spring-boot:run
```

## Step 3.1: Test RESTful service using curl
For testing you can use below curl commands (Only for Unix OS based systems).
### numSeatsAvailable()
```
# Sample request command
$ curl localhost:8080/ticket-service/numSeatsAvailable/
# Sample Response
$ {"numSeatsAvailable":100}
```

### findAndHoldSeats(int numSeats, String customerEmail)
```
# Sample request command
$ curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"5","email":"test@test.com"}' localhost:8080/ticket-service/holdSeats/
# Sample Response
$ {"seatHoldId":1}
```

### reserveSeats(int seatHoldId, String customerEmail)
```
# Sample request command
$ curl -H "Content-Type: application/json" -X POST -d '{"seatHoldId":"1","email":"test@test.com"}' localhost:8080/ticket-service/reserveSeats/
# Sample Response
$ {"confirmationId":"J6NYPU1E-1"}
```
