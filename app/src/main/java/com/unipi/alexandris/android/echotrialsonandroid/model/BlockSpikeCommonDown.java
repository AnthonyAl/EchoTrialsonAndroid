package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;

/**
 * Common spike block - standard hazardous spike.
 */
public class BlockSpikeCommonDown extends BlockSpike {

    public BlockSpikeCommonDown(double x, double y, ObjectHandler handler, int groupId) {
        super(x, y, ID.BlockSpikeCommonDown, groupId, handler, R.drawable.spikes_d);
    }

    public BlockSpikeCommonDown(Region region, ObjectHandler handler, int groupId) {
        super(region, ID.BlockSpikeCommonDown, groupId, handler, R.drawable.spikes_d);
    }
}
