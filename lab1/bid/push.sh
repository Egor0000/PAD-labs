#!/bin/bash

# Set your Docker image and tag
IMAGE_NAME="bid-service"
IMAGE_TAG="latest"

HUB_NAME="egorbabcinetchi"

# Push the Docker image to the container registry using Docker config credentials
docker tag "$IMAGE_NAME:$IMAGE_TAG" "$HUB_NAME/$IMAGE_NAME:$IMAGE_TAG"

docker push "$HUB_NAME/$IMAGE_NAME:$IMAGE_TAG"

# Check if the push was successful
if [ $? -eq 0 ]; then
    echo "Docker image $IMAGE_NAME:$IMAGE_TAG pushed to $HUB_NAME."
else
    echo "Failed to push Docker image to $HUB_NAME."
fi
