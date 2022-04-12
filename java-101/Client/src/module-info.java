import learn.jpms.service.spi.ServiceSpecification;

module jpms.Client {
    requires java.sql;
    requires jpms.ExternalService;
    uses ServiceSpecification;
}