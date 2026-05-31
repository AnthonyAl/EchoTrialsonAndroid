package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;

/**
 * Common spike block - standard hazardous spike.
 */
public class BlockSpikeCommonLeft extends BlockSpike {

    public BlockSpikeCommonLeft(double x, double y, ObjectHandler handler, int groupId) {
        super(x, y, ID.BlockSpikeCommonLeft, groupId, handler, R.drawable.spikes_l);
    }

    public BlockSpikeCommonLeft(Region region, ObjectHandler handler, int groupId) {
        super(region, ID.BlockSpikeCommonLeft, groupId, handler, R.drawable.spikes_l);
    }
}
