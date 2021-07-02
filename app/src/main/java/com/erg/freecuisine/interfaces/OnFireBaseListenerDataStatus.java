package com.erg.freecuisine.interfaces;

import com.erg.freecuisine.models.LinkModel;

import java.util.List;

public interface OnFireBaseListenerDataStatus {
    void onLinksLoaded(List<LinkModel> links, List<String> keys);
    void onMainUrlsLoaded(List<LinkModel> links, List<String> keys);
    void onConnectionListener(boolean isConnected);
//    void dataIsInserted();
//    void dataIsUpdated();
//    void dataIsDeleted();
}
