package com.unipi.alexandris.android.echotrialsonandroid.model;

import android.graphics.Region;

import com.unipi.alexandris.android.echotrialsonandroid.R;
import com.unipi.alexandris.android.echotrialsonandroid.utility.ObjectHandler;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLSpriteBatch;
import com.unipi.alexandris.android.echotrialsonandroid.utility.opengl.GLTextureManager;

/**
 * Ice spike block - spikes with icy properties.
 */
public class BlockSpikeIce extends BlockSpike {

    public BlockSpikeIce(double x, double y, ObjectHandler handler, int groupId) {
        super(x, y, ID.BlockSpikeIce, groupId, handler, R.drawable.ice_spikes);
    }
    
    public BlockSpikeIce(Region region, ObjectHandler handler, int groupId) {
        super(region, ID.BlockSpikeIce, groupId, handler, R.drawable.ice_spikes);
    }
}
