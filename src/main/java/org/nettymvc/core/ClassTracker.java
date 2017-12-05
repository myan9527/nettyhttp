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

import io.netty.util.internal.StringUtil;
import org.nettymvc.exception.ActionExecuteException;
import org.nettymvc.exception.InitializeException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by myan on 12/5/2017.
 * Intellij IDEA
 */
final class ClassTracker { // util for loading classes
    
    private static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
    
    /*load certain class*/
    private static Class<?> loadClass(String className, boolean initialize) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className, initialize, getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new InitializeException(e);
        }
        return clazz;
    }
    
    /*load classes under certain package*/
    static Set<Class<?>> loadClasses(String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        
        try {
            Enumeration<URL> urls = getClassLoader().getResources(packageName.replace(".", "/"));
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url != null) {
                    String protocol = url.getProtocol();
                    if (protocol.equals("file")) {
                        String packagePath = url.getPath().replaceAll("%20", " ");
                        addClass(classes, packageName, packagePath);
                    } else if (protocol.equals("jar")) {
                        JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                        if (jarConnection != null) {
                            JarFile jarFile = jarConnection.getJarFile();
                            if (jarFile != null) {
                                Enumeration<JarEntry> jarEntries = jarFile.entries();
                                while (jarEntries.hasMoreElements()) {
                                    JarEntry entry = jarEntries.nextElement();
                                    String entryName = entry.getName();
                                    if (entryName.endsWith(".class")) {
                                        String className = entryName.substring(0, entryName.lastIndexOf("."))
                                                .replaceAll("/", ".");
                                        addClass(classes, className);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new InitializeException(e);
        }
        return classes;
    }
    
    /*add single class*/
    private static void addClass(Set<Class<?>> classes, String className) {
        Class<?> clazz = loadClass(className, false);
        classes.add(clazz);
    }
    
    /*add package classes*/
    private static void addClass(Set<Class<?>> classes, String packageName, String packagePath) {
        File[] files = new File(packagePath).listFiles((f) -> (f.isFile() && f.getName().endsWith(".class"))
                || f.isDirectory());
        
        for (File file : files != null ? files : new File[0]) {
            String fileName = file.getName();
            if (file.isFile()) {
                String className = fileName.substring(0, fileName.lastIndexOf("."));
                if (!StringUtil.isNullOrEmpty(packageName)) {
                    className = packageName + "." + className;
                }
                addClass(classes, className);
            } else {
                String subPackagePath = fileName;
                if (!StringUtil.isNullOrEmpty(packagePath))
                    subPackagePath = packagePath + "/" + subPackagePath;
                String subPackageName = fileName;
                if (!StringUtil.isNullOrEmpty(packageName))
                    subPackageName = packageName + "." + subPackageName;
                addClass(classes, subPackageName, subPackagePath);
            }
        }
    }
    
    /*do invoke method staff*/
    static Object invokeMethod(Object target, Method method, Object... params) {
        Object result;
        try {
            method.setAccessible(true);
            if (method.getParameterCount() == 0 || params.length == 0)
                result = method.invoke(target);
            else
                result = method.invoke(target, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ActionExecuteException(e);
        }
        return result;
    }
}
