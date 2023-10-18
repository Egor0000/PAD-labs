#!/bin/bash

# Set your Docker image and tag
IMAGE_NAME="service-discovery"
IMAGE_TAG="latest"

# Push the Docker image to the container registry using Docker config credentials
docker tag "$IMAGE_NAME:$IMAGE_TAG" egorbabcinetchi/"$IMAGE_NAME:$IMAGE_TAG"

docker push egorbabcinetchi/"$IMAGE_NAME:$IMAGE_TAG"

# Check if the push was successful
if [ $? -eq 0 ]; then
    echo "Docker image $IMAGE_NAME:$IMAGE_TAG pushed to $REGISTRY_SERVER."
else
    echo "Failed to push Docker image to $REGISTRY_SERVER."
fi
