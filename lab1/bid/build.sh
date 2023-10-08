#!/bin/bash

# Replace 'your-app' with your actual application name
APP_NAME="bid-service-prod"

start=$(date +%s.%N)

export DOCKER_BUILDKIT=1

# Build the Docker image
docker build --network=host -t "$APP_NAME":latest -f docker/Dockerfile .

docker rmi -f $(docker images -f "dangling=true" -q)

docker save "$APP_NAME"

# end time
end=$(date +%s.%N)
# run time
runtime=$(python3 -c "print(${end} - ${start})")
echo "Runtime was $run  time seconds"