import learn.jpms.service.impl.DeclaringModuleServiceImpl;
import learn.jpms.service.spi.ServiceSpecification;

//open module service {
module jpms.service {
    exports learn.jpms.service.spi to jpms.client, jpms.externalService, NonExistent;
    opens learn.jpms.service.impl to jpms.externalService, NonExistent, jpms.client;
    provides ServiceSpecification with DeclaringModuleServiceImpl;
}