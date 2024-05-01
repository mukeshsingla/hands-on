### Event-Driven Architecture
**Patterns** 
* **Mark Richard - Mastering Patterns in Event-Driven Architecture**
    * _Fundamentals of Software Architecture - Chapter 14_
    * **Event Forwarding** - Guarantee no message loss
    * **Thread Delegate** - Increase performance and throughput while maintaining message processing order
    * **Ambulance** - Handling priority events but still maintain responsiveness for all other events (Carpool)
    * **Watch Notification** - Sending notification events to services without maintaining a persistent connection to those services
    * **Supervisor Consumer** - Handling varying request load and still maintain a consistent response time
    * **Multi-broker** - Increase the throughput and capacity of events through the system