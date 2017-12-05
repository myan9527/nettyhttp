package org.nettymvc.core;

import java.lang.reflect.Method;

/**
 * Created by myan on 12/4/2017.
 * Intellij IDEA
 */

public class ActionHandler {
    
    private final Class<?> router;
    
    private final Method method;
    
    public ActionHandler(Class<?> router, Method method) {
        this.router = router;
        this.method = method;
    }
    
    public Class<?> getRouter() {
        return router;
    }
    
    public Method getMethod() {
        return method;
    }
}
