package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;

/**
 * Ice spike block - spikes with icy properties.
 */
public class BlockSpikeIceDown extends BlockSpike {

    public BlockSpikeIceDown(double x, double y, ObjectHandler handler, int groupId) {
        super(x, y, ID.BlockSpikeIceDown, groupId, handler, R.drawable.ice_spikes_d);
    }

    public BlockSpikeIceDown(Region region, ObjectHandler handler, int groupId) {
        super(region, ID.BlockSpikeIceDown, groupId, handler, R.drawable.ice_spikes_d);
    }
}
