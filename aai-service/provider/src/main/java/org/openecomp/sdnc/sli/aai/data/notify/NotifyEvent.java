/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 							reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.sli.aai.data.notify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "event-id",
    "event-trigger",
    "key-data",
    "node-type",
    "selflink"
})
public class NotifyEvent {

    @JsonProperty("event-id")
    private String eventId;
    @JsonProperty("event-trigger")
    private String eventTrigger;
    @JsonProperty("key-data")
    private List<KeyDatum> keyData = new ArrayList<KeyDatum>();
    @JsonProperty("node-type")
    private String nodeType;
    @JsonProperty("selflink")
    private String selflink;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The eventId
     */
    @JsonProperty("event-id")
    public String getEventId() {
        return eventId;
    }

    /**
     * 
     * @param eventId
     *     The event-id
     */
    @JsonProperty("event-id")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * 
     * @return
     *     The eventTrigger
     */
    @JsonProperty("event-trigger")
    public String getEventTrigger() {
        return eventTrigger;
    }

    /**
     * 
     * @param eventTrigger
     *     The event-trigger
     */
    @JsonProperty("event-trigger")
    public void setEventTrigger(String eventTrigger) {
        this.eventTrigger = eventTrigger;
    }

    /**
     * 
     * @return
     *     The keyData
     */
    @JsonProperty("key-data")
    public List<KeyDatum> getKeyData() {
        return keyData;
    }

    /**
     * 
     * @param keyData
     *     The key-data
     */
    @JsonProperty("key-data")
    public void setKeyData(List<KeyDatum> keyData) {
        this.keyData = keyData;
    }

    /**
     * 
     * @return
     *     The nodeType
     */
    @JsonProperty("node-type")
    public String getNodeType() {
        return nodeType;
    }

    /**
     * 
     * @param nodeType
     *     The node-type
     */
    @JsonProperty("node-type")
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * 
     * @return
     *     The selflink
     */
    @JsonProperty("selflink")
    public String getSelflink() {
        return selflink;
    }

    /**
     * 
     * @param selflink
     *     The selflink
     */
    @JsonProperty("selflink")
    public void setSelflink(String selflink) {
        this.selflink = selflink;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}