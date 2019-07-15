package com.balanstudios.showerly;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int spaceHeight;

    public SpaceItemDecoration(int spaceHeight) {
        this.spaceHeight = spaceHeight;
    }

    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.bottom = (int) (spaceHeight / 1.25);
        outRect.top = (int) (spaceHeight / 1.25);
        outRect.left = (int) (spaceHeight * 1.5);
        outRect.right = (int) (spaceHeight * 1.5);
    }

}
