import learn.jpms.service.spi.ServiceSpecification;

module jpms.client {
    requires java.sql;
    requires jpms.externalService;
    uses learn.jpms.service.spi.ServiceSpecification;
}