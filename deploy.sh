#!/bin/sh

if [ "$1" != "" ]; then
  PROFILE="--profile $1"
  echo "Using profile $PROFILE for deployment"
fi

ECR_REPOSITORY="612928833356.dkr.ecr.us-west-2.amazonaws.com/lambda-deployment-images"
STACK_NAME="komorebi-video-processing"

sam build \
--use-container

sam package \
--output-template-file package.yaml \
--image-repository $ECR_REPOSITORY \
$PROFILE

sam deploy \
--stack-name $STACK_NAME \
--capabilities CAPABILITY_NAMED_IAM \
--image-repository $ECR_REPOSITORY \
--region us-west-2 \
$PROFILE
