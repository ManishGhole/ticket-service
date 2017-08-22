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
### Application configurations
* [application.properties](https://github.com/ManishGhole/ticket-service-dao/blob/master/src/main/resources/application.properties):
This contains application related configurarions
* [ticketData.properties](https://github.com/ManishGhole/ticket-service-dao/blob/master/src/main/resources/ticketData.properties)
This contains database (pretended) related configurarions

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
*********************************************** SEAT HOLD REGULAR TEST **************************************
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

## Step 3.2: Testing RESTful service using HttpClient
### numSeatsAvailable()
```
URL: http://localhost:8080/ticket-service/numSeatsAvailable/
Method: POST
```
### findAndHoldSeats(int numSeats, String customerEmail)
```
URL: http://localhost:8080/ticket-service/holdSeats/
Method: POST
Content-Type: application/json
Request-Body: {"numSeats":"5","email":"test@test.com"}
```
### reserveSeats(int seatHoldId, String customerEmail)
```
URL: http://localhost:8080/ticket-service/reserveSeats/
Method: POST
Content-Type: application/json
Request-Body: {"seatHoldId":"1","email":"test@test.com"}
```

# Assumptions & Notes
## What is seat type?
Based on postion of the seat from stage, seat type will be determined.
There are three seat types and seats will priced based on it.
* GOLD: Most preferable and closest to the stage (Ofcourse based row number and column number & configuration).
* SILVER: 2nd most preferrable seats in the hall.
* BRONZE: Rest of the seats other than GOLD & SILVER
## What is seat rank
Rank defines the hierarchy of seats. 1st seat will have higher rank and last seat will have lower rank.
Seats will be served based on first come first serve basis. Whoever comes first, will always get higher ranked seats.
### Seat type and ranking (Example)
Assume below configuration.
```
rows=5
cols=5
maxGoldRows=1
maxSilverRows=2
```
Then thats how seats will be ranked and typed.
```
*********** STAGE **********
S1    S2    S3    S4    S5  - GOLD
S6    S7    S8    S9    S10 - SILVER
S11   S12   S13   S14   S15 - SILVER
S16   S17   S18   S19   S20 - BRONZE
S21   S22   S23   S24   S25 - BRONZE
```

Here S1 will have highest rank and S25 will have lowest rank. But check below how first 10 seats are ranked.
```
S1  S2  S3  S4  S5  S10  S9  S8  S7  S6
```
Seats will be ranked on zigzag basis to make sure customers will always get consecutive seats.

# Below things could have done better
* TreeSet: I could have used used TreeSet for getting seats ordered by rank. But TreeSet was dropping elements. Check example program I created for that.
[Click here](https://github.com/ManishGhole/ticket-service/blob/master/src/test/java/com/myprojects/ticket/service/TicketServiceTest.java#L426-L438).
This will print the the output while running Junit test cases.
* File logging: Due to unknown to your environment and where to store the log file, I used only console appending.
* Application config: I bundled application confiuraton files within the project. In real life we don't bundle it with deployable artifacts.
* Junit coverage: I left out POJOs, simulators and exception blocks for testng coverage.
* Junit testing is done with application configuration but not with specific test configuration.
