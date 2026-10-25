package com.example.watchrecorder.tile

import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.example.watchrecorder.presentation.MainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * 워치페이스 옆 타일. 탭하면 앱(MainActivity)을 열며 바로 녹음을 시작한다.
 * (안드로이드14 백그라운드 포그라운드서비스 시작 제한 때문에, 서비스를 직접 띄우지 않고
 *  앱을 여는 방식 사용 — 앱이 열리면서 녹음을 시작함.)
 */
class RecorderTileService : TileService() {

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest,
    ): ListenableFuture<TileBuilders.Tile> {
        val tile = TileBuilders.Tile.Builder()
            .setResourcesVersion(RES_VERSION)
            .setTileTimeline(
                TimelineBuilders.Timeline.fromLayoutElement(tileLayout()),
            )
            .build()
        return Futures.immediateFuture(tile)
    }

    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ): ListenableFuture<ResourceBuilders.Resources> =
        Futures.immediateFuture(
            ResourceBuilders.Resources.Builder().setVersion(RES_VERSION).build(),
        )

    private fun tileLayout(): LayoutElementBuilders.LayoutElement {
        val launch = ActionBuilders.LaunchAction.Builder()
            .setAndroidActivity(
                ActionBuilders.AndroidActivity.Builder()
                    .setPackageName(packageName)
                    .setClassName(MainActivity::class.java.name)
                    .build(),
            )
            .build()

        val clickable = ModifiersBuilders.Clickable.Builder()
            .setId("start_recording")
            .setOnClick(launch)
            .build()

        return LayoutElementBuilders.Box.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder().setClickable(clickable).build(),
            )
            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText("● 녹음 시작")
                    .setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder()
                            .setSize(sp(16f))
                            .setColor(argb(0xFFFFFFFF.toInt()))
                            .build(),
                    )
                    .build(),
            )
            .build()
    }

    companion object {
        private const val RES_VERSION = "1"
    }
}
