# Lab Setup

    # ubuntu-pm-01 as root
    apt update && apt upgrade -y
    apt install net-tools -y
    apt install smartmontools -y
    apt install lm-sensors -y
    apt install nfs-kernel-server -y
    systemctl status nfs-kernel-server
    
    # ubuntu-vm-01 as root
    apt update -y && apt upgrade -y
    apt install nfs-common -y
    apt install net-tools -y
    apt install nfs-common -y
    
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
    
    # Update hostnames in Win 10 hosts file at C:\Windows\System32\drivers\etc

## Storage
    # ubuntu-pm-01 as root
    nano /etc/fstab
        ## Add following
        # OS HDD - VG-LV Partition
        /dev/disk/by-uuid/d99bb891-1248-44b6-8a9c-9d1adb7f30a3 /storage/local-data-store ext4 defaults 0 2
        # SSD - 1 TB
        /dev/disk/by-uuid/ef6d5d99-ed7c-4911-b8c0-b6d2523a09eb /shared/ssd-01 ext4 noatime,defaults 0 2
        # HDD - 2 TB
        /dev/disk/by-uuid/ab7734fa-09f0-446a-8e0a-92c03bb7dff2 /shared/hdd-01 ext4 defaults 0 2
        
    lvcreate -l 100%FREE -n ubuntu-lv2 ubuntu-vg
    mkfs.ext4 /dev/ubuntu-vg/ubuntu-lv2

References
* https://linuxhint.com/lvm-ubuntu-tutorial/
* https://techguides.yt/guides/how-to-partition-format-and-auto-mount-disk-on-ubuntu-20-04/
* https://blog.shadypixel.com/monitoring-hard-drive-health-on-linux-with-smartmontools/

## Network
    # ubuntu-pm-01 as root
    ip a
    sudo nano /etc/netplan/00-installer-config.yaml
        ## Add following lines
        network:
          ethernets:
            eno1:
              addresses:
              - 192.168.1.171/24
              gateway4: 192.168.1.1
              nameservers:
                addresses: [1.1.1.1, 1.0.0.1]
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
          gateway4: 192.168.1.1
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

## NAS

    # ubuntu-pm-01 - as root
    mkdir -p /shared/hdd-01
    mkdir -p /shared/ssd-01
    chown -R msingla:msingla /shared/hdd-01
    chown -R msingla:msingla /shared/ssd-01
    nano /etc/exports
        ## Add following lines
        /storage/data-store  192.168.1.0/24(rw,sync,no_subtree_check,crossmnt)
        /storage/ssd-01  192.168.1.0/24(rw,sync,no_subtree_check,crossmnt)
        /storage/hdd-01  192.168.1.0/24(rw,sync,no_subtree_check,crossmnt)
    
    exportfs -ra
    exportfs -v
    service nfs-kernel-server restart
    
    # ubuntu-vm-01 as root
    mkdir /mnt/nfs-data-store
    mkdir /mnt/nfs-hdd-01
    mkdir /mnt/nfs-ssd-01
    chown -R msingla:msingla /mnt/*
    sudo mount -t nfs -o vers=4 192.168.1.102:/storage/data-store /mnt/nfs-data-store           # For test
    sudo nano /etc/fstab
        ## Add following
        # NFS - Shared by ubuntu-pm-01
        192.168.1.102:/storage/data-store   /mnt/nfs-data-store nfs     defaults,timeo=900,retrans=5,_netdev    0       0
        192.168.1.102:/storage/hdd-01   /mnt/nfs-hdd-01 nfs     defaults,timeo=900,retrans=5,_netdev    0       0
        192.168.1.102:/storage/ssd-01   /mnt/nfs-ssd-01 nfs     defaults,timeo=900,retrans=5,_netdev    0       0
    mount -a

References
* https://sysadminjournal.com/how-to-install-nfs-server-and-client-on-ubuntu-20-04/
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
    sudo sysctl -p
    sudo apt install -y apt-transport-https curl
    curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add
    sudo apt-add-repository "deb http://apt.kubernetes.io/ kubernetes-xenial main"
    sudo apt update
    sudo apt install -y kubelet kubeadm kubectl
    sudo kubeadm init
    mkdir -p .kube
    sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
    sudo chown $(id -u):$(id -g) $HOME/.kube/config
    
    # ubuntu-vm-02 as worker
    kubeadm join 192.168.1.172:6443 --token jq0wcr.w27lnnuae5fhaboc \
        --discovery-token-ca-cert-hash sha256:18eb9d322616cb71a58312eab5159776e25c57e72dc5495d7dddbd90ff7636c9
        
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

