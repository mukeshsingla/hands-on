# Powershell

#### To install help
    Get-Help -Name Get-NetIPInterface

#### Finding help on commands
    help <Command-Name> [-<parameters>]

## Network
#### Prioritizing Network traffic Interface
    Get-NetIPInterface | Sort-Object -Property AddressFamily,InterfaceIndex | Format-Table
        ifIndex InterfaceAlias                  AddressFamily NlMtu(Bytes) InterfaceMetric Dhcp     ConnectionState PolicyStore
        ------- --------------                  ------------- ------------ --------------- ----     --------------- -----------
        15      Ethernet                        IPv4                  1500               5 Enabled  Disconnected    ActiveStore
        6       Ethernet 2                      IPv4                  1500               5 Enabled  Disconnected    ActiveStore
        18      Wi-Fi                           IPv4                  1500              40 Disabled Connected       ActiveStore
        
    Set-NetIPInterface -InterfaceIndex 15 -InterfaceMetric 15
    Set-NetIPInterface -InterfaceIndex 6 -InterfaceMetric 15
    Set-NetIPInterface -InterfaceIndex 18 -InterfaceMetric 5

##### Reverting back changes
    Set-NetIPInterface -InterfaceIndex 15 -AutomaticMetric enabled
    Set-NetIPInterface -InterfaceIndex 6 -AutomaticMetric enabled
    Set-NetIPInterface -InterfaceIndex 18 -AutomaticMetric enabled

##### Adding routes to routing table
    route ADD 192.168.1.171 MASK 255.255.255.255 192.168.1.1 IF 6 metric 5
    route ADD 192.168.1.172 MASK 255.255.255.255 192.168.1.1 IF 6 metric 5
    route ADD 192.168.1.173 MASK 255.255.255.255 192.168.1.1 IF 6 metric 5