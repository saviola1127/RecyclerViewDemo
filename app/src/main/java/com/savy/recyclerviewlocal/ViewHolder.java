package com.savy.recyclerviewlocal;

import android.view.View;

public class ViewHolder {

    public View itemView;
    private int mItemType = -1;

    public ViewHolder(View itemView) {
        this.itemView = itemView;
    }

    public int getItemType() {
        return mItemType;
    }

    public void setItemType(int itemType) {
        this.mItemType = itemType;
    }
}
