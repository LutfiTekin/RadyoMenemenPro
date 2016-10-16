package com.incitorrent.radyo.menemen.pro.utils;

/**
 * RadyoMenemenPro Created by lutfi on 16.10.2016.
 */

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;


public class WrapContentLinearLayoutManager extends LinearLayoutManager {
    /**
     * Fix for "java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter"
     * @param context
     */
    public WrapContentLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Log.v("WrapContentLinearLM", "IndexOutOfBoundsException is catched " + e.toString());
        }
    }
}