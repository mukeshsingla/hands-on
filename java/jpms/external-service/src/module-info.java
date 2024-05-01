import learn.jpms.service.another.ImplementingModuleServiceImpl;
import learn.jpms.service.spi.ServiceSpecification;

//open module provider2 {
module jpms.externalService {
    requires transitive jpms.service;
    opens learn.jpms.service.another to jpms.client, NonExistent;
    provides learn.jpms.service.spi.ServiceSpecification with ImplementingModuleServiceImpl;
}