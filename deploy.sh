serviceName=weather-service
filePath=/data/Ai-Sensing/DeployCenter/weather-service

container_id=$(docker ps -a | grep ${serviceName} | awk '{print $1}')
echo "停止容器和删除容器"
if docker ps -a | grep ${serviceName} | awk '{print $1}'; then
 docker stop ${container_id}
 docker rm ${container_id}

 echo "删除镜像"
 docker rmi ${serviceName}
fi
echo "创建镜像"
cd ${filePath}
docker build -t ${serviceName} .

echo "启动容器"
docker run -itd -p 9010:9010 -v /data/Ai-Sensing/DataCenter/weather/tmp/:/data/Ai-Sensing/DataCenter/weather/tmp/ -v /data/Ai-Sensing/DataCenter/weather/export/:/data/Ai-Sensing/DataCenter/weather/export/ --name  weather-service weather-service

docker ps

