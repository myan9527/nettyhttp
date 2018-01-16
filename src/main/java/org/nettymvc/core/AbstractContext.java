/*
  MIT License
  <p>
  Copyright (c) 2017 Michael Yan
  <p>
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
  <p>
  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.
  <p>
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
 */
package org.nettymvc.core;

import org.aeonbits.owner.ConfigFactory;
import org.nettymvc.annotation.Action;
import org.nettymvc.annotation.RequestMethod;
import org.nettymvc.annotation.Router;
import org.nettymvc.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chhuo on 2017/12/5.
 * Intellij IDEA
 */
class AbstractContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContext.class);
    
    private static final ServerConfig CONFIG = ConfigFactory.create(ServerConfig.class);
    
    private final Object lock = new Object();
    
    private volatile boolean initialized;
    
    private final String basePackage;
    
    // holds all routers.
    private final Set<Class<?>> routers = new HashSet<>();
    // holds all injected singletons.
    private final static Map<Class<?>, Object> SINGLETONS = new HashMap<>();
    
    final Map<RoutingRequest, ActionHandler> actionMap = new ConcurrentHashMap<>();
    
    AbstractContext() {
        this.basePackage = CONFIG.basePackage();
        init();
    }
    
    void init() {
        if (!initialized) {
            // collect all annotation classes for only single thread.
            Set<Class<?>> classes = ClassTracker.loadClasses(basePackage);
            synchronized (this.lock) {
                for (Class<?> clazz : classes) {
                    if (clazz.isAnnotationPresent(Router.class)) {
                        routers.add(clazz);
                    }
                    SINGLETONS.put(clazz, ClassTracker.newInstance(clazz));
                }
                // let's build action map for processing requests
                if (!routers.isEmpty()) {
                    buildActionMap();
                }
                initialized = true;
            }
        }
    }
    
    private void buildActionMap() {
        for (Class<?> router : routers) {
            Method[] methods = router.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Action.class)) {
                    Action action = method.getAnnotation(Action.class);
                    String path = action.value();
                    RequestMethod[] requestMethods = action.method();
                    RoutingRequest routingRequest = new RoutingRequest(path, requestMethods);
                    ActionHandler handler = new ActionHandler(router, method);
                    LOGGER.info(String.format("Mapped url \"%s\" for \"%s-%s()\"", path, router.getName(),
                            method.getName()));
                    this.actionMap.put(routingRequest, handler);
                }
            }
            
        }
    }
    
    boolean isInitialized() {
        return initialized;
    }
    
    Map<Class<?>, Object> getSingletons() {
        return SINGLETONS;
    }
    
    public static ServerConfig getConfig() {
        return CONFIG;
    }
}
