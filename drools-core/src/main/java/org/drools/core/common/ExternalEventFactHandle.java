package org.drools.core.common;

import org.kie.api.runtime.rule.EntryPoint;

/**
 * User: andrey.kravets
 * Date: 2/17/14 11:24 AM
 */
public class ExternalEventFactHandle extends EventFactHandle {

    private InfinispanBasedTwoLevelCache<Integer, Object> cache;

    public ExternalEventFactHandle(int id, Object object, long recency, long timestamp, long duration, EntryPoint wmEntryPoint, boolean isTraitOrTraitable) {
        super(id, object, recency, timestamp, duration, wmEntryPoint, isTraitOrTraitable);
        this.object = null;

        try {
            cache = ((NamedEntryPoint) wmEntryPoint).getCache();

            if (cache != null) {
                cache.put(new Integer(id), object);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Object getObject() {
        Object obj = cache.get(getId());
        return obj;
    }
}
