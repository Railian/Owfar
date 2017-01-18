package com.owfar.android;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DelegatesSet<DelegateType> {

    //region constants
    public static final String DEFAULT_DELEGATE_TAG = "DEFAULT_DELEGATE_TAG";
    private static final String TAG_DETAILS_KEY = "/";
    public static final String ALL = null;
    //endregion

    //region fields
    private Class<DelegateType> delegateClass;
    private Map<String, Set<DelegateType>> delegates;
    private FiltersPresets filtersPresets;
    private DelegateType delegatesNotifier;
    //endregion

    //region Constructors
    public DelegatesSet(Class<DelegateType> delegateClass) {
        this.delegateClass = delegateClass;
        delegates = new TreeMap<>();
        filtersPresets = new FiltersPresets();
        delegatesNotifier = createDelegatesNotifier();
    }
    //endregion

    //region DataSource Methods
    public void addDelegate(String tag, DelegateType delegate) {
        if (delegate == null) return;
        tag = removeDetails(tag);
        Set<DelegateType> taggedDelegate = delegates.get(tag);
        if (taggedDelegate == null) {
            taggedDelegate = new HashSet<>();
            delegates.put(tag, taggedDelegate);
        }
        taggedDelegate.add(delegate);
    }

    public void removeDelegate(DelegateType delegate) {
        for (String tag : delegates.keySet()) {
            Set<DelegateType> taggedDelegates = delegates.get(tag);
            if (taggedDelegates != null)
                if (taggedDelegates.remove(delegate) && taggedDelegates.size() == 0)
                    delegates.put(tag, null);
        }
    }

    public void removeAllDelegates(String tag) {
        tag = removeDetails(tag);
        Set<DelegateType> taggedDelegates = delegates.get(tag);
        if (taggedDelegates != null)
            taggedDelegates.clear();
        delegates.remove(tag);
    }

    public void removeAllDelegates() {
        for (Set<DelegateType> taggedDelegates : delegates.values())
            if (taggedDelegates != null)
                taggedDelegates.clear();
        delegates.clear();
    }
    //endregion

    //region Tag Details
    public static String appendDetailsToTag(String tag, String details) {
        if (TextUtils.isEmpty(tag)) tag = DEFAULT_DELEGATE_TAG;
        if (TextUtils.isEmpty(details)) return tag;
        return String.format("%s%s%s", tag, TAG_DETAILS_KEY, details);
    }

    public static String removeDetails(String tag) {
        if (TextUtils.isEmpty(tag)) return DEFAULT_DELEGATE_TAG;
        int detailsKeyIndex = tag.indexOf(TAG_DETAILS_KEY);
        return detailsKeyIndex == -1 ? tag : tag.substring(0, detailsKeyIndex);
    }

    public static String getDetails(String tag) {
        if (TextUtils.isEmpty(tag)) return null;
        int detailsKeyIndex = tag.indexOf(TAG_DETAILS_KEY);
        return detailsKeyIndex == -1 ? null : tag.substring(detailsKeyIndex + 1, tag.length());
    }
    //endregion

    //region Delegates Notifier
    public DelegateType notify(@Nullable String... filters) {
        filtersPresets.setFilters(filters);
        return delegatesNotifier;
    }

    private <DelegateType> DelegateType createDelegatesNotifier() {
        return (DelegateType) Proxy.newProxyInstance(delegateClass.getClassLoader(),
                new Class<?>[]{delegateClass}, new DelegateInvocationHandler());
    }
    //endregion

    //region Class FiltersPresets
    private class FiltersPresets {

        private Set<String> filters;

        public Set<String> getFilters() {
            return filters;
        }

        public void setFilters(String... filters) {
            resetFilters();
            if (filters != null)
                for (String filter : filters)
                    addFilter(filter);
        }

        private void addFilter(String filter) {
            if (filters == null) filters = new HashSet<>();
            filters.add(removeDetails(filter));
        }

        public void resetFilters() {
            if (filters != null) {
                filters.clear();
                filters = null;
            }
        }
    }
    //endregion

    //region Class DelegateInvocationHandler
    private class DelegateInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == delegateClass) {
                Set<String> filters = filtersPresets.getFilters();
                if (filters == null) filters = delegates.keySet();
                for (String tag : filters) {
                    Set<DelegateType> taggedDelegates = delegates.get(tag);
                    if (taggedDelegates != null)
                        for (DelegateType delegate : taggedDelegates)
                            if (delegate != null)
                                delegate.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(delegate, args);
                }
                filtersPresets.resetFilters();
            } else
                return method.invoke(this, args);
            return null;
        }
    }

    //endregion
}