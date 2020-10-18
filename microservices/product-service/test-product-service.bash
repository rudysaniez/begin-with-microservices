#!/usr/bin/env bash

echo ""
cat test-banner.txt
echo ""

: ${HOST=localhost}
: ${PORT=8081}


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
            sleep 6
            echo -n ", retry #$n "
        fi
    done
}


###
# Test the expected http status with the result of the curl commannd passed in second parameter.
# The first parameter is the expected http status.
# Example : testExpectedHttpStatus "404" "curl -X GET http://localhost:8083/api/v1/reviews/1 -s"
###
function testExpectedHttpStatus() {

	local expectedHttpCode=$1
	local curlCmd="$2 -w \"%{http_code}\""
	local result=$(eval $curlCmd)
	local httpStatus="${result:(-3)}"
	
	if [ "$expectedHttpCode" = "$httpStatus" ]; then
		echo "Ok"
		return 0
	else
		return 1
	fi
}


###
# Wait a http status.
# For example waitHttpStatus 200 "curl -X GET http://localhost:8083/api/v1/products/1 -s ".
# If it's correct then the waits is completed.
###
function waitHttpStatus() {

	httpExpected=$1
	curlCommand=$2
    echo -n "Wait the http status: $httpExpected for curl command: $curlCommand... "
    n=0
    until testExpectedHttpStatus "$httpExpected" "$curlCommand"
    do
        n=$((n + 1))
        if [[ $n == 100 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 5
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
waitHttpStatus 404 "curl -X GET http://$HOST:$PORT/api/v1/products/1 -s "

assertCurl 201 "curl -X POST -H \"Content-Type: application/json\" -d '{\"productID\":1,\"name\":\"Panneau_solaire\",\"weight\":1}' \"http://$HOST:$PORT/api/v1/products\" -s " "Product creation"

assertEqual PANNEAU_SOLAIRE $(echo $RESPONSE | jq .name) "The product name is PANNEAU_SOLAIRE"

assertCurl 200 "curl -X GET \"http://$HOST:$PORT/api/v1/products?name=panneau\" -s " "Search the product named PANNEAU_SOLAIRE"

cleanDoubleQuote "$(echo $RESPONSE | jq .content[0].productID)"

assertCurl 200 "curl -X GET http://$HOST:$PORT/api/v1/products/$CLEAN -s " "Search a product by identifier"


echo ""
cat ./test-banner-completed.txt
echo ""


if [[ $@ == *"stop"* ]]
then
	echo "Stopping the test environment..."
	echo "$ docker-compose down"
	docker-compose down
fi
