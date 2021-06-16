package com.erg.freecuisine.controller.network.helpers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.erg.freecuisine.interfaces.OnFireBaseListenerDataStatus;
import com.erg.freecuisine.models.LinkModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.erg.freecuisine.util.Constants.LINKS_FIRE_BASE_REFERENCE;

public class FireBaseHelper {
    private static final String TAG = "FireBaseHelper";

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference rootReference;
    private List<LinkModel> links = new ArrayList<>();


    public FireBaseHelper() {
        firebaseDatabase =  FirebaseDatabase.getInstance();
        rootReference = firebaseDatabase.getReference(LINKS_FIRE_BASE_REFERENCE);
        Log.d(TAG, "Constructor: FirebaseReference: " + rootReference.toString());
    }

    public void getLinks(final OnFireBaseListenerDataStatus dataStatus) {
        Log.d(TAG, "getLinks");
        rootReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    links.clear();
                    List<String> keys = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    keys.add(snap.getKey());
                    LinkModel link = snap.getValue(LinkModel.class);
                    links.add(link);
                }
                dataStatus.onLinksLoaded(links, keys);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled: ", error.toException());
            }
        });
    }
}
