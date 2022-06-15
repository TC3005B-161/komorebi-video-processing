# komorebi-video-processing

## Video Processing (Lambda)

This Lambda is part of the Komorebi Connect backend architecture. The repository containing the corresponding web services accessible via API is found in the following repository: https://github.com/TC3005B-161/komorebi-backend. 

The purpose of the Lambda is to process different files for the creation of a unified recording of an Amazon Connect call. The input files are:
- Audio file recorded by Amazon Connect
- Video and audio file recorded by Komorebi Connect. It contains not only the audio of the call, but also the one that comes after until the contact is ended after performing the After Call Work.
- Text string indicating the time the Amazon Connect call ends.

The Lambda was displayed using an image. The Docker container has FFMPEG, a recording processing tool, installed. By using Java code, Shell commands that are executed  are responsible for processing the files. The steps are shown below:

1. Make a query to the database to obtain the recording information
2. Download the audio from Amazon Connect
3. Download Komorebi Connect Recording
4. Separate the video and audio from the Komorebi Connect recording.
5. Extract the post-call audio from the full audio from the Komorebi Connect recording.
6. Merge Amazon Connect audio with Komorebi Connect post-call audio.
7. Merge the generated full audio with the video from the Komorebi Connect recording
8. Upload the generated file to an S3 bucket
9. Update the recording in the DB to indicate that the video has been processed

The SQS service was used for the Lambda invocation, since it allows a certain number of asynchronous retries to be made if the initial processing fails for any reason. Also, it is possible to process multiple recordings in batches in case of high concurrent usage in the system.

### Code and libraries 

Regarding the code of the video processing service, the AWS Lambda programming model was used. Meaning we have a function in a Java class that is responsible for receiving the events, processing them, and returning a response. It has been integrated with the SQS service to have the possibility to retry the processing of a recording in case of an error. And also the integration of a Dead Letter Queue, allows you to store the information of the videos that could not be processed after several attempts.

At the code level, several dependencies must be included to interact with the different services. Maven was used to keep track of this. The libraries used are listed below:
- aws-lambda-java-runtime-interface-client V1.0.0: This library implements the AWS lambda programming model.
- aws-java-sdk-s3 V1.12.227: Allows access to the S3 service through an SDK.
- jackson-mapper-asl V1.9.13: Utility to work with JSON
- dynamodb-enhanced V2.17.154: Library to interact with tables in DynamoDB through an SDK.
- aws-lambda-java-events V3.11.0: Library to receive Lambda events from other AWS services and easily parse them to a Java object. In this case, to receive events from SQS.

### AWS Authentication Configuration 

For the Lambda to work, it is not necessary to configure credentials at the code level. This is possible because the role can assume a role that has sufficient permissions to interact with the services. The role was created in a Cloudformation template along with the rest of the architecture. 

### AWS Infrastructure

The specification of the AWS infrastructure was carried out through a Cloudformation template, and the deployment was carried out using SAM (Serverless Application Model). The resources created were:
- IAM Role for Lambda execution
- IAM Policy to specify the actions allowed to the Lambda Role
- Lambda Function deployed as an image, with a 2GB Ephemeral Storage to be able to process long videos
- SQS Queue to receive the messages from our controller and send them to the Lambda
- SQS Dead Letter Queue to store messages that have been retried a certain number of times without success.
- Lambda Event Source Mapping to configure that the Lambda receives an event from the SQS queue.

Additionally, two AWS buckets were created and the ARN was retrieved to place it as a parameter in the Cloudformation template.

Below is a small excerpt from the Cloudformation template: 

<img width="1159" alt="Screen Shot 2022-06-10 at 19 37 57" src="https://user-images.githubusercontent.com/45611081/173166198-b558688b-94a8-4065-90d5-6da8661d6442.png">


### CI/CD Configuration 

For the deployment of the application, a Jenkins server was configured in an EC2 instance of type t2.small. Here's a pipeline that clones the Github repository, runs the SAM build command, followed by SAM package and SAM deploy. Below is the Jenkinsfile:

<img width="972" alt="Screen Shot 2022-06-10 at 19 38 59" src="https://user-images.githubusercontent.com/45611081/173166211-e593c086-7f82-446c-b2a1-ed6b88a26f69.png">


To achieve this, the EC2 instance on which the Jenkins server was brought up must have the following configurations:
-Docker installation
- Installation of AWS CLI
- Configuration of AWS credentials (via aws configure)
- Installation of SAM (Serverless Application Model) V1.15.0




