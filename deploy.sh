#!/bin/bash
echo "Building Angular frontend..."
cd document-web
ng build --configuration=production
cd ..

echo "Copying frontend to Spring Boot static folder..."
rm -rf App/document/Doc-Api/src/main/resources/static/*
cp -r frontend/dist/your-angular-app/* App/document/Doc-Api/src/main/resources/static/*

echo "Building Spring Boot WAR..."
cd backend
./gradlew clean bootWar

echo "Deploying to server..."
scp build/libs/myapp-1.0.0.war user@your-server:/opt/tomcat/webapps/

echo "Restarting Tomcat..."
ssh user@your-server "sudo systemctl restart tomcat"

echo "Deployment complete!"
