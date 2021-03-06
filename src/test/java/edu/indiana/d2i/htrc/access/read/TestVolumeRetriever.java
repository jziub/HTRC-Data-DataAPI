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
# File:  TestVolumeRetriever.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.read;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.ContentReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.exception.KeyNotFoundException;
import edu.indiana.d2i.htrc.access.id.ItemCoordinatesParserFactory;
import edu.indiana.d2i.htrc.access.id.ItemCoordinatesImpl;
import edu.indiana.d2i.htrc.access.read.VolumeReaderImpl.ContentReaderImpl;

/**
 * @author Yiming Sun
 *
 */
public class TestVolumeRetriever implements VolumeRetriever {

    private List<VolumeReader> volumeReaders = null;
    private Iterator<VolumeReader> volumeReaderIterator = null;
    
    public TestVolumeRetriever() throws Exception {
        volumeReaders = new ArrayList<VolumeReader>();

        volumeReaders.add(generateFakeVolumeReader(1, 4));
        volumeReaders.add(generateFakeVolumeReader(2, 10));
        volumeReaders.add(generateFakeVolumeReader(3, 7));
        
        volumeReaderIterator = volumeReaders.iterator();
    }
    
    protected VolumeReader generateFakeVolumeReader(int volumeIDIndex, int maxPageSequence) throws Exception {
        ItemCoordinatesImpl pageId = new ItemCoordinatesImpl("test.volume/id/" + volumeIDIndex);
        List<ContentReader> pageReaders = new ArrayList<ContentReader>();
        List<ContentReader> metadataReaders = new ArrayList<ContentReader>();
        
        for (int i = 1; i <= maxPageSequence; i++) {
            String pageSequenceString = ItemCoordinatesParserFactory.Parser.generatePageSequenceString(i);
            pageId.addPageSequence(pageSequenceString);
            
            ContentReader pageReader = new ContentReaderImpl(pageSequenceString, ("the content of page " + i + " for volume " + pageId.getVolumeID()).getBytes("utf-8"));
            pageReaders.add(pageReader);
        }
        
        VolumeReader volumeReader = new VolumeReaderImpl(pageId);
        volumeReader.setPages(pageReaders);

        String fakeMETS = generateFakeMETSString(pageId.getVolumeID());
        ContentReader metadataReader = new ContentReaderImpl(HectorResource.CN_VOLUME_METS, fakeMETS.getBytes("utf-8"));
        metadataReaders.add(metadataReader);
        volumeReader.setMetadata(metadataReaders);

        return volumeReader;
    }
    
    
    protected String generateFakeMETSString(String volumeID) {
        // TODO: ultimately it would be good to make a schema-conforming METS so that if some tests try to validate
        // the fake data against the METS schema, it won't fail.  But for now it is okay, since all tests are just
        // to retrieve the data without any validation.
        StringBuilder builder = new StringBuilder("<?xml version=\"1.0\">");
        builder.append("<METS:mets xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:METS=\"http://www.loc.gov/METS\" OBJID=\"");
        builder.append(volumeID);
        builder.append("\"></METS>");

        return builder.toString();
    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.VolumeRetriever#hasMoreVolumes()
     */
    @Override
    public boolean hasMoreVolumes() {
        return volumeReaderIterator.hasNext();
    }

    /**
     * @see edu.indiana.d2i.htrc.access.VolumeRetriever#nextVolume()
     */
    @Override
    public VolumeReader nextVolume() throws KeyNotFoundException {
        return volumeReaderIterator.next();
    }

    
}

