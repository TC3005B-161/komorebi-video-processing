# komorebi-video-processing

## Procesamiento de Video (Lambda)

Esta Lambda es parte de la arquitectura backend de Komorebi Connect. El repositorio de los servicios Web accesibles por medio de API se encuentra en el siguiente repositorio: https://github.com/TC3005B-161/komorebi-backend

### Código y Librerías 

Para el código del servicio de procesamiento de video se utilizó el modelo de programación Lambda de AWS. Esto significa que tenemos una función en una clase de Java que se encarga de recibir los eventos, procesarlos, y regresar una respuesta. Se integró con el servicio de SQS para tener la posibilidad de reintentar el procesamiento de una grabación en caso de que haya un error. Y además la integración de una Dead Letter Queue permite almacenar la información de los videos que no se pudieron procesar después de varios intentos. 

A nivel código, se deben incluir varias dependencias para interactuar con los distintos servicios. Se utilizó Maven para llevar el control de esto. A continuación se enlistan las librerías utilizadas: 
- aws-lambda-java-runtime-interface-client V1.0.0: Esta librería implementa el modelo de programación lambda de AWS. 
- aws-java-sdk-s3 V1.12.227: Permite el acceso al servicio de S3 por medio de un SDK. 
- jackson-mapper-asl V1.9.13: Utilería para trabajar con JSON 
- dynamodb-enhanced V2.17.154: Librería para interactuar con tablas en DynamoDB por medio de un SDK. 
- aws-lambda-java-events V3.11.0: Librería para recibir eventos a la Lambda desde otros servicios de AWS y parsearlos a un objeto de Java de manera sencilla. En este caso, para recibir eventos de SQS. 

### Configuración de autenticación AWS 

Para el funcionamiento de la Lambda no es necesario configurar credenciales a nivel código. Esto es posible gracias a que la función puede asumir un rol que tiene los permisos suficientes para interactuar con los servicios. El rol fue creado en un template de Cloudformation junto con el resto de la arquitectura. 

### Infraestructura de AWS

La especificación de la infraestructura de AWS se realizó por medio de un template de Cloudformation, y el despliegue se realizó utilizando SAM (Serverless Application Model). Los recursos creados fueron: 
- IAM Role para la ejecución de la Lambda
- IAM Policy para especificar las acciones permitidas al Role de la Lambda 
- Lambda Function deployada como imágen, con un Ephemeral Storage de 2GB para poder procesar videos de larga duración
- SQS Queue para recibir los mensajes desde nuestro controlador y enviarlos a la Lambda
- SQS Dead Letter Queue para almacenar los mensajes que se reintentaron cierto número de veces sin éxito. 
- Lambda Event Source Mapping para configurar que la Lambda recibe un evento desde la queue de SQS. 

Adicionalmente, se crearon dos buckets de AWS y se recuperó el ARN para colocarlo como parámetro en el template de Cloudformation. 

A continuación se muestra un pequeño extracto del template de Cloudformation: 

<img width="1159" alt="Screen Shot 2022-06-10 at 19 37 57" src="https://user-images.githubusercontent.com/45611081/173166198-b558688b-94a8-4065-90d5-6da8661d6442.png">


### Configuración CI/CD 

Para el despliegue de la aplicación se configuró un servidor Jenkins en una instancia de EC2 de tipo t2.small. Aquí se creó una pipeline que clona el repositorio de Github, ejecuta el comando SAM build, seguido de SAM package y SAM deploy. A continuación se muestra el Jenkinsfile:

<img width="972" alt="Screen Shot 2022-06-10 at 19 38 59" src="https://user-images.githubusercontent.com/45611081/173166211-e593c086-7f82-446c-b2a1-ed6b88a26f69.png">


Para lograr esto, la instancia de EC2 en la que se levantó el servidor Jenkins debe tener las siguientes configuraciones: 
- Instalación de Docker 
- Instalación de AWS CLI 
- Configuración de las credenciales de AWS (por medio de aws configure)
- Instalación de SAM (Serverless Application Model) V1.15.0




