#!/bin/sh

if [ "$2" != "" ]; then
  PROFILE="--profile $2"
  echo "Using profile $PROFILE for deployment"
fi

ECR_REPOSITORY="${1}.dkr.ecr.us-east-1.amazonaws.com/lambda-deployment-images"
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
