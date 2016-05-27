#!/bin/bash -x
# These are the commands we ran in the training to get the system running.  We ran them in different terminal windows.

cd Common/ && sbt publish-local && cd ../ && find . -maxdepth 1 -type d -exec sh -c "cd {}; sbt compile" \;

cd ServiceRegistry; sbt "run 9001" &
sleep 5
curl -H 'Content-Type: application/json' -X PUT -d '{"one":"1","two":"2","three":"3"}' http://localhost:9001/registry/service/Test/info

cd ../Topics; sbt "run 9002" &
sleep 5
curl -H 'Content-Type: application/json' -X POST -d '{"type":"test.Test","message":"test-payload"}' http://localhost:9002/topics/Test

cd ../Collaboration; sbt "run 9003" &
sleep 5
curl -X GET http://localhost:9003/testing

cd ../AgilePM; sbt "run 9004" &
sleep 5
curl -i -H 'Content-Type: application/json' -X POST -d '{"name":"Product1","description":"This is product 1.", "requestDiscussion":true}' http://localhost:9004/agilepm/products

cd ../IssueTracker; sbt "run 9005" &
sleep 5
