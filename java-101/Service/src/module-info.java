import learn.jpms.service.impl.DeclaringModuleServiceImpl;
import learn.jpms.service.spi.ServiceSpecification;

//open module service {
module jpms.Service {
    exports learn.jpms.service.spi to jpms.Client, jpms.ExternalService, NonExistent;
    opens learn.jpms.service.impl to jpms.ExternalService, NonExistent, jpms.Client;
    provides ServiceSpecification with DeclaringModuleServiceImpl;
}