package learn.jpms.service.impl;

import learn.jpms.service.spi.ServiceSpecification;

public class DeclaringModuleServiceImpl implements ServiceSpecification {

    private static  String message = "Secret message";

    @Override
    public String getMessage() {
        return message;
    }
}
