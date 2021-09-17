
package com.peppa.common.feign;


import org.springframework.cloud.openfeign.FeignContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class PeppaPrometheusFeignContext
        extends FeignContext {
    private final FeignContext delegate;
    private final AbstractPeppaPrometheusFeignObjectWrapper peppaPrometheusFeignObjectWrapper;

    public PeppaPrometheusFeignContext(AbstractPeppaPrometheusFeignObjectWrapper wrapper, FeignContext delegate) {
        this.peppaPrometheusFeignObjectWrapper = wrapper;
        this.delegate = delegate;
    }

    public <T> T getInstance(String name, Class<T> type) {
        T object = (T) this.delegate.getInstance(name, type);
        return (T) this.peppaPrometheusFeignObjectWrapper.wrap(object);
    }

    public <T> Map<String, T> getInstances(String name, Class<T> type) {
        Map<String, T> instances = this.delegate.getInstances(name, type);
        if (instances == null) {
            return null;
        }
        Map<String, T> convertedInstances = new HashMap<>();
        Iterator<Map.Entry<String, T>> var5 = instances.entrySet().iterator();

        while (var5.hasNext()) {
            Map.Entry<String, T> entry = var5.next();
            convertedInstances.put(entry.getKey(), (T) this.peppaPrometheusFeignObjectWrapper.wrap(entry.getValue()));
        }

        return convertedInstances;
    }
}

