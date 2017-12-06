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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by myan on 12/5/2017.
 * Intellij IDEA
 */
public class RequestParam {
    
    private List<Param> params;
    
    public void add(Param param) {
        if (this.params == null || this.params.isEmpty())
            params = new LinkedList<>();
        this.params.add(param);
    }
    
    private Map<String, Object> getFieldMap() {
        Map<String, Object> fieldMap = new HashMap<>();
        if (this.params != null && !this.params.isEmpty()) {
            for (Param param : this.params) {
                String fieldName = param.getFieldName();
                Object fieldValue = param.getFieldValue();
                if (fieldMap.containsKey(fieldName))
                    fieldValue = fieldMap.get(fieldName) + "," + fieldValue;
                fieldMap.put(fieldName, fieldValue);
            }
        }
        return fieldMap;
    }
    
    public String getString(String fieldName) {
        return CastUtil.castString(getFieldMap().get(fieldName));
    }
    
    public boolean getBoolean(String fieldName) {
        return CastUtil.castBoolean(getFieldMap().get(fieldName));
    }
    
    public double getDouble(String fieldName) {
        return CastUtil.castDouble(getFieldMap().get(fieldName));
    }
    
    public int getInt(String fieldName) {
        return CastUtil.castInt(getFieldMap().get(fieldName));
    }
    
    public long getLong(String fieldName) {
        return CastUtil.castLong(getFieldMap().get(fieldName));
    }
}
