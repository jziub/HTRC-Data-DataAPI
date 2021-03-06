/*
#
# Copyright 2013 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api
# File:  TestVolumeValidityChecker.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.validity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.TestParameterContainer;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.exception.PolicyViolationException;
import edu.indiana.d2i.htrc.access.exception.RepositoryException;
import edu.indiana.d2i.htrc.access.id.ItemCoordinatesImpl;
import edu.indiana.d2i.htrc.access.policy.MaxPagesPerVolumePolicyChecker;
import edu.indiana.d2i.htrc.access.policy.MaxTotalPagesPolicyChecker;
import edu.indiana.d2i.htrc.access.policy.MaxVolumesPolicyChecker;
import edu.indiana.d2i.htrc.access.policy.PolicyCheckerRegistryImpl;
import edu.indiana.d2i.htrc.access.read.HectorResource;
import edu.indiana.d2i.htrc.access.read.TestHectorResource;


/**
 * @author Yiming Sun
 *
 */
public class RequestValidityCheckerTest {
    
    public static final String NON_EXISTING_VOLUME_ID = "invalid.fake:/0001/volume1"; 
    
    private static ParameterContainer parameterContainer = null;
    private static HectorResource hectorResource = null;
    private static PolicyCheckerRegistryImpl policyCheckerRegistry = null;
    
    
    
    @BeforeClass
    public static void setup() throws Exception {
        setupWithDefaultLimits();
    }
    
    private static void setupWithDefaultLimits() throws Exception {
        setupWithLimits("3", "10", "5");
    }
    
    private static void setupWithLimits(String maxVolumes, String maxTotalPages, String maxPagesPerVolume) throws Exception {
        parameterContainer = new TestParameterContainer();
        parameterContainer.setParameter(HectorResource.PN_CASSANDRA_NODE_COUNT, "1");
        parameterContainer.setParameter(HectorResource.PN_CASSANDRA_NODE_NAME_ + "1", "127.0.0.1");
        parameterContainer.setParameter(HectorResource.PN_CASSANDRA_CLUSTER_NAME, "No such cluster");
        parameterContainer.setParameter(HectorResource.PN_CASSANDRA_KEYSPACE_NAME, "NoSuchKeyspace");
        parameterContainer.setParameter(HectorResource.PN_HECTOR_ACCESS_FAIL_INIT_DELAY, "200");
        parameterContainer.setParameter(HectorResource.PN_HECTOR_ACCESS_FAIL_MAX_DELAY, "1000");
        parameterContainer.setParameter(HectorResource.PN_HECTOR_ACCESS_MAX_ATTEMPTS, "4");

        parameterContainer.setParameter(MaxVolumesPolicyChecker.PN_MAX_VOLUMES_ALLOWED, maxVolumes);
        parameterContainer.setParameter(MaxTotalPagesPolicyChecker.PN_MAX_TOTAL_PAGES_ALLOWED, maxTotalPages);
        parameterContainer.setParameter(MaxPagesPerVolumePolicyChecker.PN_MAX_PAGES_PER_VOLUME_ALLOWED, maxPagesPerVolume);
        
        hectorResource = new TestHectorResource(parameterContainer);
        
        policyCheckerRegistry = PolicyCheckerRegistryImpl.getInstance();
        policyCheckerRegistry.registerPolicyChecker(MaxVolumesPolicyChecker.POLICY_NAME, new MaxVolumesPolicyChecker(parameterContainer));
        policyCheckerRegistry.registerPolicyChecker(MaxPagesPerVolumePolicyChecker.POLICY_NAME, new MaxPagesPerVolumePolicyChecker(parameterContainer));
        policyCheckerRegistry.registerPolicyChecker(MaxTotalPagesPolicyChecker.POLICY_NAME, new MaxTotalPagesPolicyChecker(parameterContainer));
    }
    

    // This case tests that a KeyNotFoundException should be raised when a non-existing volumeID is requested.
    @Test(expected = KeyNotFoundException.class)
    public void testInvalidVolumeID1() throws KeyNotFoundException, PolicyViolationException, RepositoryException {
        ItemCoordinatesImpl[] volumeIDs = new ItemCoordinatesImpl[] {new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[2]),
                                                           new ItemCoordinatesImpl(NON_EXISTING_VOLUME_ID),
                                                           new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[3])};
        List<ItemCoordinatesImpl> idList = Arrays.asList(volumeIDs);
        VolumeValidityChecker checker = new VolumeValidityChecker(hectorResource, parameterContainer, policyCheckerRegistry);
        checker.validateRequest(idList);
    }
    
    // This case tests that a KeyNotFoundException should be raised when a non-existing pageSequence of an existing volumeID is requested.
    @Test(expected = KeyNotFoundException.class)
    public void testInvalidPageID1()  throws KeyNotFoundException, PolicyViolationException, RepositoryException {
        ItemCoordinatesImpl pageID1 = new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[1]);
        pageID1.addPageSequence("00000004");
        pageID1.addPageSequence("00000002");
        pageID1.addPageSequence("00000001");
        

        ItemCoordinatesImpl pageID2 = new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[3]);
        pageID2.addPageSequence("00000003");
        pageID2.addPageSequence("00000001");
        pageID2.addPageSequence("00000004");
        pageID2.addPageSequence("00000005");
        
        List<ItemCoordinatesImpl> idList = new ArrayList<ItemCoordinatesImpl>(2);
        idList.add(pageID1);
        idList.add(pageID2);
        
        PageValidityChecker checker = new PageValidityChecker(hectorResource, parameterContainer, policyCheckerRegistry);
        checker.validateRequest(idList);

    }
    
    // This case tests that a KeyNotFoundException should be raised when a non-existing volumeID is requested
    @Test(expected = KeyNotFoundException.class)
    public void testInvalidPageID2() throws KeyNotFoundException, PolicyViolationException, RepositoryException {
        ItemCoordinatesImpl pageID1 = new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[1]);
        pageID1.addPageSequence("00000004");
        pageID1.addPageSequence("00000002");
        pageID1.addPageSequence("00000001");
        

        ItemCoordinatesImpl pageID2 = new ItemCoordinatesImpl(NON_EXISTING_VOLUME_ID);
        pageID2.addPageSequence("00000002");
        pageID2.addPageSequence("00000003");
        pageID2.addPageSequence("00000001");
        
        List<ItemCoordinatesImpl> idList = new ArrayList<ItemCoordinatesImpl>(2);
        idList.add(pageID1);
        idList.add(pageID2);
        
        PageValidityChecker checker = new PageValidityChecker(hectorResource, parameterContainer, policyCheckerRegistry);
        checker.validateRequest(idList);

    }
    
    // This case tests that a PolicyVioationException should be raised when number of volumeIDs requested exceeds max volume limit of 3
    // This case modifies the default limits setting so other limits won't trigger the exceptions
    @Test(expected = PolicyViolationException.class)
    public void testMaxVolumesViolation1() throws KeyNotFoundException, PolicyViolationException, RepositoryException, Exception {
        ItemCoordinatesImpl[] volumeIDs = new ItemCoordinatesImpl[] {new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[2]), 
                                                           new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[0]),
                                                           new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[3]),
                                                           new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[1])};
        try {
            List<ItemCoordinatesImpl> idList = Arrays.asList(volumeIDs);
    
            setupWithLimits("3", "50", "10");
            
            VolumeValidityChecker checker = new VolumeValidityChecker(hectorResource, parameterContainer, policyCheckerRegistry);
            checker.validateRequest(idList);
        } finally {
            setupWithDefaultLimits();
        }
        
    }
    
    // This case tests that a PolicyViolationException should be raised when number of pageIDs requested exceeds max volume limit of 3
    @Test(expected = PolicyViolationException.class)
    public void testMaxVolumesViolation2() throws KeyNotFoundException, PolicyViolationException, RepositoryException {
        ItemCoordinatesImpl pageID1 = new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[1]);
        pageID1.addPageSequence("00000001");
        

        ItemCoordinatesImpl pageID2 = new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[3]);
        pageID2.addPageSequence("00000001");


        ItemCoordinatesImpl pageID3 = new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[0]);
        pageID3.addPageSequence("00000001");

        ItemCoordinatesImpl pageID4 = new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[2]);
        pageID4.addPageSequence("00000001");
        
        List<ItemCoordinatesImpl> idList = new ArrayList<ItemCoordinatesImpl>(2);
        idList.add(pageID1);
        idList.add(pageID2);
        idList.add(pageID3);
        idList.add(pageID4);
        
        PageValidityChecker checker = new PageValidityChecker(hectorResource, parameterContainer, policyCheckerRegistry);
        checker.validateRequest(idList);
        
    }
    
    // This case tests that a PolicyViolationException should be raised when volumes requested have total page count exceeding max total page limit of 10
    @Test(expected = PolicyViolationException.class)
    public void testMaxTotalPagesViolation1() throws KeyNotFoundException, PolicyViolationException, RepositoryException {
        ItemCoordinatesImpl[] volumeIDs = new ItemCoordinatesImpl[] {new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[2]), 
                new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[1]),
                new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[3])};

        List<ItemCoordinatesImpl> idList = Arrays.asList(volumeIDs);
        
        VolumeValidityChecker checker = new VolumeValidityChecker(hectorResource, parameterContainer, policyCheckerRegistry);
        checker.validateRequest(idList);
    }
    
    // This case tests that a PolicyViolationException should be raised when total number of pages requested exceeds max total page limit of 10
    @Test(expected = PolicyViolationException.class)
    public void testMaxTotalPagesViolation2() throws KeyNotFoundException, PolicyViolationException, RepositoryException {
        ItemCoordinatesImpl pageID1 = new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[1]);
        pageID1.addPageSequence("00000004");
        pageID1.addPageSequence("00000002");
        pageID1.addPageSequence("00000003");
        pageID1.addPageSequence("00000001");
        

        ItemCoordinatesImpl pageID2 = new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[0]);
        pageID2.addPageSequence("00000001");
        pageID2.addPageSequence("00000004");
        pageID2.addPageSequence("00000002");
        pageID2.addPageSequence("00000003");


        ItemCoordinatesImpl pageID3 = new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[2]);
        pageID3.addPageSequence("00000002");
        pageID3.addPageSequence("00000004");
        pageID3.addPageSequence("00000003");

        
        List<ItemCoordinatesImpl> idList = new ArrayList<ItemCoordinatesImpl>(2);
        idList.add(pageID1);
        idList.add(pageID2);
        idList.add(pageID3);
        
        PageValidityChecker checker = new PageValidityChecker(hectorResource, parameterContainer, policyCheckerRegistry);
        checker.validateRequest(idList);
    }
    
    // This case tests that a PolicyViolationException should be raised when a requested volume has number of pages exceeding max pages per volume limit of 5
    @Test(expected = PolicyViolationException.class)
    public void testMaxPagesPerVolumeViolation1() throws KeyNotFoundException, PolicyViolationException, RepositoryException {
        ItemCoordinatesImpl[] volumeIDs = new ItemCoordinatesImpl[] {new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[2]), 
                new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[0])};

        List<ItemCoordinatesImpl> idList = Arrays.asList(volumeIDs);
        
        VolumeValidityChecker checker = new VolumeValidityChecker(hectorResource, parameterContainer, policyCheckerRegistry);
        checker.validateRequest(idList);
        
    }
    
    // This case tests that a PolicyViolationException should be raised when number of pages requested for a volume exceeeds max pages per volume limit of 5
    @Test(expected = PolicyViolationException.class)
    public void testMaxPagesPerVolumeViolation2() throws KeyNotFoundException, PolicyViolationException, RepositoryException {
        ItemCoordinatesImpl pageID1 = new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[1]);
        pageID1.addPageSequence("00000004");
        pageID1.addPageSequence("00000002");
        pageID1.addPageSequence("00000003");
        pageID1.addPageSequence("00000001");
        

        ItemCoordinatesImpl pageID2 = new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[0]);
        pageID2.addPageSequence("00000001");
        pageID2.addPageSequence("00000004");
        pageID2.addPageSequence("00000002");
        pageID2.addPageSequence("00000003");
        pageID2.addPageSequence("00000006");
        pageID2.addPageSequence("00000005");


        ItemCoordinatesImpl pageID3 = new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[2]);
        pageID3.addPageSequence("00000002");
        pageID3.addPageSequence("00000004");
        pageID3.addPageSequence("00000003");

        
        List<ItemCoordinatesImpl> idList = new ArrayList<ItemCoordinatesImpl>(2);
        idList.add(pageID1);
        idList.add(pageID2);
        idList.add(pageID3);
        
        PageValidityChecker checker = new PageValidityChecker(hectorResource, parameterContainer, policyCheckerRegistry);
        checker.validateRequest(idList);
        
    }

    // This case tests that by disabling all limits there should not be any violations for VolumeIDs
    // This case modifies the default limits settings
    @Test
    public void testVolumesNoViolation1() throws KeyNotFoundException, PolicyViolationException, RepositoryException, Exception {
        
        boolean testPassed = false;

        ItemCoordinatesImpl[] volumeIDs = new ItemCoordinatesImpl[] {new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[2]), 
                                                               new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[0]),
                                                               new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[3]),
                                                               new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[1])};
        
        
        try {
            List<ItemCoordinatesImpl> idList = Arrays.asList(volumeIDs);
    
            setupWithLimits("0", "0", "0");
            
            VolumeValidityChecker checker = new VolumeValidityChecker(hectorResource, parameterContainer, policyCheckerRegistry);
            checker.validateRequest(idList);
            testPassed = true;
        } finally {
            setupWithDefaultLimits();
            Assert.assertTrue(testPassed);
        }
        
    }
    

    
    // This case tests that by disabling all limits there should not be any violations for VolumePageIDs
    // This case modifies the default limits settings
    @Test
    public void testPagesNoViolation1() throws KeyNotFoundException, PolicyViolationException, RepositoryException, Exception {
        
        boolean testPassed = false;
        
        ItemCoordinatesImpl[] volumePageIDs = new ItemCoordinatesImpl[] {new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[3]),
                                                                           new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[1]),
                                                                           new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[0]),
                                                                           new ItemCoordinatesImpl(TestHectorResource.VOLUME_IDS[2])};

        volumePageIDs[0].addPageSequence("00000001");
        volumePageIDs[0].addPageSequence("00000003");
        volumePageIDs[0].addPageSequence("00000002");
        
        volumePageIDs[1].addPageSequence("00000004");
        volumePageIDs[1].addPageSequence("00000001");
        volumePageIDs[1].addPageSequence("00000002");
        volumePageIDs[1].addPageSequence("00000005");
        volumePageIDs[1].addPageSequence("00000003");

        volumePageIDs[2].addPageSequence("00000003");
        volumePageIDs[2].addPageSequence("00000002");
        volumePageIDs[2].addPageSequence("00000004");
        volumePageIDs[2].addPageSequence("00000006");
        volumePageIDs[2].addPageSequence("00000001");
        volumePageIDs[2].addPageSequence("00000005");

        volumePageIDs[3].addPageSequence("00000001");
        volumePageIDs[3].addPageSequence("00000004");
        volumePageIDs[3].addPageSequence("00000003");
        volumePageIDs[3].addPageSequence("00000002");

        try {
            List<ItemCoordinatesImpl> idList = Arrays.asList(volumePageIDs);
    
            setupWithLimits("0", "0", "0");
            
            PageValidityChecker checker = new PageValidityChecker(hectorResource, parameterContainer, policyCheckerRegistry);
            checker.validateRequest(idList);
            testPassed = true;
        } finally {
            setupWithDefaultLimits();
        }
        
        Assert.assertTrue(testPassed);
        
    }
    

}

