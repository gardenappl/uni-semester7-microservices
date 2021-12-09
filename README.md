Car rental project.

Spring Boot + Docker + Kubernetes

Front-end is here:
https://github.com/gardenappl/uni-semester6-oop/tree/semester7-micro/lab2-front

## Deploy to K8s

`kubectl apply -f deployment.yml`

## Building Docker images

Build images using gradle:

```
cd car-rental-cars
./gradlew buildBootImage
cd -

cd car-rental-requests
./gradlew buildBootImage
cd -

# ...
```
