package de.funkedigital.autotagging.semantic.entities;

import com.google.common.base.Splitter;

import java.util.List;

public class UnicornStore {

    private String assetId;

    public UnicornStore(String assetId) {
        this.assetId = assetId;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getArticleId() {
        List<String> asset = Splitter.on("-").splitToList(assetId);
        if (asset.size() == 2) {
            return asset.get(1);
        }
        return null;
    }
}
