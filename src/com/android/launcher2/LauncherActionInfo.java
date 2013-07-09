package com.android.launcher2;

import android.content.ContentValues;

import java.util.Arrays;

class LauncherActionInfo extends ShortcutInfo {

    /*
     * The launcher action
     */
    LauncherAction.Action action;

    LauncherActionInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_LAUNCHER_ACTION;
    }

    @Override
    void onAddToDatabase(ContentValues values) {
        super.onAddToDatabase(values);

        String titleStr = title != null && (customTitle || itemType == LauncherSettings.Favorites.ITEM_TYPE_LAUNCHER_ACTION) ?
                title.toString() : null;

        values.put(LauncherSettings.BaseLauncherColumns.TITLE, titleStr);

        String uri = null;
        values.put(LauncherSettings.BaseLauncherColumns.INTENT, uri);

        String actionText = action != null ? action.name() : null;
        values.put(LauncherSettings.Favorites.LAUNCHER_ACTION, actionText);
    }

    @Override
    public String toString() {
        return "LauncherActionInfo (title=" + title.toString() + "intent=" + intent + "id=" + this.id
                + " type=" + this.itemType + " container=" + this.container + " screen=" + screen
                + " cellX=" + cellX + " cellY=" + cellY + " spanX=" + spanX + " spanY=" + spanY
                + " dropPos=" + Arrays.toString(dropPos) + ")";
    }

}