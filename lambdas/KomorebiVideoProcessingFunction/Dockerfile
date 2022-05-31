FROM public.ecr.aws/sam/build-java11:latest as maven
WORKDIR /usr/src/app/
COPY pom.xml .
RUN mvn package
COPY . .
RUN mvn -DskipTests=true package dependency:copy-dependencies

FROM public.ecr.aws/lambda/java:11

WORKDIR /var/task
ARG MAVEN_PATH=/usr/src/app/target
ARG FFMPEG_SRC=https://johnvansickle.com/ffmpeg/builds/ffmpeg-git-amd64-static.tar.xz

COPY --from=maven ${MAVEN_PATH}/classes .
COPY --from=maven ${MAVEN_PATH}/dependency/* lib/

RUN yum install -y wget tar xz
RUN wget ${FFMPEG_SRC}
RUN tar xf ffmpeg-git-amd64-static.tar.xz && \
    DIR=$(ls | grep -E ffmpeg-git-[0-9]{8}) && \
    cd $DIR && \
    mv ffmpeg ffprobe /usr/local/bin && \
    rm -rf ffmpeg-git-amd64-static.tar.xz && \
    rm -rf $DIR

CMD [ "com.komorebi.video.LambdaHandler::handleRequest" ]
