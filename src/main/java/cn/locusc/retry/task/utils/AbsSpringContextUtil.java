package cn.locusc.retry.task.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

public abstract class AbsSpringContextUtil implements ApplicationContextAware {
    private static ApplicationContext ac;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ac = applicationContext;
    }

    /**
     * 通过name获取 Bean.
     */
    public static Object getBean(String name) {
        return ac.getBean(name);
    }

    public static String[] getBeans() {
        return ac.getBeanDefinitionNames();
    }

    /**
     * 通过class获取Bean.
     */
    public static <T> T getBean(Class<T> clazz) {
        return ac.getBean(clazz);
    }

    /**
     * 发布事件
     */
    public static void publishEvent(ApplicationEvent event) {
        ac.publishEvent(event);
    }

    /**
     * 通过class获取Bean集合
     */
    public <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return ac.getBeansOfType(clazz);
    }

    public boolean containsBean(String var1) {
        return ac.containsBean(var1);
    }

}
