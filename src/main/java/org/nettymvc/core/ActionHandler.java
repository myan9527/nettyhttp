package org.nettymvc.core;

import java.lang.reflect.Method;

/**
 * Created by myan on 12/4/2017.
 * Intellij IDEA
 */

class ActionHandler {
    
    private final Class<?> router;
    
    private final Method method;
    
    ActionHandler(Class<?> router, Method method) {
        this.router = router;
        this.method = method;
    }
    
    Class<?> getRouter() {
        return router;
    }
    
    Method getMethod() {
        return method;
    }
}
