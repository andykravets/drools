/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.reteoo;

import org.drools.core.common.*;
import org.drools.core.rule.TypeDeclaration;
import org.drools.core.spi.FactHandleFactory;
import org.kie.api.runtime.rule.EntryPoint;

import java.io.Serializable;

public class ReteooFactHandleFactory extends AbstractFactHandleFactory implements Serializable {

    private static final long serialVersionUID = 510l;

    public ReteooFactHandleFactory() {
        super();
    }

    public ReteooFactHandleFactory(int id,
                                   long counter) {
        super(id,
                counter);
    }

    /* (non-Javadoc)
     * @see org.kie.reteoo.FactHandleFactory#newFactHandle(long)
     */
    public InternalFactHandle newFactHandle(final int id,
                                            final Object object,
                                            final long recency,
                                            final ObjectTypeConf conf,
                                            final InternalWorkingMemory workingMemory,
                                            final EntryPoint wmEntryPoint) {
        if (conf != null && conf.isEvent()) {
            TypeDeclaration type = conf.getTypeDeclaration();
            long timestamp;
            if (type.getTimestampExtractor() != null) {
                timestamp = type.getTimestampExtractor().getLongValue(workingMemory,
                        object);
            } else {
                timestamp = workingMemory.getTimerService().getCurrentTime();
            }
            long duration = 0;
            if (type.getDurationExtractor() != null) {
                duration = type.getDurationExtractor().getLongValue(workingMemory,
                        object);
            }
            if (isANamedEntryPoint(wmEntryPoint)) {
                return new ExternalEventFactHandle(id,
                        object,
                        recency,
                        timestamp,
                        duration,
                        wmEntryPoint,
                        isTraitOrTraitable(conf));
            } else {
                return new EventFactHandle(id,
                        object,
                        recency,
                        timestamp,
                        duration,
                        wmEntryPoint,
                        isTraitOrTraitable(conf));
            }
        } else {
            if (isANamedEntryPoint(wmEntryPoint)) {
                return new ExternalFactHandle(id, object, recency, wmEntryPoint);
            }
            return new DefaultFactHandle(id,
                    object,
                    recency,
                    wmEntryPoint,
                    isTraitOrTraitable(conf));
        }
    }

    private boolean isTraitOrTraitable(ObjectTypeConf conf) {
        return conf != null && conf.isTrait();
    }

    private boolean isANamedEntryPoint(EntryPoint wmEntryPoint) {
        return wmEntryPoint instanceof NamedEntryPoint;
    }

    /* (non-Javadoc)
     * @see org.kie.reteoo.FactHandleFactory#newInstance()
     */
    public FactHandleFactory newInstance() {
        return new ReteooFactHandleFactory();
    }

    public FactHandleFactory newInstance(int id,
                                         long counter) {
        return new ReteooFactHandleFactory(id,
                counter);
    }

    public Class getFactHandleType() {
        return DefaultFactHandle.class;
    }
}
