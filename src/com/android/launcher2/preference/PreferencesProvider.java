/*
 * Copyright (C) 2011 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher2.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.android.launcher2.LauncherApplication;
import com.android.launcher2.Workspace;
import com.android.launcher2.AppsCustomizePagedView;

import java.util.Map;

public final class PreferencesProvider {
    public static final String PREFERENCES_KEY = "com.android.launcher_preferences";

    public static final String PREFERENCES_CHANGED = "preferences_changed";

    private static Map<String, Object> sKeyValues;

    @SuppressWarnings("unchecked")
    public static void load(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
        sKeyValues = (Map<String, Object>)preferences.getAll();
    }

    private static int getInt(String key, int def) {
        return sKeyValues.containsKey(key) && sKeyValues.get(key) instanceof Integer ?
                (Integer) sKeyValues.get(key) : def;
    }

    private static boolean getBoolean(String key, boolean def) {
        return sKeyValues.containsKey(key) && sKeyValues.get(key) instanceof Boolean ?
                (Boolean) sKeyValues.get(key) : def;
    }

    private static void setBoolean(Context ctx, String key, boolean value) {
        SharedPreferences preferences = ctx.getSharedPreferences(PREFERENCES_KEY, 0);
        Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply(); // For better performance
        sKeyValues.put(key, Boolean.valueOf(value));
    }

    private static String getString(String key, String def) {
        return sKeyValues.containsKey(key) && sKeyValues.get(key) instanceof String ?
                (String) sKeyValues.get(key) : def;
    }

    public static class Interface {
        public static class Homescreen {
            public static int getNumberHomescreens() {
                return getInt("ui_homescreen_screens", 7);
            }
            public static void setNumberHomescreens(Context context, int count) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                Editor editor = preferences.edit();
                editor.putInt("ui_homescreen_screens", count);
                editor.commit();
            }
            public static int getDefaultHomescreen(int def) {
                return getInt("ui_homescreen_default_screen", def + 1) - 1;
            }
            public static void setDefaultHomescreen(Context context, int def) {
                final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
                Editor editor = preferences.edit();
                editor.putInt("ui_homescreen_default_screen", def);
                editor.commit();
            }
            public static int getCellCountX(int def) {
                String[] values = getString("ui_homescreen_grid", "0|" + def).split("\\|");
                try {
                    return Integer.parseInt(values[1]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static int getCellCountY(int def) {
                String[] values = getString("ui_homescreen_grid", def + "|0").split("\\|");
                try {
                    return Integer.parseInt(values[0]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static boolean getStretchScreens() {
                return getBoolean("ui_homescreen_stretch_screens", true);
            }
            public static boolean getShowSearchBar() {
                return getBoolean("ui_homescreen_general_search", true);
            }
            public static int getSearchBarBackground() {
                return Integer.parseInt(getString("ui_homescreen_search_bar_background", "0"));
            }
            public static boolean getResizeAnyWidget() {
                return getBoolean("ui_homescreen_general_resize_any_widget", true);
            }
            public static int getIconScale(int def) {
                return getInt("ui_homescreen_icon_scale", def);
            }
            public static boolean getHideIconLabels() {
                return getBoolean("ui_homescreen_general_hide_icon_labels", false);
            }
            public static class Scrolling {
                public static Workspace.TransitionEffect getTransitionEffect(String def) {
                    try {
                        return Workspace.TransitionEffect.valueOf(
                                getString("ui_homescreen_scrolling_transition_effect", def));
                    } catch (IllegalArgumentException iae) {
                        // Continue
                    }

                    try {
                        return Workspace.TransitionEffect.valueOf(def);
                    } catch (IllegalArgumentException iae) {
                        // Continue
                    }

                    return Workspace.TransitionEffect.Standard;
                }
                public static boolean getScrollWallpaper() {
                    return getBoolean("ui_homescreen_scrolling_scroll_wallpaper", true);
                }
                public static boolean getWallpaperHack(boolean def) {
                    return getBoolean("ui_homescreen_scrolling_wallpaper_hack", def);
                }
                public static int getWallpaperSize() {
                    return getInt("ui_homescreen_scrolling_wallpaper_size", 2);
                }
                public static boolean getFadeInAdjacentScreens(boolean def) {
                    return getBoolean("ui_homescreen_scrolling_fade_adjacent_screens", def);
                }
                public static boolean getShowOutlines(boolean def) {
                    return getBoolean("ui_homescreen_scrolling_show_outlines", def);
                }
            }
            public static class Indicator {
                public static boolean getShowScrollingIndicator() {
                    return getBoolean("ui_homescreen_indicator_enable", true);
                }
                public static boolean getFadeScrollingIndicator() {
                    return getBoolean("ui_homescreen_indicator_fade", true);
                }
                public static int getScrollingIndicatorPosition() {
                    return Integer.parseInt(getString("ui_homescreen_indicator_position", "0"));
                }
            }

            public static class FolderIconStyle {
                public static int getFolderIconStyle() {
                    return Integer.parseInt(getString("ui_homescreen_folder_style", "0"));
                }
                public static int getFolderIconBackground() {
                    return Integer.parseInt(getString("ui_homescreen_folder_background", "0"));
                }
            }

            public static class Gestures {
                public static String getUpGestureAction() {
                    return getString("ui_homescreen_up_gesture", "toggle_status_bar");
                }
                public static String getDownGestureAction() {
                    return getString("ui_homescreen_down_gesture", "expand_status_bar");
                }
                public static String getPinchGestureAction() {
                    return getString("ui_homescreen_pinch_gesture", "show_previews");
                }
                public static String getSpreadGestureAction() {
                    return getString("ui_homescreen_spread_gesture", "open_app_drawer");
                }
            }
        }

        public static class Drawer {
            public static int getCellCountX() {
                int def = 4;
                String[] values = getString("ui_drawer_grid", "0|" + def).split("\\|");
                try {
                    return Integer.parseInt(values[1]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static int getCellCountY() {
                int def = 5;
                String[] values = getString("ui_drawer_grid", def + "|0").split("\\|");
                try {
                    return Integer.parseInt(values[0]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static int getWidgetCountX() {
                int def = 2;
                String[] values = getString("ui_drawer_widget_grid", "0|" + def).split("\\|");
                try {
                    return Integer.parseInt(values[1]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static int getWidgetCountY() {
                int def = 3;
                String[] values = getString("ui_drawer_widget_grid", def + "|0").split("\\|");
                try {
                    return Integer.parseInt(values[0]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static boolean getWidgetIcons() {
                return getBoolean("ui_drawer_widget_icon_style", false);
            }
            public static int getTabStyle() {
                return Integer.parseInt(getString("ui_drawer_tab_style", "0"));
            }
            public static int getDrawerTransparency() {
                return getInt("ui_drawer_transparency", 50);
            }
            public static boolean getVertical() {
                return getString("ui_drawer_orientation", "horizontal").equals("vertical");
            }
            public static boolean getJoinWidgetsApps() {
                return getBoolean("ui_drawer_widgets_join_apps", true);
            }
            public static boolean getListActions() {
                return getBoolean("ui_drawer_widgets_list_actions", false);
            }
            public static String getHiddenApps() {
                return getString("ui_drawer_hidden_apps", "");
            }
            public static int getIconScale(int def) {
                return getInt("ui_drawer_icon_scale", def);
            }
            public static class Scrolling {
                public static AppsCustomizePagedView.TransitionEffect getTransitionEffect(String def) {
                    try {
                        return AppsCustomizePagedView.TransitionEffect.valueOf(
                                getString("ui_drawer_scrolling_transition_effect", def));
                    } catch (IllegalArgumentException iae) {
                        // Continue
                    }

                    try {
                        return AppsCustomizePagedView.TransitionEffect.valueOf(def);
                    } catch (IllegalArgumentException iae) {
                        // Continue
                    }

                    return AppsCustomizePagedView.TransitionEffect.Standard;
                }
                public static boolean getFadeInAdjacentScreens() {
                    return getBoolean("ui_drawer_scrolling_fade_adjacent_screens", false);
                }
            }
            public static class Indicator {
                public static boolean getShowScrollingIndicator() {
                    return getBoolean("ui_drawer_indicator_enable", true);
                }
                public static boolean getFadeScrollingIndicator() {
                    return getBoolean("ui_drawer_indicator_fade", true);
                }
                public static int getScrollingIndicatorPosition() {
                    return Integer.parseInt(getString("ui_drawer_indicator_position", "0"));
                }
            }
        }

        public static class Dock {
            public static boolean getShowDock() {
                return getBoolean("ui_dock_enabled", true);
            }
            public static void setShowDock(Context ctx, boolean value) {
                setBoolean(ctx, "ui_dock_enabled", value);
            }
            public static boolean getHideIconLabels() {
                return getBoolean("ui_dock_hide_icon_labels", true);
            }
            public static int getNumberPages() {
                return getInt("ui_dock_pages", 1);
            }
            public static int getDefaultPage(int def) {
                return getInt("ui_dock_default_page", def + 1) - 1;
            }
            public static int getNumberIcons(int def) {
                return getInt("ui_dock_icons", def);
            }
            public static int getIconScale(int def) {
                return getInt("ui_dock_icon_scale", def);
            }
            public static int getDockBackground() {
                return Integer.parseInt(getString("ui_dock_background", "0"));
            }
            public static boolean getShowDivider() {
                return getBoolean("ui_dock_divider", false);
            }
        }

        public static class General {
            public static boolean getAutoRotate(boolean def) {
                return getBoolean("ui_general_orientation", def);
            }
            public static boolean getLockWorkspace() {
                return getBoolean("ui_general_lock_workspace", false);
            }
            public static void setLockWorkspace(Context ctx, boolean value) {
                setBoolean(ctx, "ui_general_lock_workspace", value);
            }
            public static boolean getFullscreenMode() {
                return getBoolean("ui_general_fullscreen", false);
            }
            public static void setFullscreenMode(Context ctx, boolean value) {
                setBoolean(ctx, "ui_general_fullscreen", value);
            }
        }

        public static class LiveWallpaper {
            public static int getBeamColour() {
                return Integer.parseInt(getString("livewallpaper_beam_colour", "0"));
            }
            public static int getDotColour() {
                return Integer.parseInt(getString("livewallpaper_dot_colour", "0"));
            }
            public static int getBackgroundColour() {
                return Integer.parseInt(getString("livewallpaper_background_colour", "0"));
            }
        }
    }

    public static class Application {

    }
}
