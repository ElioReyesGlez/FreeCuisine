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

import static com.erg.freecuisine.util.Constants.HOME_RECOMMENDED_MAIN_TAG;
import static com.erg.freecuisine.util.Constants.LINKS_FIRE_BASE_REFERENCE;
import static com.erg.freecuisine.util.Constants.MAIN_URL_FIRE_BASE_REFERENCE;
import static com.erg.freecuisine.util.Constants.RECETA_GRATIS_COLUMN;

public class FireBaseHelper {
    private static final String TAG = "FireBaseHelper";

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference linksReference, mainUrl;


    public FireBaseHelper() {
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    public void getLinks(final OnFireBaseListenerDataStatus dataStatus) {
        linksReference = firebaseDatabase.getReference(LINKS_FIRE_BASE_REFERENCE);
        List<LinkModel> links = new ArrayList<>();
        linksReference.addValueEventListener(new ValueEventListener() {
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

    public void getMainUrl(final OnFireBaseListenerDataStatus dataStatus) {
        linksReference = firebaseDatabase.getReference(MAIN_URL_FIRE_BASE_REFERENCE);
        linksReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LinkModel link = new LinkModel();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    link.setUrl(snap.child(RECETA_GRATIS_COLUMN).getValue(String.class));
                    break;
                }
                dataStatus.onMainUrlLoaded(link);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled: ", error.toException());
            }
        });
    }
}
