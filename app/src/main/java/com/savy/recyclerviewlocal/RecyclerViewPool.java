package com.savy.recyclerviewlocal;

import android.util.SparseArray;

import java.util.ArrayList;

public class RecyclerViewPool {

    //缓存view的集合，一个二维的集合
    static class ScrapData {
        final ArrayList<ViewHolder> mScrapHeap = new ArrayList<>();
    }

    //使用sparseArray提升hashmap在手机上的搜索效率
    SparseArray<ScrapData> mScrap = new SparseArray<>();


    //get
    public ViewHolder getRecyclerView(int type) {

        ScrapData scrapData = mScrap.get(type);
        if (scrapData != null && !scrapData.mScrapHeap.isEmpty()) {
            //有一个跟type类似的viewholder在heap中可以被用来复用

            final ArrayList<ViewHolder> scrapHeap = scrapData.mScrapHeap;

            for (int i = scrapHeap.size(); i > 0; i--) {
                return scrapHeap.remove(i - 1);
            }
        }

        return null;
    }


    private ScrapData getScrapDataForType(int viewType) {
        ScrapData scrapData = mScrap.get(viewType);
        if (scrapData == null) {
            scrapData = new ScrapData();
            mScrap.put(viewType, scrapData);
        }

        return scrapData;
    }

    //set
    public void putRecyclerView(ViewHolder scrap) {
        final int viewType = scrap.getItemType();
        final ArrayList<ViewHolder> scrapHeap = getScrapDataForType(viewType).mScrapHeap;
        scrapHeap.add(scrap);
    }
}
