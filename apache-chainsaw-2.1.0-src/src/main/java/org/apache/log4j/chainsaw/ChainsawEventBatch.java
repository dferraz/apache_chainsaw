/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.chainsaw;

import org.apache.log4j.spi.LoggingEvent;

import java.util.*;


/**
 * A container class that contains a group of events split up
 * into branches based on Identifiers
 *
 * @author Paul Smith &lt;psmith@apache.org&gt;
 * @author Scott Deboy &lt;sdeboy@apache.org&gt;
 */
class ChainsawEventBatch {
    private Map<String, List<LoggingEvent>> identEventMap = new HashMap<>();

    ChainsawEventBatch() {
    }

    /**
     * @param ident
     * @param e
     */
    void addEvent(String ident, LoggingEvent e) {
        List<LoggingEvent> events = identEventMap.get(ident);

        if (events == null) {
            events = new ArrayList<>();
            identEventMap.put(ident, events);
        }

        events.add(e);
    }

    /**
     * Returns an iterator of Identifier strings that this payload contains.
     * <p>
     * The values returned from this iterator can be used to query the
     *
     * @return Iterator
     */
    Iterator<String> identifierIterator() {
        return identEventMap.keySet().iterator();
    }

    /**
     * Returns a Collection of LoggingEvent objects that
     * are bound to the identifier
     *
     * @param identifier
     * @return Collection of LoggingEvent instances
     */
    List<LoggingEvent> entrySet(String identifier) {
        return identEventMap.get(identifier);
    }
}
