package com.ribbit.android.CustomClasses;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

/**
 * Created by aballiu_admin on 7/21/15.
 */
public class FlashScreenImageView extends ImageView {
    public FlashScreenImageView(Context context) {
        super(context);
    }

    public FlashScreenImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlashScreenImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            YoYo.with(Techniques.FadeIn).delay(1000).playOn(this);
            setVisibility(View.INVISIBLE);
        }
        if (visibility == View.INVISIBLE){
            YoYo.with(Techniques.FadeOut).delay(1000).playOn(this);
        }
        super.setVisibility(visibility);
    }

}
