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
package org.nettymvc.data;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by myan on 12/6/2017.
 * Intellij IDEA
 */
final class CastUtil {
    static String castString(Object property) {
        return castString(property, "");
    }
    
    private static String castString(Object property, String defaultValue) {
        return property != null ? String.valueOf(property) : defaultValue;
    }
    
    static double castDouble(Object property) {
        return castDouble(property, 0);
    }
    
    private static double castDouble(Object property, double defaultValue) {
        double value = defaultValue;
        if (property != null) {
            String string = castString(property);
            if (StringUtils.isNotEmpty(string)) {
                try {
                    value = Double.parseDouble(string);
                } catch (NumberFormatException e) {
                    value = defaultValue;
                }
            }
        }
        return value;
    }
    
    static long castLong(Object property) {
        return castLong(property, 0);
    }
    
    private static long castLong(Object property, long defaultValue) {
        long value = defaultValue;
        if (property != null) {
            String string = castString(property);
            if (StringUtils.isNotEmpty(string)) {
                try {
                    value = Long.parseLong(string);
                } catch (NumberFormatException e) {
                    value = defaultValue;
                }
            }
        }
        return value;
    }
    
    static int castInt(Object property) {
        return castInt(property, 0);
    }
    
    private static int castInt(Object property, int defaultValue) {
        int value = defaultValue;
        if (property != null) {
            String string = castString(property);
            if (StringUtils.isNotEmpty(string)) {
                try {
                    value = Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    value = defaultValue;
                }
            }
        }
        return value;
    }
    
    static boolean castBoolean(Object property) {
        return castBoolean(property, false);
    }
    
    private static boolean castBoolean(Object property, boolean defaultValue) {
        boolean value = defaultValue;
        if (property != null)
            value = Boolean.parseBoolean(castString(property));
        return value;
    }
}
