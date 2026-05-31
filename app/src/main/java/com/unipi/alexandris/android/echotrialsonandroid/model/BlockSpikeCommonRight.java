package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;

/**
 * Common spike block - standard hazardous spike.
 */
public class BlockSpikeCommonRight extends BlockSpike {

    public BlockSpikeCommonRight(double x, double y, ObjectHandler handler, int groupId) {
        super(x, y, ID.BlockSpikeCommonRight, groupId, handler, R.drawable.spikes_r);
    }

    public BlockSpikeCommonRight(Region region, ObjectHandler handler, int groupId) {
        super(region, ID.BlockSpikeCommonRight, groupId, handler, R.drawable.spikes_r);
    }
}
