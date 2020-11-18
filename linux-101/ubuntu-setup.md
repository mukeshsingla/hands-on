# After installation
sudo apt update && sudo apt upgrade -y

sudo -i

apt install net-tools -y

apt install mdadm
apt install lvm2 -y

## Basic
echo "set showmode" >> ~/.vimrc	# Shows insert mode in VI editor

apt install vim-gui-common

## USB Wifi Drivers
https://forums.developer.nvidia.com/t/rtl88x2bu-wifi-usb-adapter-0bda-b812/106920

#for finding device details

https://medium.com/@EThaiZone/my-stupid-when-i-try-to-install-usb-device-driver-on-linux-2a8d052bc04c

    lsusb
    sudo apt update && sudo apt upgrade -y
    sudo apt install build-essential dkms git
    cd /home/msingla/Downloads/
    git clone https://github.com/cilynx/rtl88x2bu.git
    cd rtl88x2bu/
    VER=$(sed -n 's/\PACKAGE_VERSION="\(.*\)"/\1/p' dkms.conf)
    sudo rsync -rvhP ./ /usr/src/rtl88x2bu-${VER}
    sudo dkms add -m rtl88x2bu -v ${VER}
    sudo dkms build -m rtl88x2bu -v ${VER}
    sudo dkms install -m rtl88x2bu -v ${VER}
    sudo modprobe 88x2bu

Drivers will be installed

    modinfo 88x2bu
    lshw -C network
    rfkill list



## For extending life of SSD 

https://easylinuxtipsproject.blogspot.com/p/ssd.html#ID4

	- After the installation: noatime
	- Limiting swap wear
	- Limiting the disk write actions of Firefox
	- Some SSD's: set the scheduler to NOOP
	
		UUID=6e5af26e-631b-4103-83bc-b852e8a2784f /               ext4    noatime,errors=remount-ro 0       1
		# /ssdStore was on /dev/sda3 during installation
		UUID=f8ecaa46-e417-4cd6-afc9-f8a8a0bddd33 /ssdStore       ext4    noatime,defaults        0       2

mount -a

chmod 777 /ssdStore

systemctl status fstrim.timer
	
## LVM based RAID setup

### RAID Setup

https://www.digitalocean.com/community/tutorials/how-to-create-raid-arrays-with-mdadm-on-ubuntu-18-04#creating-a-raid-1-array

    fdisk -l	# then follow above instructions after doing 
    parted /dev/sdb mklabel msdos
    parted /dev/sdc mklabel msdos
    
    fdisk /dev/sdb
    fdisk /dev/sdc

#mdadm --examine /dev/sdb1 /dev/sdc1
#mdadm --create /dev/md0 --level=mirror --raid-devices=2 /dev/sdb1 /dev/sdc1

mdadm --examine /dev/sdb /dev/sdc
mdadm --create --verbose /dev/md0 --level=1 --raid-devices=2 /dev/sda /dev/sdb

less /proc/mdstat

mdadm --detail /dev/md0
mdadm --detail --scan --verbose | sudo tee -a /etc/mdadm/mdadm.conf
update-initramfs -u


### LVM - https://www.howtogeek.com/howto/40702/how-to-manage-and-use-lvm-logical-volume-management-in-ubuntu/, https://linuxhint.com/lvm-ubuntu-tutorial/
lvm
	lvmdiskscan
	pvcreate /dev/md0
	lvmdiskscan
	pvdisplay
	vgcreate hdrvg1 /dev/md0
	vgdisplay
	lvcreate -L 16G -n lv_swap hdrvg1
	lvcreate -L 50G -n lv_tmp hdrvg1
	lvcreate -l 100%FREE -n lv_data hdrvg1
	# For resizing
		lvresize -L -50G hdrvg1/lv_data
	lvmdiskscan
	lvdisplay
	lvscan

mkfs.ext4 /dev/hdrvg1/lv_tmp
mkfs.ext4 /dev/hdrvg1/lv_data

resize2fs -p /dev/mapper/hdrvg1-lv_tmp 
resize2fs -p /dev/mapper/hdrvg1-lv_data 

df -h


## Moving /home, /tmp and swap from SSD to HDD

### /home - https://help.ubuntu.com/community/Partitioning/Home/Moving
mkdir /mnt/home
mount /dev/hdrvg1/lv_data /mnt/home
blkid		# for UUIDs
vi /etc/fstab
	UUID=<replaceWithUUID> /mnt/home	ext4	defaults        0       2

mount -a
rsync -aXS --progress --exclude='/*/.gvfs' /home/. /mnt/home/.
diff -r /home /mnt/home -x ".gvfs/*"
vi /etc/fstab
	UUID=<replaceWithUUID> /home	ext4	defaults        0       2

cd / && sudo mv /home /old_home && sudo mkdir /home
mount -a


### /tmp - https://help.ubuntu.com/community/Partitioning/Home/Moving, https://kerneltalks.com/howto/how-to-move-tmp-on-a-separate-disk-as-a-separate-mount-point/
mkdir /mnt/tmp
mount /dev/hdrvg1/lv_tmp /mnt/tmp
blkid		# for UUIDs
vi /etc/fstab
	UUID=<replaceWithUUID-/dev/hdrvg1/lv_tmp> /mnt/tmp	ext4	defaults        0       2

mount -a
rsync -aXS --progress --exclude='/*/.gvfs' /tmp/. /mnt/tmp/.
diff -r /tmp /mnt/tmp -x ".gvfs/*"
vi /etc/fstab
	UUID=<replaceWithUUID-/dev/hdrvg1/lv_tmp> /mnt/home	ext4	defaults        0       2

cd / && sudo mv /tmp /old_tmp && sudo mkdir /tmp
mount -a

chmod 1777 /tmp

### swap - https://help.ubuntu.com/community/SwapFaq, https://docs.fedoraproject.org/en-US/Fedora/14/html/Storage_Administration_Guide/s2-swap-creating-lvm2.html
swapoff --all
mkswapon --all
mkswap -f /dev/hdrvg1/lv_swap
vi /etc/fstab
	/dev/hdrvg1/lv_swap swap        swap    default              0       0

swapon -v /dev/hdrvg1/lv_swap
cat /proc/swaps


- Reboot system

## Citrix Workspace - Download tar.gz
cd /home/msingla/Downloads/
mkdir citrix-workspace
mv linuxx64-20.06.0.15.tar.gz ./citrix-workspace
cd citrix-workspace
tar -xvf linuxx64-20.06.0.15.tar.gz
./setupwfc

ln -s /usr/share/ca-certificates/mozilla/* /opt/Citrix/ICAClient/keystore/cacerts	# Resolving SSL error 61 - https://support.citrix.com/article/CTX231524


