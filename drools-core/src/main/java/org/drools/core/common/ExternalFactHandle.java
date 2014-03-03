package org.drools.core.common;

import org.kie.api.runtime.rule.EntryPoint;

/**
 * User: andrey.kravets
 * Date: 2/14/14 3:55 PM
 */
public class ExternalFactHandle extends DefaultFactHandle {
    private InfinispanBasedTwoLevelCache<Integer, Object> cache;

    public ExternalFactHandle(final int id,
                              final Object object,
                              final long recency,
                              final EntryPoint wmEntryPoint) {

        super(id, object, recency, wmEntryPoint);
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
