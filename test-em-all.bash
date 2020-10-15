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
    if [ "$httpCode" = "200" -o "$httpCode" = "201" ]
    then
      echo "Test OK (HTTP Code: $httpCode, description=$3)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE, description=$3)"
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
    echo "Test OK (actual value: $CLEAN), description=$3"
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
            sleep 15
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

#assertCurl 201 "curl -X POST -H \"Content-Type: application/json\" -d '{\"productID\":1,\"name\":\"panneau_solaire\",\"weight\":1,\"recommendations\":[{\"recommendationID\":1,\"author\":\"rudysaniez\",\"rate\":1,\"content\":\"Good product!\"}],\"reviews\":[{\"reviewID\":1,\"author\":\"rudysaniez\",\"subject\":\"My opinion\",\"content\":\"Beautiful! it works\"}]}' \"http://$HOST:$PORT/api/v1/products-composite\" -s " "Product-composite creation"
assertCurl 201 "curl -X POST -H \"Content-Type: application/json\" -d '{\"productID\":1,\"name\":\"panneau_solaire\",\"weight\":1,\"recommendations\":[{\"recommendationID\":1,\"author\":\"rudysaniez\",\"rate\":1,\"content\":\"Good product!\"}]}' \"http://$HOST:$PORT/api/v1/products-composite\" -s " "Product-composite creation"

# Verify that a normal request works, expect three recommendations and three reviews
assertCurl 200 "curl http://$HOST:$PORT/api/v1/products-composite/1 -s" "Test of service named product-composite with ID=1"

assertEqual 1 $(echo $RESPONSE | jq .productId) "The productID is equals to 1"
assertEqual PANNEAU_SOLAIRE $(echo $RESPONSE | jq ".name") "The product name is \"PANNEAU_SOLAIRE\""
assertEqual 1 $(echo $RESPONSE | jq ".recommendations | length") "1 recommendation is found"
#assertEqual 1 $(echo $RESPONSE | jq ".reviews | length") "1 review is found"

assertCurl 404 "curl http://$HOST:$PORT/api/v1/products-composite/2 -s" "Get a 404 response status when the productId eq 1"

assertCurl 422 "curl http://$HOST:$PORT/api/v1/products-composite/0 -s" "Get a 422 response status when the productId eq 0"

if [[ $@ == *"stop"* ]]
then
	echo "Stopping the test environment..."
	echo "$ docker-compose down"
	docker-compose down
fi
