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

package org.openecomp.sdnc.rm.comp;

import java.util.List;

import org.openecomp.sdnc.rm.data.AllocationOutcome;
import org.openecomp.sdnc.rm.data.AllocationRequest;
import org.openecomp.sdnc.rm.data.Resource;

public interface ResourceManager {

	Resource getResource(String resourceName, String assetId);

	List<Resource> getResourceUnion(String resourceUnionId);

	AllocationOutcome allocateResources(AllocationRequest allocationRequest);

	void releaseResourceSet(String resourceSetId);

	void releaseResourceUnion(String resourceUnionId);
}