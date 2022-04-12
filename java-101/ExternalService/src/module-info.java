import learn.jpms.service.another.ImplementingModuleServiceImpl;
import learn.jpms.service.spi.ServiceSpecification;

//open module provider2 {
module jpms.ExternalService {
    requires transitive jpms.Service;
    opens learn.jpms.service.another to jpms.Client, NonExistent;
    provides ServiceSpecification with ImplementingModuleServiceImpl;
}