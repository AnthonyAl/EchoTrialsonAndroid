package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;

/**
 * Ice spike block - spikes with icy properties.
 */
public class BlockSpikeIceRight extends BlockSpike {

    public BlockSpikeIceRight(double x, double y, ObjectHandler handler, int groupId) {
        super(x, y, ID.BlockSpikeIceRight, groupId, handler, R.drawable.ice_spikes_r);
    }

    public BlockSpikeIceRight(Region region, ObjectHandler handler, int groupId) {
        super(region, ID.BlockSpikeIceRight, groupId, handler, R.drawable.ice_spikes_r);
    }
}
