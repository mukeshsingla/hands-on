## Docker	https://medium.com/@Grigorkh/how-to-install-docker-on-ubuntu-20-04-f1b99845959e
	

## mysql

### on docker
#### 192.168.1.152
docker run --name=mysql01 -d -e MYSQL_ROOT_PASSWORD=Muk35h4u -p 3306:3306 mysql:latest
docker run --name=mysql01 --mount type=bind,src=/mnt/d/development/env/mysql,dst=/var/lib/mysql --rm -d -e MYSQL_ROOT_PASSWORD=Muk35h4u -p 3306:3306 mysql:latest

# mysql
docker run --name=mysql01 -d -e MYSQL_ROOT_PASSWORD=Muk35h4u -p 3306:3306 mysql:latest


# DB
docker run --name master-mysql -e MYSQL_ROOT_PASSWORD=masterdb -d -p 3306:3306 mysql:8

# Jenkins - http://192.168.1.113:8000/login?from=%2F	mukesh/@pple`01
docker run --name master-jenkins -d -p 8000:8080 -p 50000:50000 jenkins:2.32.2


