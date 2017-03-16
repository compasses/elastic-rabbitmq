package springcommon;

/**
 * Created by I311352 on 12/29/2016.
 */
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHolder implements ApplicationContextAware, BeanFactoryAware {

    private static ApplicationContext applicationContext;
    private static BeanFactory beanFactory;

    private static final ThreadLocal<HttpServletRequest> request = new ThreadLocal<>();
    private static final ThreadLocal<HttpServletResponse> response = new ThreadLocal<>();

    private static final Map<Integer, Set<HttpSession>> httpSessions = new ConcurrentHashMap<>();

    private final static ReadWriteLock rwLock = new ReentrantReadWriteLock();

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        ApplicationContextHolder.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * @return the beanFactory
     */
    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }

    /**
     * @param clazz
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {

        if (clazz == null) {
            return null;
        }

        Lock rlock = rwLock.readLock();
        rlock.lock();
        try {
            return beanFactory == null ? null : beanFactory.getBean(clazz);
        } finally {
            rlock.unlock();
        }
    }

    public static <T> Collection<T> getBeans(Class<T> clazz) {
        Map<String, T> beans = null;
        Lock rlock = rwLock.readLock();
        rlock.lock();
        try {
            beans = applicationContext.getBeansOfType(clazz);
        } finally {
            rlock.unlock();
        }
        for (Iterator<Map.Entry<String, T>> iter = beans.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<String, T> entry = iter.next();
            if (entry.getKey().startsWith("scopedTarget")) {
                iter.remove();
            }
        }

        return beans.values();
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        Lock rlock = rwLock.readLock();
        rlock.lock();
        try {
            return beanFactory.getBean(name, clazz);
        } finally {
            rlock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name, Object... args) {
        Lock rlock = rwLock.readLock();
        rlock.lock();
        try {
            return (T) beanFactory.getBean(name, args);
        } finally {
            rlock.unlock();
        }
    }

    /**
     * @param
     * @return
     */
    public static Object getBean(String name) {
        Lock rlock = rwLock.readLock();
        rlock.lock();
        try {
            return beanFactory.getBean(name);
        } finally {
            rlock.unlock();
        }
    }

    public static <T> void registerBeanDefinition(Class<T> clazz, String scope) {
        BeanDefinition definition = new GenericBeanDefinition();
        definition.setScope(scope);
        definition.setBeanClassName(clazz.getName());
        Lock wlock = rwLock.writeLock();
        wlock.lock();
        try {
            ((DefaultListableBeanFactory) beanFactory).registerBeanDefinition(clazz.getName(), definition);
            // TODO : Has to register beans twice to flush cache, might be a Spring defect. Workaround.
            ((DefaultListableBeanFactory) beanFactory).registerBeanDefinition(clazz.getName(), definition);
        } finally {
            wlock.unlock();
        }
    }

    /**
     * @param request
     */
    public static void setRequest(HttpServletRequest request) {
        ApplicationContextHolder.request.set(request);
    }

    /**
     * @param response
     */
    public static void setResponse(HttpServletResponse response) {
        ApplicationContextHolder.response.set(response);
    }

    /**
     * @return
     */
    public static HttpServletRequest getRequest() {
        return ApplicationContextHolder.request.get();
    }

    /**
     * @return
     */
    public static HttpServletResponse getResponse() {
        return ApplicationContextHolder.response.get();
    }

    /**
     * @return
     */
    public static HttpSession getSession() {
        return ApplicationContextHolder.request.get().getSession();
    }

    /**
     * @return
     */
    public static Map<Integer, Set<HttpSession>> getHttpsessions() {
        return httpSessions;
    }

    /**
     * @param tenantId
     * @param httpSession
     */
    public static void addSession(Integer tenantId, HttpSession httpSession) {
        if (!httpSessions.containsKey(tenantId)) {
            httpSessions.put(tenantId, new HashSet<HttpSession>());
        }

        httpSessions.get(tenantId).add(httpSession);
    }

    /**
     * @param tenantId
     * @param httpSession
     */
    public static void removeSession(Integer tenantId, HttpSession httpSession) {
        if (httpSessions.containsKey(tenantId)) {
            httpSessions.get(tenantId).remove(httpSession);
        }
    }

    public static boolean containsBean(String name) {
        Lock rlock = rwLock.readLock();
        rlock.lock();
        try {
            return beanFactory.containsBean(name);
        } finally {
            rlock.unlock();
        }
    }
}