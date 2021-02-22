# Lab Setup

    # ubuntu-pm-01 as root
    apt update -y && apt upgrade -y && apt autoremove
    apt install net-tools -y
    apt install smartmontools -y
    apt install lm-sensors -y
    apt install nfs-kernel-server -y
    systemctl status nfs-kernel-server
    
    # ubuntu-vm-01 as root
    apt update -y && apt upgrade -y && apt autoremove
    apt install nfs-common -y
    apt install net-tools -y
    
    ## Clone from ubuntu-vm-01 after shutting down
    # ubuntu-vm-02 as root
    nano /etc/netplan/00-installer-config.yaml      # Update ip address for both wireless and ethernet: 192.168.1.164 & 192.168.3.104
    netplan try
    netplan apply
    ip a
    
    nano /etc/hostname      # Update hostname from ubuntu-vm-01 -> ubuntu-vm-02
    nano /etc/hosts         # Update hostname from ubuntu-vm-01 -> ubuntu-vm-02
    
    ## Add hosts names in /etc/hosts for all the machines correspondingly
    192.168.1.171 ubuntu-pm-01
    192.168.1.172 ubuntu-vm-01
    192.168.1.173 ubuntu-vm-02
     
    sudo apt update -y && sudo apt upgrade -y && sudo apt autoremove
    
    # Update hostnames in Win 10 hosts file at C:\Windows\System32\drivers\etc

## Storage
    # ubuntu-pm-01 as root
    nano /etc/fstab
        ## Add following
        # OS HDD - VG-LV Partition
        /dev/disk/by-uuid/d99bb891-1248-44b6-8a9c-9d1adb7f30a3 /storage/data-store ext4 defaults 0 0
        # SSD - 1 TB
        /dev/disk/by-uuid/ef6d5d99-ed7c-4911-b8c0-b6d2523a09eb /storage/ssd-01 ext4 noatime,defaults 0 0
        # HDD - 2 TB
        /dev/disk/by-uuid/ab7734fa-09f0-446a-8e0a-92c03bb7dff2 /storage/hdd-01 ext4 defaults 0 0
        # USB NTFS backup - Seagate
        /dev/disk/by-uuid/FAA49AFFA49ABD97 /storage/usb-backup-01 ntfs async,big_writes,noatime,nodiratime,nofail,umask-0007,uid-1000,gid-1000,rw 0 0
        # USB NTFS backup - Hitachi
        /dev/disk/by-uuid/7650905750902043 /storage/usb-backup-02 ntfs async,big_writes,noatime,nodiratime,nofail,umask-0007,uid-1000,gid-1000,rw 0 0
        
    lvcreate -l 100%FREE -n ubuntu-lv2 ubuntu-vg
    mkfs.ext4 /dev/ubuntu-vg/ubuntu-lv2

References
* https://linuxhint.com/lvm-ubuntu-tutorial/
* https://techguides.yt/guides/how-to-partition-format-and-auto-mount-disk-on-ubuntu-20-04/
* https://blog.shadypixel.com/monitoring-hard-drive-health-on-linux-with-smartmontools/

## Network
    # ubuntu-pm-01 as root

    ## USB Wifi Drivers 
    ### for finding device details 
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

    ## Drivers
    lspci -k

    # Getting connected to Internet
    ip link set dev wlp12s0 up
    wpa_supplicant -B -i wlp12s0 -c /etc/wpa_supplicant/wpa_supplicant.conf
    dhclient -r
    dhclient wlp12s0

    ip a
    sudo nano /etc/netplan/00-installer-config.yaml
        ## Add following lines
        network:
        #  ethernets:
        #    eno1:
        #      addresses:
        #      - 192.168.1.171/24
        #      gateway4: 192.168.1.163
        #      nameservers:
        #        addresses: [1.1.1.1, 1.0.0.1]
          ethernets: {}
          wifis:
            wlx1cbfcedb9c14:
            dhcp4: no
            dhcp6: no
            optional:true
              addresses:
              - 192.168.1.170/24
              gateway4: 192.168.1.1
              nameservers:
                addresses: [1.1.1.1, 1.0.0.1]
              access-points:
                "JAM_HOUSE_5G":
                  password: "19M14A02J"
          version: 2

Copy this content to file and replace interface name
    
    # ubuntu-vm-* as root
    # This is the network config written by 'subiquity'
    network:
      version: 2
      ethernets:
        ens33:
          addresses:
          - 192.168.1.172/24
          gateway4: 192.168.1.163
          nameservers:
            addresses:
            - 1.1.1.1
            - 1.0.0.1

<pre>
sudo netplan try
sudo netplan apply
ip a
route -n
</pre>

References
* https://forums.developer.nvidia.com/t/rtl88x2bu-wifi-usb-adapter-0bda-b812/106920
* https://medium.com/@EThaiZone/my-stupid-when-i-try-to-install-usb-device-driver-on-linux-2a8d052bc04c


## NAS

    # ubuntu-pm-01 - as root
    mkdir -p /storage/hdd-01
    mkdir -p /storage/ssd-01
    chown -R msingla:msingla /storage/hdd-01
    chown -R msingla:msingla /storage/ssd-01
    nano /etc/exports
        ## Add following lines
        /storage/data-store  192.168.1.0/24(rw,sync,no_subtree_check,crossmnt)
        /storage/ssd-01  192.168.1.0/24(rw,sync,no_subtree_check,crossmnt)
        /storage/hdd-01  192.168.1.0/24(rw,sync,no_subtree_check,crossmnt)
        /storage/usb-backup-01  192.168.1.0/24(rw,sync,no_subtree_check,crossmnt)
        /storage/usb-backup-02  192.168.1.0/24(rw,sync,no_subtree_check,crossmnt)
    
    exportfs -ra
    exportfs -v
    service nfs-kernel-server restart
    
    # ubuntu-vm-01 as root
    mkdir /mnt/nfs-data-store
    mkdir /mnt/nfs-hdd-01
    mkdir /mnt/nfs-ssd-01
    chown -R msingla:msingla /mnt/*
    sudo mount -t nfs -o vers=4 192.168.1.170:/storage/data-store /mnt/nfs-data-store           # For test
    sudo nano /etc/fstab
        ## Add following
        # NFS - Shared by ubuntu-pm-01
        192.168.1.170:/storage/data-store   /mnt/nfs-data-store nfs     defaults,timeo=900,retrans=5,_netdev    0       0
        192.168.1.170:/storage/hdd-01   /mnt/nfs-hdd-01 nfs     defaults,timeo=900,retrans=5,_netdev    0       0
        192.168.1.170:/storage/ssd-01   /mnt/nfs-ssd-01 nfs     defaults,timeo=900,retrans=5,_netdev    0       0
        192.168.1.170:/storage/usb-backup-01   /mnt/nfs-usb-backup-01 nfs     defaults,timeo=900,retrans=5,_netdev    0       0
        192.168.1.170:/storage/usb-backup-02   /mnt/nfs-usb-backup-02 nfs     defaults,timeo=900,retrans=5,_netdev    0       0
    mount -a

    # ubuntu-pm-01 - as root
    apt install samba samba-common python-glade2 system-config-samba
    whereis samba

    cp /etc/samba/smb.conf /etc/samba/smb.conf.backup
    nano /etc/samba/smb.conf
      # Add following
        # Edited by msingla
        netbios name = ubuntu-pm-01
        security = user

        # Sharing storage
        [data-store]
          comment = Data Store
          path = /storage/data-store
          browsable = yes
          writable = yes
          guest ok = yes
          read only = no
          force user = msingla
        
        [ssd-01]
          comment = SSD-01
          path = /storage/ssd-01
          browsable = yes
          writable = yes
          guest ok = yes
          read only = no
          force user = msingla
        
        [hdd-01]
          comment = HDD-01
          path = /storage/hdd-01
          browsable = yes
          writable = yes
          guest ok = yes
          read only = no
          force user = msingla

        [usb-backup-01]
          comment = USBBackup01
          path = /storage/usb-backup-01
          browsable = yes
          writable = yes
          guest ok = yes
          read only = no
          force user = msingla
        
        [usb-backup-02]
          comment = USBBackup02
          path = /storage/usb-backup-02
          browsable = yes
          writable = yes
          guest ok = yes
          read only = no
          force user = msingla

    testparm
    sudo service smbd restart

    ## Access and map the folders from Windows

References
* https://sysadminjournal.com/how-to-install-nfs-server-and-client-on-ubuntu-20-04/
* https://www.fosslinux.com/8703/how-to-setup-samba-file-sharing-server-on-ubuntu.htm
* https://ubuntu.com/tutorials/install-and-configure-samba#1-overview
* https://help.ubuntu.com/community/Samba/SambaServerGuide

## Kubernetes

    # ubuntu-vm-01 as master & ubuntu-vm-02 as worker
    sudo apt install -y docker.io
    sudo systemctl enable docker.service --now
    systemctl status docker
    docker --version
    ls -lh /etc/fstab*
    sudo cp /etc/fstab /etc/fstab.20201109
    sudo nano /etc/fstab
    sudo swapoff -a
    sudo nano /etc/sysctl.conf
        # Uncomment line - "net.ipv4.ip_forward = 1"
    sudo sysctl -p
    sudo apt install -y apt-transport-https curl
    curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add
    sudo apt-add-repository "deb http://apt.kubernetes.io/ kubernetes-xenial main"
    sudo apt update
    sudo apt install -y kubelet kubeadm kubectl
    
    # ubuntu-vm-01 as master
    sudo kubeadm init
    mkdir -p .kube
    sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
    sudo chown $(id -u):$(id -g) $HOME/.kube/config
    
    # ubuntu-vm-02 as worker
    sudo kubeadm join 192.168.1.172:6443 --token xuo76m.xdq1n4d0mnb105ro --discovery-token-ca-cert-hash sha256:39ceb1a2dae22dcac566507ebb5ab6d51a7cb4b487c54f449cf4d88c003761e2
        
    # Master Node  - ubuntu-vm-01
    kubectl get nodes
    kubectl apply -f https://docs.projectcalico.org/v3.14/manifests/calico.yaml
    kubectl get nodes
    kubectl get pods --all-namespaces
    echo 'source <(kubectl completion bash)' >>~/.bashrc
    source .bashrc

References
* https://www.linuxtechi.com/install-kubernetes-k8s-on-ubuntu-20-04/
* Other references
    * https://www.nakivo.com/blog/install-kubernetes-ubuntu/
    * https://phoenixnap.com/kb/install-kubernetes-on-ubuntu

