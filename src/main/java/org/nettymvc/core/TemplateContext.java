/*
    MIT License
     
    Copyright (c) 2017 Michael Yan
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/
package org.nettymvc.core;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import org.nettymvc.exception.InitializeException;

import java.io.File;
import java.io.IOException;

/**
 * Created by myan on 12/12/2017.
 * Intellij IDEA
 */

public class TemplateContext extends AbstractContext {
    private final Configuration markerConfig;
    private static TemplateContext INSTANCE;
    private static final String CLASSPATH = "classpath:";
    
    private TemplateContext() {
        String templatePath = config.templatePath();
        this.markerConfig = new Configuration(Configuration.VERSION_2_3_0);
        try {
            if(templatePath.startsWith(CLASSPATH)) {
                // resolve path from classpath root.
                String[] paths = templatePath.split(CLASSPATH);
                if(paths.length == 2) {
                    // target/classes/templates
                    String path = TemplateContext.class.getResource("/").getFile().substring(1) + paths[1] + File.separator;
                    this.markerConfig.setTemplateLoader(new FileTemplateLoader(new File(path)));
                }
            } else {
                throw new IllegalStateException("The template path must start with classpath.");
            }
        } catch (Exception e) {
            throw new InitializeException(e);
        }
    }
    
    public static TemplateContext getTemplateContext() {
        if(INSTANCE == null) {
            INSTANCE = new TemplateContext();
        }
        return INSTANCE;
    }
    
    public Configuration getMarkerConfig() {
        return markerConfig;
    }
    
}
