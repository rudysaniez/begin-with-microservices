#!/usr/bin/env bash

echo ""
cat test-banner.txt
echo ""

: ${HOST=localhost}
: ${PORT=9080}


function assertCurl() {

  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]
  then
    if [ "$httpCode" = "200" -o "$httpCode" = "201" ]; then
    	echo "Test OK (HTTP Code: $httpCode), $3"
    elif [ "$httpCode" = "404" -o "$httpCode" = "422" ]; then
    	echo "Test OK (HTTP Code: $httpCode, message: " $(echo $RESPONSE | jq .message) "), $3"
    else
    	echo "Test OK (HTTP Code: $httpCode, $RESPONSE), $3"
    fi
  else
      echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
      echo  "- Failing command: $curlCmd"
      echo  "- Response Body: $RESPONSE"
      exit 1
  fi
}


function assertEqual() {

  local expected=$1
  local actual=$2
  
  cleanDoubleQuote $2

  if [ "$CLEAN" = "$expected" ]
  then
    echo "Test OK (actual value: $CLEAN), $3"
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $CLEAN, WILL ABORT"
    exit 1
  fi
}

function cleanDoubleQuote() {

	local toclean=$1
	
	if [ "${toclean:0:1}" = "\"" -a "${toclean:(-1)}" = "\"" ]
	then
		toclean=${toclean%\"}
		toclean=${toclean#\"}
	else 
	
		if [ "${toclean:0:1}" = "\"" ]
		then
			toclean=${toclean#\"}
		else
			if [ "${toclean:(-1)}" = "\"" ]
			then
				toclean=${toclean%\"}
			fi
		fi
	fi
	
	CLEAN=$toclean
}

function testUrl() {

    url=$@
    if curl $url -ks -f -o /dev/null
    then
          echo "Ok"
          return 0
    else
          echo -n "not yet"
          return 1
    fi;
}


function waitForService() {

    url=$@
    echo -n "Wait for: $url... "
    n=0
    until testUrl $url
    do
        n=$((n + 1))
        if [[ $n == 100 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 30
            echo -n ", retry #$n "
        fi
    done
}


if [[ $@ == *"start"* ]]
then
	echo "Restarting the test environment..."
	echo "$ docker-compose down"
	docker-compose down
	echo "$ docker-compose up --build --detach"
	docker-compose up --build --detach
fi


waitForService http://$HOST:$PORT/api/v1/management/info

echo ""
echo " > Waiting... preparation of services..."
echo ""
#sleep 15

echo " > Launch the product-composite creation : PANNEAU_SOLAIRE."
assertCurl 201 "curl -X POST -H \"Content-Type: application/json\" -d '{\"productID\":1,\"name\":\"panneau_solaire\",\"weight\":1,\"recommendations\":[{\"recommendationID\":1,\"author\":\"rudysaniez\",\"rate\":1,\"content\":\"Good product!\"}]}' \"http://$HOST:$PORT/api/v1/products-composite\" -s " "Get a 201 response status : Product-composite is created (PANNEAU_SOLAIRE)."
assertCurl 200 "curl http://$HOST:$PORT/api/v1/products-composite/1 -s" "Get a 200 response status when get a product-composite with id=1"

assertEqual 1 $(echo $RESPONSE | jq .productID) "The productID is equals to 1."
assertEqual PANNEAU_SOLAIRE $(echo $RESPONSE | jq ".name") "The product name is equals to \"PANNEAU_SOLAIRE\"."
assertEqual 1 $(echo $RESPONSE | jq ".recommendations.content | length") "1 recommendation is found for the product-composite with id=1."

echo ""
echo " > Provoke a duplicate key exception."
assertCurl 422 "curl -X POST -H \"Content-Type: application/json\" -d '{\"productID\":1,\"name\":\"panneau_solaire\",\"weight\":1,\"recommendations\":[{\"recommendationID\":1,\"author\":\"rudysaniez\",\"rate\":1,\"content\":\"Good product!\"}]}' \"http://$HOST:$PORT/api/v1/products-composite\" -s " "Get a 422 response status : Duplicate key exception."

echo ""
echo " > Launch tests for get NOT_FOUND and UNPROCESSABLE_ENTITY status."
assertCurl 404 "curl http://$HOST:$PORT/api/v1/products-composite/999 -s" "Get a 404 response status when the productID is equals to 999"
assertCurl 422 "curl http://$HOST:$PORT/api/v1/products-composite/0 -s" "Get a 422 response status when the productID is equals to 0"

echo ""
echo " > Please wait for the second part of tests..."
#sleep 15

echo ""
echo " > Launch the deletion of product-composite with the id=1."
assertCurl 200 "curl -X DELETE http://localhost:9080/api/v1/products-composite/1 -s " "Get a 200 response status when deleting a product-composite with id=1 (PANNEAU_SOLAIRE)"

echo ""
echo " > Launch the creation of product-composite :  PONCEUSE"
assertCurl 201 "curl -X POST -H \"Content-Type: application/json\" -d '{\"productID\":2,\"name\":\"Ponceuse\",\"weight\":1,\"recommendations\":[{\"recommendationID\":2,\"author\":\"rsaniez\",\"rate\":2,\"content\":\"This tool is a good product.\"}],\"reviews\":[{\"reviewID\":2,\"author\":\"rsaniez\",\"subject\":\"My opinion\",\"content\":\"Good product, and powerful!\"}]}' \"http://$HOST:$PORT/api/v1/products-composite\" -s " "Get a 201 response status : Product-composite is created (PONCEUSE)."
assertCurl 200 "curl http://$HOST:$PORT/api/v1/products-composite/2 -s " "Get a 200 response status when get a product-composite with id=2."

assertEqual PONCEUSE $(echo $RESPONSE | jq ".name") "The product name is equals to \"PONCEUSE\"."
assertEqual 1 $(echo $RESPONSE | jq ".recommendations.content | length") "1 recommendation is found for the product-composite with id=2."
assertEqual 1 $(echo $RESPONSE | jq ".reviews.content | length") "1 review is found for the product-composite with id=2."

echo ""
echo " > Launch the deletion of product-composite with the id=2."
assertCurl 200 "curl -X DELETE http://localhost:9080/api/v1/products-composite/2 -s " "Get a 200 response status when deleting a product with id=2 (PONCEUSE)."

echo ""
cat ./test-banner-completed.txt
echo ""

if [[ $@ == *"stop"* ]]
then
	echo "Stopping the test environment..."
	echo "$ docker-compose down"
	docker-compose down
fi
