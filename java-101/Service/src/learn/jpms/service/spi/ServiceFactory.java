package learn.jpms.service.spi;

import learn.jpms.service.impl.DeclaringModuleServiceImpl;

public class ServiceFactory {
    public static ServiceSpecification getService() {
        return new DeclaringModuleServiceImpl();
    }
}
