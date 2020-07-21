package learn.spring.framework;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.Consumer;

@Component("beanFactoryPostProcessor101")
public class BeanFactoryPostProcessor101 implements BeanFactoryPostProcessor {
    private static final String BEAN_NAME = "beanFactoryPostProcessor101";
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerAlias(BEAN_NAME, "aliasedBeanFactoryPostProcessor");
        beanFactory.getBeanNamesIterator().forEachRemaining(printer);
        System.out.println(String.format("Alias for %s: %s", BEAN_NAME, Arrays.toString(beanFactory.getAliases(BEAN_NAME))));
    }

    private Consumer<String> printer = value -> System.out.println(value);
}
