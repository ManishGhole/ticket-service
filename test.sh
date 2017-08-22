clear
BASE_URL=localhost:8080/ticket-service
NUM_SEATS_AVAIL_URL=$BASE_URL/numSeatsAvailable/
HOLD_SEATS_URL=$BASE_URL/holdSeats/
RESERVE_SEATS_URL=$BASE_URL/reserveSeats/
SEAT_HOLD_EXPIRY=30

echo "\n\n*********************************************** SEAT HOLD REGULAR TEST *******************************************************************"
echo "Number of seats available: " 
curl $NUM_SEATS_AVAIL_URL

echo "\n\n************************************"
echo "Hold seats: " 
curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"5","email":"test@test.com"}' $HOLD_SEATS_URL

echo "\n\n************************************"
echo "Number of seats available: " 
curl $NUM_SEATS_AVAIL_URL

echo "\n\n************************************"
echo "Confirmed reservation: " 
curl -H "Content-Type: application/json" -X POST -d '{"seatHoldId":"1","email":"test@test.com"}' $RESERVE_SEATS_URL

echo "\n\n************************************"
echo "Number of seats available: " 
curl $NUM_SEATS_AVAIL_URL

echo "\n\n*********************************************** SEAT HOLD EXPIRY TEST ********************************************************************"
echo "Hold seats: " 
curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"5","email":"test@test.com"}' $HOLD_SEATS_URL

echo "\n\n************************************"
echo "Sleeping for ${SEAT_HOLD_EXPIRY} seconds to expire previous Seat hold: " 
sleep ${SEAT_HOLD_EXPIRY}s

echo "\n\n************************************"
echo "Expired reservation: " 
curl -H "Content-Type: application/json" -X POST -d '{"seatHoldId":"2","email":"test@test.com"}' $RESERVE_SEATS_URL

echo "\n\n************************************"
echo "Number of seats available: " 
curl $NUM_SEATS_AVAIL_URL


echo "\n\n*********************************************** HOUSE FULL TEST **************************************************************************"
echo "Hold seats: " 
curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"10","email":"test@test.com"}' $HOLD_SEATS_URL
curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"10","email":"test@test.com"}' $HOLD_SEATS_URL
curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"10","email":"test@test.com"}' $HOLD_SEATS_URL
curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"10","email":"test@test.com"}' $HOLD_SEATS_URL
curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"10","email":"test@test.com"}' $HOLD_SEATS_URL
curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"10","email":"test@test.com"}' $HOLD_SEATS_URL
curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"10","email":"test@test.com"}' $HOLD_SEATS_URL
curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"10","email":"test@test.com"}' $HOLD_SEATS_URL
curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"10","email":"test@test.com"}' $HOLD_SEATS_URL
curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"10","email":"test@test.com"}' $HOLD_SEATS_URL

echo "\n\n************************************"
echo "House full show: "
curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"10","email":"test@test.com"}' $HOLD_SEATS_URL


echo "\n\n************************************"
echo "Number of seats available: " 
curl $NUM_SEATS_AVAIL_URL

echo "\n************************************************************************************************************************************************"