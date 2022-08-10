package com.example.watchApp.pizzawatchface.tile;

/*

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.annotation.NonNull;
import androidx.wear.tiles.LayoutElementBuilders;
import androidx.wear.tiles.RequestBuilders;
import androidx.wear.tiles.ResourceBuilders;
import androidx.wear.tiles.TileBuilders;
import androidx.wear.tiles.TileService;
import androidx.wear.tiles.TimelineBuilders;

import static androidx.wear.tiles.ColorBuilders.argb;

 class MyTileService extends TileService {
    private static final String RESOURCES_VERSION = "1";

    @NonNull
    @Override
    protected ListenableFuture<TileBuilders.Tile> onTileRequest(
            @NonNull RequestBuilders.TileRequest requestParams
    ) {
        return Futures.immediateFuture(new TileBuilders.Tile.Builder()
                .setResourcesVersion(RESOURCES_VERSION)
                .setFreshnessIntervalMillis(60 * 60 * 1000) // 60 minutes
                .setTimeline(new TimelineBuilders.Timeline.Builder()
                        .addTimelineEntry(new TimelineBuilders.TimelineEntry.Builder()
                                .setLayout(new LayoutElementBuilders.Layout.Builder()
                                        .setRoot(new LayoutElementBuilders.Text.Builder()
                                                .setText("Hello world!")
                                                .setFontStyle(new LayoutElementBuilders.FontStyle.Builder()
                                                        .setColor(argb(0xFF000000)).build()
                                                ).build()
                                        ).build()
                                ).build()
                        ).build()
                ).build()
        );
    }

    @NonNull
    @Override
    protected ListenableFuture<ResourceBuilders.Resources> onResourcesRequest(@NonNull RequestBuilders.ResourcesRequest requestParams) {
        return Futures.immediateFuture(new ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()
        );
    }

}*/
