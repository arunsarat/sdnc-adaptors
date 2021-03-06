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

package org.openecomp.sdnc.rm.dao.jdbc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openecomp.sdnc.rm.dao.ResourceDao;
import org.openecomp.sdnc.rm.data.LabelAllocationItem;
import org.openecomp.sdnc.rm.data.LabelResource;
import org.openecomp.sdnc.rm.data.LimitAllocationItem;
import org.openecomp.sdnc.rm.data.LimitResource;
import org.openecomp.sdnc.rm.data.RangeAllocationItem;
import org.openecomp.sdnc.rm.data.RangeResource;
import org.openecomp.sdnc.rm.data.ResourceKey;
import org.openecomp.sdnc.rm.data.ResourceType;
import org.openecomp.sdnc.util.str.StrUtil;

public class ResourceDaoImpl implements ResourceDao {

	private ResourceJdbcDao resourceJdbcDao;
	private ResourceLoadJdbcDao resourceLoadJdbcDao;
	private AllocationItemJdbcDao allocationItemJdbcDao;

	@Override
	public org.openecomp.sdnc.rm.data.Resource getResource(String assetId, String resourceName) {
		Resource rEntity = resourceJdbcDao.getResource(assetId, resourceName);
		org.openecomp.sdnc.rm.data.Resource r = createResource(rEntity);

		if (r != null) {
			List<AllocationItem> aiEntityList = allocationItemJdbcDao.getAllocationItems(rEntity.id);
			r.allocationItems = new ArrayList<org.openecomp.sdnc.rm.data.AllocationItem>();
			for (AllocationItem aiEntity : aiEntityList) {
				org.openecomp.sdnc.rm.data.AllocationItem ai = createAllocationItem(r, aiEntity);
				r.allocationItems.add(ai);
			}

			List<ResourceLoad> rlEntityList = resourceLoadJdbcDao.getResourceLoads(rEntity.id);
			r.resourceLoadList = new ArrayList<org.openecomp.sdnc.rm.data.ResourceLoad>();
			for (ResourceLoad rlEntity : rlEntityList) {
				org.openecomp.sdnc.rm.data.ResourceLoad rl = createResourceLoad(r, rlEntity);
				r.resourceLoadList.add(rl);
			}
		}

		return r;
	}

	@Override
	public void saveResource(org.openecomp.sdnc.rm.data.Resource resource) {
		if (resource == null)
			return;

		org.openecomp.sdnc.rm.dao.jdbc.Resource resourceEntity =
		        resourceJdbcDao.getResource(resource.resourceKey.assetId, resource.resourceKey.resourceName);
		if (resourceEntity == null) {
			resourceEntity = createResourceEntity(resource);
			resourceJdbcDao.add(resourceEntity);
			if (resource.allocationItems != null)
				for (org.openecomp.sdnc.rm.data.AllocationItem ai : resource.allocationItems) {
					AllocationItem aiEntity = createAllocationItemEntity(resourceEntity.id, ai);
					allocationItemJdbcDao.add(aiEntity);
				}
			if (resource.resourceLoadList != null)
				for (org.openecomp.sdnc.rm.data.ResourceLoad rl : resource.resourceLoadList) {
					ResourceLoad rlEntity = createResourceLoadEntity(resourceEntity.id, rl);
					resourceLoadJdbcDao.add(rlEntity);
				}
		} else {
			updateResourceEntity(resourceEntity, resource);
			resourceJdbcDao.update(resourceEntity);

			List<AllocationItem> oldAiEntityList = allocationItemJdbcDao.getAllocationItems(resourceEntity.id);
			if (resource.allocationItems != null)
				for (org.openecomp.sdnc.rm.data.AllocationItem newai : resource.allocationItems) {
					AllocationItem foundAiEntity = null;
					for (AllocationItem oldAiEntity : oldAiEntityList)
						if (oldAiEntity.resourceSetId.equals(newai.resourceSetId)) {
							foundAiEntity = oldAiEntity;
							break;
						}
					if (foundAiEntity != null) {
						updateAllocationItemEntity(foundAiEntity, newai);
						allocationItemJdbcDao.update(foundAiEntity);
					} else {
						AllocationItem newAiEntity = createAllocationItemEntity(resourceEntity.id, newai);
						allocationItemJdbcDao.add(newAiEntity);
					}
				}
			for (AllocationItem oldAiEntity : oldAiEntityList) {
				boolean found = false;
				if (resource.allocationItems != null)
					for (org.openecomp.sdnc.rm.data.AllocationItem newai : resource.allocationItems)
						if (oldAiEntity.resourceSetId.equals(newai.resourceSetId)) {
							found = true;
							break;
						}
				if (!found)
					allocationItemJdbcDao.delete(oldAiEntity.id);
			}

			List<ResourceLoad> oldRlEntityList = resourceLoadJdbcDao.getResourceLoads(resourceEntity.id);
			if (resource.resourceLoadList != null)
				for (org.openecomp.sdnc.rm.data.ResourceLoad newrl : resource.resourceLoadList) {
					ResourceLoad foundRlEntity = null;
					for (ResourceLoad oldRlEntity : oldRlEntityList)
						if (oldRlEntity.applicationId.equals(newrl.applicationId)) {
							foundRlEntity = oldRlEntity;
							break;
						}
					if (foundRlEntity != null) {
						updateResourceLoadEntity(foundRlEntity, newrl);
						resourceLoadJdbcDao.update(foundRlEntity);
					} else {
						ResourceLoad newRlEntity = createResourceLoadEntity(resourceEntity.id, newrl);
						resourceLoadJdbcDao.add(newRlEntity);
					}
				}
			for (ResourceLoad oldRlEntity : oldRlEntityList) {
				boolean found = false;
				if (resource.resourceLoadList != null)
					for (org.openecomp.sdnc.rm.data.ResourceLoad newrl : resource.resourceLoadList)
						if (oldRlEntity.applicationId.equals(newrl.applicationId)) {
							found = true;
							break;
						}
				if (!found)
					resourceLoadJdbcDao.delete(oldRlEntity.id);
			}
		}
	}

	@Override
	public void deleteResource(String assetId, String resourceName) {
		org.openecomp.sdnc.rm.dao.jdbc.Resource resourceEntity = resourceJdbcDao.getResource(assetId, resourceName);
		if (resourceEntity != null)
			resourceJdbcDao.delete(resourceEntity.id);
	}

	@Override
	public List<org.openecomp.sdnc.rm.data.Resource> getResourceSet(String resourceSetId) {
		List<Resource> rEntityList = resourceJdbcDao.getResourceSet(resourceSetId);
		List<org.openecomp.sdnc.rm.data.Resource> rlist = new ArrayList<org.openecomp.sdnc.rm.data.Resource>();
		for (Resource rEntity : rEntityList) {
			org.openecomp.sdnc.rm.data.Resource r = createResource(rEntity);
			rlist.add(r);

			List<AllocationItem> aiEntityList = allocationItemJdbcDao.getAllocationItems(rEntity.id);
			r.allocationItems = new ArrayList<org.openecomp.sdnc.rm.data.AllocationItem>();
			for (AllocationItem aiEntity : aiEntityList) {
				org.openecomp.sdnc.rm.data.AllocationItem ai = createAllocationItem(r, aiEntity);
				r.allocationItems.add(ai);
			}

			List<ResourceLoad> rlEntityList = resourceLoadJdbcDao.getResourceLoads(rEntity.id);
			r.resourceLoadList = new ArrayList<org.openecomp.sdnc.rm.data.ResourceLoad>();
			for (ResourceLoad rlEntity : rlEntityList) {
				org.openecomp.sdnc.rm.data.ResourceLoad rl = createResourceLoad(r, rlEntity);
				r.resourceLoadList.add(rl);
			}
		}
		return rlist;
	}

	@Override
	public List<org.openecomp.sdnc.rm.data.Resource> getResourceUnion(String resourceUnionId) {
		List<Resource> rEntityList = resourceJdbcDao.getResourceUnion(resourceUnionId);
		List<org.openecomp.sdnc.rm.data.Resource> rlist = new ArrayList<org.openecomp.sdnc.rm.data.Resource>();
		for (Resource rEntity : rEntityList) {
			org.openecomp.sdnc.rm.data.Resource r = createResource(rEntity);
			rlist.add(r);

			List<AllocationItem> aiEntityList = allocationItemJdbcDao.getAllocationItems(rEntity.id);
			r.allocationItems = new ArrayList<org.openecomp.sdnc.rm.data.AllocationItem>();
			for (AllocationItem aiEntity : aiEntityList) {
				org.openecomp.sdnc.rm.data.AllocationItem ai = createAllocationItem(r, aiEntity);
				r.allocationItems.add(ai);
			}

			List<ResourceLoad> rlEntityList = resourceLoadJdbcDao.getResourceLoads(rEntity.id);
			r.resourceLoadList = new ArrayList<org.openecomp.sdnc.rm.data.ResourceLoad>();
			for (ResourceLoad rlEntity : rlEntityList) {
				org.openecomp.sdnc.rm.data.ResourceLoad rl = createResourceLoad(r, rlEntity);
				r.resourceLoadList.add(rl);
			}
		}
		return rlist;
	}

	private Resource createResourceEntity(org.openecomp.sdnc.rm.data.Resource resource) {
		Resource resourceEntity = new Resource();
		resourceEntity.assetId = resource.resourceKey.assetId;
		resourceEntity.name = resource.resourceKey.resourceName;
		resourceEntity.type = resource.resourceType.toString();
		if (resource.resourceType == ResourceType.Limit)
			resourceEntity.ltUsed = ((LimitResource) resource).used;
		else if (resource.resourceType == ResourceType.Label) {
			resourceEntity.llLabel = ((LabelResource) resource).label;
			resourceEntity.llReferenceCount = ((LabelResource) resource).referenceCount;
		} else if (resource.resourceType == ResourceType.Range)
			resourceEntity.rrUsed = StrUtil.listInt(((RangeResource) resource).used);

		return resourceEntity;
	}

	private ResourceLoad createResourceLoadEntity(long resourceId, org.openecomp.sdnc.rm.data.ResourceLoad rl) {
		ResourceLoad rlEntity = new ResourceLoad();
		rlEntity.resourceId = resourceId;
		rlEntity.applicationId = rl.applicationId;
		rlEntity.loadTime = rl.resourceLoadTime;
		rlEntity.expirationTime = rl.resourceExpirationTime;
		return rlEntity;
	}

	private void updateResourceLoadEntity(ResourceLoad rlEntity, org.openecomp.sdnc.rm.data.ResourceLoad rl) {
		rlEntity.loadTime = rl.resourceLoadTime;
		rlEntity.expirationTime = rl.resourceExpirationTime;
	}

	private AllocationItem createAllocationItemEntity(long resourceId, org.openecomp.sdnc.rm.data.AllocationItem ai) {
		AllocationItem aiEntity = new AllocationItem();
		aiEntity.resourceId = resourceId;
		aiEntity.resourceSetId = ai.resourceSetId;
		aiEntity.resourceUnionId = ai.resourceUnionId;
		aiEntity.resourceShareGroupList = StrUtil.listStr(ai.resourceShareGroupList);
		aiEntity.applicationId = ai.applicationId;
		aiEntity.allocationTime = ai.allocationTime;
		if (ai.resourceType == ResourceType.Limit)
			aiEntity.ltUsed = ((LimitAllocationItem) ai).used;
		else if (ai.resourceType == ResourceType.Label)
			aiEntity.llLabel = ((LabelAllocationItem) ai).label;
		else if (ai.resourceType == ResourceType.Range)
			aiEntity.rrUsed = StrUtil.listInt(((RangeAllocationItem) ai).used);
		return aiEntity;
	}

	private void updateAllocationItemEntity(AllocationItem aiEntity, org.openecomp.sdnc.rm.data.AllocationItem ai) {
		aiEntity.resourceShareGroupList = StrUtil.listStr(ai.resourceShareGroupList);
		aiEntity.allocationTime = ai.allocationTime;
		if (ai.resourceType == ResourceType.Limit)
			aiEntity.ltUsed = ((LimitAllocationItem) ai).used;
		else if (ai.resourceType == ResourceType.Label)
			aiEntity.llLabel = ((LabelAllocationItem) ai).label;
		else if (ai.resourceType == ResourceType.Range)
			aiEntity.rrUsed = StrUtil.listInt(((RangeAllocationItem) ai).used);
	}

	private void updateResourceEntity(Resource resourceEntity, org.openecomp.sdnc.rm.data.Resource resource) {
		if (resource.resourceType == ResourceType.Limit)
			resourceEntity.ltUsed = ((LimitResource) resource).used;
		else if (resource.resourceType == ResourceType.Label) {
			resourceEntity.llLabel = ((LabelResource) resource).label;
			resourceEntity.llReferenceCount = ((LabelResource) resource).referenceCount;
		} else if (resource.resourceType == ResourceType.Range)
			resourceEntity.rrUsed = StrUtil.listInt(((RangeResource) resource).used);
	}

	private org.openecomp.sdnc.rm.data.Resource createResource(Resource resourceEntity) {
		if (resourceEntity == null)
			return null;

		org.openecomp.sdnc.rm.data.Resource r = null;
		ResourceType type = ResourceType.valueOf(resourceEntity.type);
		if (type == ResourceType.Limit) {
			LimitResource l = new LimitResource();
			l.used = resourceEntity.ltUsed;
			r = l;
		} else if (type == ResourceType.Label) {
			LabelResource l = new LabelResource();
			l.label = resourceEntity.llLabel;
			l.referenceCount = resourceEntity.llReferenceCount;
			r = l;
		} else if (type == ResourceType.Range) {
			RangeResource rr = new RangeResource();
			rr.used =
			        StrUtil.listInt(resourceEntity.rrUsed, "Invalid data found in DB in for Resource Id: " +
			                resourceEntity.id + ": RESOURCE.RR_USED: " + resourceEntity.rrUsed);
			r = rr;
		}

		r.resourceType = type;
		r.resourceKey = new ResourceKey();
		r.resourceKey.assetId = resourceEntity.assetId;
		r.resourceKey.resourceName = resourceEntity.name;

		return r;
	}

	private org.openecomp.sdnc.rm.data.AllocationItem createAllocationItem(
	        org.openecomp.sdnc.rm.data.Resource r,
	        AllocationItem aiEntity) {
		if (r == null || aiEntity == null)
			return null;

		org.openecomp.sdnc.rm.data.AllocationItem ai = null;
		if (r.resourceType == ResourceType.Limit) {
			LimitAllocationItem lai = new LimitAllocationItem();
			lai.used = aiEntity.ltUsed;
			ai = lai;
		} else if (r.resourceType == ResourceType.Label) {
			LabelAllocationItem lai = new LabelAllocationItem();
			lai.label = aiEntity.llLabel;
			ai = lai;
		} else if (r.resourceType == ResourceType.Range) {
			RangeAllocationItem rai = new RangeAllocationItem();
			rai.used =
			        StrUtil.listInt(aiEntity.rrUsed, "Invalid data found in DB in for Allocation Item Id: " +
			                aiEntity.id + ": ALLOCATION_ITEM.RR_USED: " + aiEntity.rrUsed);
			ai = rai;
		}

		ai.resourceType = r.resourceType;
		ai.resourceKey = r.resourceKey;
		ai.resourceSetId = aiEntity.resourceSetId;
		ai.resourceUnionId = aiEntity.resourceUnionId;
		if (aiEntity.resourceShareGroupList != null)
			ai.resourceShareGroupList = new HashSet<String>(StrUtil.listStr(aiEntity.resourceShareGroupList));
		ai.applicationId = aiEntity.applicationId;
		ai.allocationTime = aiEntity.allocationTime;

		return ai;
	}

	private org.openecomp.sdnc.rm.data.ResourceLoad createResourceLoad(
	        org.openecomp.sdnc.rm.data.Resource r,
	        ResourceLoad rlEntity) {
		if (rlEntity == null)
			return null;

		org.openecomp.sdnc.rm.data.ResourceLoad rl = new org.openecomp.sdnc.rm.data.ResourceLoad();
		rl.resourceKey = r.resourceKey;
		rl.applicationId = rlEntity.applicationId;
		rl.resourceLoadTime = rlEntity.loadTime;
		rl.resourceExpirationTime = rlEntity.expirationTime;

		return rl;
	}

	public void setResourceJdbcDao(ResourceJdbcDao resourceJdbcDao) {
		this.resourceJdbcDao = resourceJdbcDao;
	}

	public void setResourceLoadJdbcDao(ResourceLoadJdbcDao resourceLoadJdbcDao) {
		this.resourceLoadJdbcDao = resourceLoadJdbcDao;
	}

	public void setAllocationItemJdbcDao(AllocationItemJdbcDao allocationItemJdbcDao) {
		this.allocationItemJdbcDao = allocationItemJdbcDao;
	}
}
