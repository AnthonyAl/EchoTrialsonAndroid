package com.unipi.alexandris.android.echotrialsonandroid.utility.audio;

import android.view.View;

import com.unipi.alexandris.android.echotrialsonandroid.data.GameConstants;

public class ButtonSoundHelper {

    public static void addClickSound(View button, View.OnClickListener originalListener) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play click sound using the cosmetic sound system
                GameConstants.INSTANCE.getSoundManager().playSound(SoundID.UI_BUTTON_CLICK);
                
                // Execute the original click action
                if (originalListener != null) {
                    originalListener.onClick(v);
                }
            }
        });
    }

    public static void addClickSound(View button) {
        addClickSound(button, null);
    }

    public static void addClickSounds(View... buttons) {
        for (View button : buttons) {
            if (button != null) {
                addClickSound(button);
            }
        }
    }
}
