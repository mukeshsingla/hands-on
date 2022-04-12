package learn.jpms.service.another;

import learn.jpms.service.spi.ServiceSpecification;

public class ImplementingModuleServiceImpl implements ServiceSpecification {
    @Override
    public String getMessage() {
        return "Another secret message";
    }
}
