package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;

/**
 * Common spike block - standard hazardous spike.
 */
public class BlockSpikeCommon extends BlockSpike {

    public BlockSpikeCommon(double x, double y, ObjectHandler handler, int groupId) {
        super(x, y, ID.BlockSpikeCommon, groupId, handler, R.drawable.spikes);
    }
    
    public BlockSpikeCommon(Region region, ObjectHandler handler, int groupId) {
        super(region, ID.BlockSpikeCommon, groupId, handler, R.drawable.spikes);
    }
}
