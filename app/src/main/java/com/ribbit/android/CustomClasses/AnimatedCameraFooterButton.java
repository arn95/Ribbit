package com.ribbit.android.CustomClasses;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

/**
 * Created by aballiu_admin on 7/27/15.
 */
public class AnimatedCameraFooterButton extends ImageButton {
    public AnimatedCameraFooterButton(Context context) {
        super(context);
    }

    public AnimatedCameraFooterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatedCameraFooterButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        if (visibility == VISIBLE){
            YoYo.with(Techniques.SlideInUp).playOn(this);
        }
        else
            YoYo.with(Techniques.SlideInDown).playOn(this);
    }
}
