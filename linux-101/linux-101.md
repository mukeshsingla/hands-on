# Ubuntu 20.04

## Basic Commands
	whereis <command> | which <command>	# For finding the location of command

	sudo apt update
	sudo apt upgrade
	sudo apt dist-upgrade
	sudo apt autoremove
	sudo apt install update-manager-core
	sudo apt install vim-gui-common
	sudo apt install net-tools
	sudo apt install nfs-common

	## Sharing Folder in unix
	sudo apt-get install nfs-kernel-server
	sudo systemctl status nfs-kernel-server
	mkdir env

	sudo vi /etc/exports
		# Add following text to the EOF
		/home/msingla/env       192.168.1.151(rw,sync,no_subtree_check)

	sudo systemctl restart nfs-kernel-server
	sudo systemctl status nfs-kernel-server
	sudo exportfs

	#sudo apt install cifs-utils
	#mkdir ~/dev
	#sudo mount.cifs //192.168.1.151/dev ~/dev -o user=msingla

## .bashrc - add following content at EOF
	export DEV_ENV_BIN=/home/msingla/env/bin
	export JAVA_HOME=/home/msingla/env/jdk-11.0.6

	export IGNITE_HOME=/home/msingla/env/apache-ignite-2.8.0-bin
	export IGNITE_WORK_DIR=/home/msingla/env/apache-ignite-2.8.0-bin/work

	export PATH=$DEV_ENV_BIN:$PATH

## Storage
### VM extend storage - https://www.rootusers.com/how-to-increase-the-size-of-a-linux-lvm-by-expanding-the-virtual-machine-disk/


## Networking
### Drivers
	lspci -k

	# Getting connected to Internet
	ip link set dev wlp12s0 up
	wpa_supplicant -B -i wlp12s0 -c /etc/wpa_supplicant/wpa_supplicant.conf
	dhclient -r
	dhclient wlp12s0

	# Putting link down
	ip link set dev wlp12s0 down

	# For static IP address
	ip addr add 192.168.1.113/24 broadcast 192.168.1.255 dev wlp12s0
	ip route add default via 192.168.1.1

	# Before putting link down
	ip addr flush dev wlp12s0
	ip route flush dev wlp12s0

### Static IP
	sudo vi /etc/netplan/00-installer-config.yaml
		# This is the network config written by 'subiquity'
		network:
		ethernets:
			eth0:
			dhcp4: false
			addresses: [192.168.1.152/24]
			gateway4: 192.168.1.1
			nameservers:
					addresses: [1.1.1.1,1.0.0.1]
		version: 2

### Static IP - Add following to /etc/netplan/01-network-manager-all.yaml
	sudo less /etc/netplan/01-network-manager-all.yaml
	ethernets:
		eth0:
		dhcp4: no
		addresses: [192.168.1.151/24]
		gateway4: 192.168.1.1
		nameservers:
			addresses: [1.1.1.1,1.0.0.1] 

	sudo netplan try
	sudo netplan apply
	sudo systemctl restart network-manager

## Files/Storage
### Mounting server env folder
	sudo mount 192.168.1.152:/home/msingla/env env
	sudo vi /etc/fstab
		# Add following content EOF and restart system
		192.168.1.152:/home/msingla/env /home/msingla/env nfs auto,nofail,noatime,nolock,intr,tcp,actimeo=1800 0 0

### Resolving too many open files issue
	ulimit -Sn		# check soft limit
	ulimit -Hn		# check hard limit
	sudo vi /etc/security/limits.conf		# Paste following in the eof
		*         hard    nofile      500000
		*         soft    nofile      500000
		root      hard    nofile      500000
		root      soft    nofile      500000
		
	sudo vi /etc/pam.d/common-session
		# add this line to it and then logout
		session required pam_limits.so
		
### IntelliJ Increasing Inotify watches limit
	sudo vi /etc/sysctl.conf
		# Add following
		fs.inotify.max_user_watches = 524288
	sudo sysctl -p --system
	# Restart IDE

## XRDP
	# Installation
	sudo apt install -y xrdp
	sudo systemctl status xrdp
	sudo adduser xrdp ssl-cert
	sudo systemctl restart xrdp
	sudo systemctl enable --now xrdp
	sudo ufw enable
	sudo ufw allow 3389/tcp

	# For local like desktop with Dock, etc.
	D=/usr/share/ubuntu:/usr/local/share:/usr/share:/var/lib/snapd/desktop
	cat <<EOF > ~/.xsessionrc
	export GNOME_SHELL_SESSION_MODE=ubuntu
	export XDG_CURRENT_DESKTOP=ubuntu:GNOME
	export XDG_DATA_DIRS=${D}
	export XDG_CONFIG_DIRS=/etc/xdg/xdg-ubuntu:/etc/xdg
	EOF


## Installations
	sudo apt install openjdk-11-jdk

### Maven
	cd ~
	rsync -av ~/.m2 /mnt/dataStore/dev/maven
	mv .m2 .m2.bak
	ln -s ~/dev/maven/.m2 .m2

### MySQL
	sudo apt update
	sudo apt upgrade
	sudo apt install mysql-server
	sudo mysql_secure_installation
	sudo apt-get install libmysql-java

	pwd	msingla001
		
	systemctl status mysql.service	# to check whether its running
	sudo systemctl start mysql	# for starting
	sudo mysqladmin -p -u root version	# connecting to admin

	## Moving mysql data dir
	sudo mysql
	select @@datadir;
	sudo systemctl stop mysql
	sudo systemctl status mysql

	sudo rsync -av /var/lib/mysql /mnt/dataStore/dev/dbData
	sudo mv /var/lib/mysql /var/lib/mysql.bak
	cd /etc/mysql/mysql.conf.d/
	sudo cp mysqld.cnf mysqld.cnf.bak
	sudo vi mysqld.cnf		# Update location of dataDir to /mnt/dataStore/dev/dbData/mysql/

	## Updating apparmor config too for above
	cd /etc/apparmor.d/tunables
	sudo cp alias alias.bak
	sudo vi alias
	# Add at the bottom of file
		alias /var/lib/mysql/ -> /mnt/dataStore/dev/dbData/mysql/,

	sudo systemctl restart apparmor

	sudo mkdir /var/lib/mysql/mysql -p

	sudo systemctl start mysql
	sudo systemctl status mysql

	rm -rf /var/lib/mysql.bak


	sudo apt-get install libmysql-java		# JDBC drivers
		# export CLASSPATH=$CLASSPATH:/usr/share/java/mysql-connector-java.jar

### Apache Ignite
	https://console.gridgain.com	# Web Console	er.mukesh.singla@gmail.com/Muk35h4u
		Token:	436e8817-50ba-41b6-b12d-2869c6bd7f53

### ElasticSearch
	sudo vi /etc/sysctl.conf
		# Add following at EOF
		## Modified by msingla
		vm.max_map_count = 262144

	# Starting server
	nohup ignite &>> env/logs/ignite-homeLabServer-1.out &

	# Stop processes like Ctrl+C / SIGINT - Latest
	pkill -2 -g 53955

