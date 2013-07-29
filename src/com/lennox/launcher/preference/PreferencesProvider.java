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

package com.lennox.launcher.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.lennox.launcher.Workspace;
import com.lennox.launcher.AppsCustomizePagedView;
import com.lennox.launcher.Hotseat;

import java.util.Map;

public final class PreferencesProvider {
    public static final String PREFERENCES_KEY = "com.lennox.launcher_preferences";

    public static final String PREFERENCES_CHANGED = "preferences_changed";

    public static final String PREFERENCES_VISITED = "preferences_visited";

    // SharedPreferences
    public final static String DEFAULT = "Default";
    public final static String DEFAULT_PACKAGE = "com.lennox.launcher";
    public final static String THEME_PACKAGE_NAME = "icon_theme";

    private static Map<String, Object> sKeyValues;

    @SuppressWarnings("unchecked")
    public static void load(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
        sKeyValues = (Map<String, Object>)preferences.getAll();
    }

    private static boolean getBoolean(String key, boolean def) {
        return sKeyValues.containsKey(key) && sKeyValues.get(key) instanceof Boolean ?
                (Boolean) sKeyValues.get(key) : def;
    }

    private static int getInt(String key, int def) {
        return sKeyValues.containsKey(key) && sKeyValues.get(key) instanceof Integer ?
                (Integer) sKeyValues.get(key) : def;
    }

    private static String getString(String key, String def) {
        return sKeyValues.containsKey(key) && sKeyValues.get(key) instanceof String ?
                (String) sKeyValues.get(key) : def;
    }

    private static void setBoolean(Context ctx, String key, boolean value) {
        SharedPreferences preferences = ctx.getSharedPreferences(PREFERENCES_KEY, 0);
        Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply(); // For better performance
        sKeyValues.put(key, Boolean.valueOf(value));
    }

    private static void setInt(Context ctx, String key, int value) {
        SharedPreferences preferences = ctx.getSharedPreferences(PREFERENCES_KEY, 0);
        Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply(); // For better performance
        sKeyValues.put(key, Integer.valueOf(value));
    }

    private static void setString(Context ctx, String key, String value) {
        SharedPreferences preferences = ctx.getSharedPreferences(PREFERENCES_KEY, 0);
        Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply(); // For better performance
        sKeyValues.put(key, String.valueOf(value));
    }

    public static class Interface {
        public static class Homescreen {
            public static boolean getIndependentHomescreens() {
                return getBoolean("lennox_homescreen_independent_homescreens", false);
            }
            public static int getNumberHomescreens() {
                return getInt("lennox_homescreen_screens", 7);
            }
            public static void setNumberHomescreens(Context context, int count) {
                setInt(context, "lennox_homescreen_screens", count);
            }
            public static int getDefaultHomescreen(int def) {
                return getInt("lennox_homescreen_default_screen", def+1) - 1;
            }
            public static void setDefaultHomescreen(Context context, int def) {
                setInt(context, "lennox_homescreen_default_screen", def);
            }
            public static int getCellCountX(int def, boolean landscape) {
                String[] values = getString("lennox_homescreen_grid", "0|" + def + "|0|" + def).split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 3 : 1]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static int getCellCountY(int def, boolean landscape) {
                String[] values = getString("lennox_homescreen_grid", def + "|0|" + def + "|0").split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 2 : 0]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static boolean getStretchScreens(boolean landscape) {
                int def = landscape ? 1 : 1;
                String[] values = getString("lennox_homescreen_stretch_screens", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static boolean getFitToCells(boolean landscape) {
                int def = landscape ? 0 : 0;
                String[] values = getString("lennox_homescreen_fit_to_cells", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static int getIconScale(boolean landscape) {
                String[] values = getString("lennox_homescreen_icon_scale", "100|100").split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 1 : 0]);
                } catch (NumberFormatException e) {
                    return 100;
                }
            }
            public static int getTextScale(boolean landscape) {
                String[] values = getString("lennox_homescreen_text_scale", "100|100").split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 1 : 0]);
                } catch (NumberFormatException e) {
                    return 100;
                }
            }
            public static boolean getTextPadding(boolean landscape) {
                int def = landscape ? 0 : 0;
                String[] values = getString("lennox_homescreen_text_padding", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static boolean getShowSearchBar(boolean landscape) {
                int def = landscape ? 1 : 1;
                String[] values = getString("lennox_homescreen_general_search", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static int getSearchBarBackground() {
                return Integer.parseInt(getString("lennox_homescreen_search_bar_background", "0"));
            }
            public static boolean getResizeAnyWidget() {
                return getBoolean("lennox_homescreen_general_resize_any_widget", true);
            }
            public static boolean getHideIconLabels(boolean landscape) {
                int def = landscape ? 0 : 0;
                String[] values = getString("lennox_homescreen_general_hide_icon_labels", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static boolean getTabletSearchBar() {
                return getBoolean("lennox_homescreen_general_search_bar_dock", false);
            }
            public static boolean getAllowShortcuts() {
                return getBoolean("lennox_homescreen_auto_install_shortcuts", false);
            }
            public static class Scrolling {
                public static Workspace.TransitionEffect getTransitionEffect(String def) {
                    try {
                        return Workspace.TransitionEffect.valueOf(
                                getString("lennox_homescreen_scrolling_transition_effect", def));
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
                public static boolean getScrollWallpaper(boolean landscape) {
                    int def = landscape ? 1 : 1;
                    String[] values = getString("lennox_homescreen_scrolling_scroll_wallpaper", def + "|" + def).split("\\|");
                    try {
                        return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                    } catch (NumberFormatException e) {
                        return (def == 1);
                    }
                }
                public static boolean getWallpaperHack() {
                    return getBoolean("lennox_homescreen_scrolling_wallpaper_hack", false);
                }
                public static int getWallpaperSize(boolean landscape) {
                    String[] values = getString("lennox_homescreen_scrolling_wallpaper_size", "2|2").split("\\|");
                    try {
                        return Integer.parseInt(values[landscape ? 1 : 0]);
                    } catch (NumberFormatException e) {
                        return 2;
                    }
                }
                public static boolean getVertical() {
                    return getString("lennox_homescreen_orientation", "horizontal").equals("vertical");
                }
                public static boolean getFadeInAdjacentScreens(boolean def) {
                    return getBoolean("lennox_homescreen_scrolling_fade_adjacent_screens", def);
                }
                public static boolean getShowOutlines(boolean def) {
                    return getBoolean("lennox_homescreen_scrolling_show_outlines", def);
                }
                public static boolean getInfiniteScrolling(boolean landscape) {
                    int def = landscape ? 0 : 0;
                    String[] values = getString("lennox_homescreen_scrolling_infinite", def + "|" + def).split("\\|");
                    try {
                        return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                    } catch (NumberFormatException e) {
                        return (def == 1);
                    }
                }
            }
            public static class Indicator {
                public static boolean getShowScrollingIndicator() {
                    return getBoolean("lennox_homescreen_indicator_enable", true);
                }
                public static boolean getFadeScrollingIndicator() {
                    return getBoolean("lennox_homescreen_indicator_fade", true);
                }
                public static int getScrollingIndicatorPosition() {
                    return Integer.parseInt(getString("lennox_homescreen_indicator_position", "0"));
                }
            }

            public static class FolderIconStyle {
                public static int getFolderIconStyle() {
                    return Integer.parseInt(getString("lennox_homescreen_folder_style", "0"));
                }
                public static int getIconScale(boolean landscape) {
                    String[] values = getString("lennox_homescreen_folder_icon_scale", "100|100").split("\\|");
                    try {
                        return Integer.parseInt(values[landscape ? 1 : 0]);
                    } catch (NumberFormatException e) {
                        return 100;
                    }
                }
                public static int getTextScale(boolean landscape) {
                    String[] values = getString("lennox_homescreen_folder_text_scale", "100|100").split("\\|");
                    try {
                        return Integer.parseInt(values[landscape ? 1 : 0]);
                    } catch (NumberFormatException e) {
                        return 100;
                    }
                }
                public static boolean getTextPadding(boolean landscape) {
                    int def = landscape ? 0 : 0;
                    String[] values = getString("lennox_homescreen_folder_text_padding", def + "|" + def).split("\\|");
                    try {
                        return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                    } catch (NumberFormatException e) {
                        return (def == 1);
                    }
                }
            }
        }

        public static class Drawer {
            public static int getCellCountX(int def, boolean landscape) {
                String[] values = getString("lennox_drawer_grid", "0|" + def + "|0|" + def).split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 3 : 1]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static int getCellCountY(int def, boolean landscape) {
                String[] values = getString("lennox_drawer_grid", def + "|0|" + def + "|0").split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 2 : 0]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static int getWidgetCountX(int def, boolean landscape) {
                String[] values = getString("lennox_drawer_widget_grid", "0|" + def + "|0|" + def).split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 3 : 1]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static int getWidgetCountY(int def, boolean landscape) {
                String[] values = getString("lennox_drawer_widget_grid", def + "|0|" + def + "|0").split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 2 : 0]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static boolean getStretchScreens(boolean landscape) {
                int def = landscape ? 1 : 1;
                String[] values = getString("lennox_drawer_stretch_screens", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static boolean getFitToCells(boolean landscape) {
                int def = landscape ? 0 : 0;
                String[] values = getString("lennox_drawer_fit_to_cells", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static int getDrawerTransparency(boolean landscape) {
                String[] values = getString("lennox_drawer_transparency", "50|50").split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 1 : 0]);
                } catch (NumberFormatException e) {
                    return 50;
                }
            }
            public static boolean getVertical() {
                return getString("lennox_drawer_orientation", "horizontal").equals("vertical");
            }
            public static String getHiddenApps() {
                return getString("lennox_drawer_hidden_apps", "");
            }
            public static boolean getRemoveShortcutsOfHiddenApps() {
                return getBoolean("lennox_drawer_remove_hidden_apps_shortcuts", true);
            }
            public static boolean getRemoveWidgetsOfHiddenApps() {
                return getBoolean("lennox_drawer_remove_hidden_apps_widgets", true);
            }
            public static boolean getJoinWidgetsApps() {
                return getBoolean("lennox_drawer_widgets_join_apps", false);
            }
            public static boolean getListActions() {
                return getBoolean("lennox_drawer_widgets_list_actions", false);
            }
            public static int getIconScale(boolean landscape) {
                String[] values = getString("lennox_drawer_icon_scale", "100|100").split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 1 : 0]);
                } catch (NumberFormatException e) {
                    return 100;
                }
            }
            public static int getTextScale(boolean landscape) {
                String[] values = getString("lennox_drawer_text_scale", "100|100").split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 1 : 0]);
                } catch (NumberFormatException e) {
                    return 100;
                }
            }
            public static boolean getTextPadding(boolean landscape) {
                int def = landscape ? 0 : 0;
                String[] values = getString("lennox_drawer_text_padding", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static int getWidgetScale(boolean landscape) {
                String[] values = getString("lennox_drawer_widgets_scale", "100|100").split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 1 : 0]);
                } catch (NumberFormatException e) {
                    return 100;
                }
            }
            public static boolean getDismissDrawerOnTap() {
                return getBoolean("lennox_drawer_dismiss_on_tap", false);
            }
            public static class Scrolling {
                public static AppsCustomizePagedView.TransitionEffect getTransitionEffect(String def) {
                    try {
                        return AppsCustomizePagedView.TransitionEffect.valueOf(
                                getString("lennox_drawer_scrolling_transition_effect", def));
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
                    return getBoolean("lennox_drawer_scrolling_fade_adjacent_screens", true);
                }
                public static boolean getInfiniteScrolling(boolean landscape) {
                    int def = landscape ? 1 : 1;
                    String[] values = getString("lennox_drawer_scrolling_infinite", def + "|" + def).split("\\|");
                    try {
                        return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                    } catch (NumberFormatException e) {
                        return (def == 1);
                    }
                }
            }
            public static class Indicator {
                public static boolean getShowScrollingIndicator() {
                    return getBoolean("lennox_drawer_indicator_enable", true);
                }
                public static boolean getFadeScrollingIndicator() {
                    return getBoolean("lennox_drawer_indicator_fade", true);
                }
                public static int getScrollingIndicatorPosition() {
                    return Integer.parseInt(getString("lennox_drawer_indicator_position", "0"));
                }
            }
        }

        public static class Dock {
            public static boolean getShowDock(boolean landscape) {
                int def = landscape ? 1 : 1;
                String[] values = getString("lennox_dock_enabled", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static void setShowDock(Context context, boolean value, boolean landscape) {
                int newValue = value ? 1 : 0;
                String[] values = getString("lennox_dock_enabled", "1|1").split("\\|");
                if (landscape) {
                    setString(context, "lennox_dock_enabled", values[0] + "|" + newValue);
                } else {
                    setString(context, "lennox_dock_enabled", newValue + "|" + values[1]);
                }
            }
            public static boolean getDockAsOverlay(boolean landscape) {
                int def = landscape ? 0 : 0;
                String[] values = getString("lennox_dock_as_overlay", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static int getNumberPages(boolean landscape) {
                String[] values = getString("lennox_dock_pages", "1|1").split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 1 : 0]);
                } catch (NumberFormatException e) {
                    return 1;
                }
            }
            public static int getDefaultPage(int def, boolean landscape) {
                String[] values = getString("lennox_dock_default_page", (def+1) + "|" + (def+1)).split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 1 : 0]) - 1;
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static int getNumberIcons(int def, boolean landscape) {
                String[] values = getString("lennox_dock_icons", def + "|" + def).split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 1 : 0]);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            public static boolean getStretchScreens(boolean landscape) {
                int def = landscape ? 1 : 1;
                String[] values = getString("lennox_dock_stretch_screens", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static boolean getFitToCells(boolean landscape) {
                int def = landscape ? 1 : 1;
                String[] values = getString("lennox_dock_fit_to_cells", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static boolean getHideIconLabels(boolean landscape) {
                int def = landscape ? 1 : 1;
                String[] values = getString("lennox_dock_hide_icon_labels", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static int getIconScale(boolean landscape) {
                String[] values = getString("lennox_dock_icon_scale", "100|100").split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 1 : 0]);
                } catch (NumberFormatException e) {
                    return 100;
                }
            }
            public static int getTextScale(boolean landscape) {
                String[] values = getString("lennox_dock_text_scale", "100|100").split("\\|");
                try {
                    return Integer.parseInt(values[landscape ? 1 : 0]);
                } catch (NumberFormatException e) {
                    return 100;
                }
            }
            public static boolean getTextPadding(boolean landscape) {
                int def = landscape ? 0 : 0;
                String[] values = getString("lennox_dock_text_padding", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static boolean getShowDivider(boolean landscape) {
                int def = landscape ? 1 : 1;
                String[] values = getString("lennox_dock_divider", def + "|" + def).split("\\|");
                try {
                    return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                } catch (NumberFormatException e) {
                    return (def == 1);
                }
            }
            public static boolean getFadeInAdjacentScreens() {
                return getBoolean("lennox_dock_scrolling_fade_adjacent_screens", true);
            }
            public static class Scrolling {
                public static Hotseat.TransitionEffect getTransitionEffect(String def) {
                    try {
                        return Hotseat.TransitionEffect.valueOf(
                                getString("lennox_dock_scrolling_transition_effect", def));
                    } catch (IllegalArgumentException iae) {
                        // Continue
                    }
                    try {
                        return Hotseat.TransitionEffect.valueOf(def);
                    } catch (IllegalArgumentException iae) {
                        // Continue
                    }
                    return Hotseat.TransitionEffect.Standard;
                }
                public static boolean getFadeInAdjacentScreens() {
                    return getBoolean("lennox_drawer_scrolling_fade_adjacent_screens", true);
                }
                public static boolean getInfiniteScrolling(boolean landscape) {
                    int def = landscape ? 1 : 1;
                    String[] values = getString("lennox_dock_scrolling_infinite", def + "|" + def).split("\\|");
                    try {
                        return (Integer.parseInt(values[landscape ? 1 : 0]) == 1);
                    } catch (NumberFormatException e) {
                        return (def == 1);
                    }
                }
            }
        }

        public static class Icons {
        }

        public static class Gestures {
            public static String getUpGestureAction() {
                return getString("lennox_homescreen_up_gesture", "toggle_status_bar");
            }
            public static String getDownGestureAction() {
                return getString("lennox_homescreen_down_gesture", "expand_status_bar");
            }
            public static String getPinchGestureAction() {
                return getString("lennox_homescreen_pinch_gesture", "show_previews");
            }
            public static String getSpreadGestureAction() {
                return getString("lennox_homescreen_spread_gesture", "open_app_drawer");
            }
            public static String getDoubleTapGestureAction() {
                return getString("lennox_homescreen_double_tap_gesture", "nothing");
            }
        }

        public static class General {
            public static int getOrientationMode() {
                return Integer.parseInt(getString("lennox_general_orientation", "5"));
            }
            public static boolean getLockWorkspace(boolean def) {
                return getBoolean("lennox_general_lock_workspace", def);
            }
            public static void setLockWorkspace(Context ctx, boolean value) {
                setBoolean(ctx, "lennox_general_lock_workspace", value);
            }
            public static boolean getFirstLaunch(Context ctx) {
                boolean returnValue = getBoolean("lennox_general_is_first_launch", true);
                setBoolean(ctx, "lennox_general_is_first_launch", false);
                return returnValue;
            }
            public static boolean getFullscreenMode() {
                return getBoolean("lennox_general_fullscreen", false);
            }
            public static void setFullscreenMode(Context ctx, boolean value) {
                setBoolean(ctx, "lennox_general_fullscreen", value);
            }
            public static boolean getPersistent() {
                return getBoolean("lennox_general_persist", false);
            }
        }

        public static class Theme {
            public static String getThemePackageName() {
                return getString("icon_theme", DEFAULT_PACKAGE);
            }
            public static int getThemeColor() {
                return getInt("theme_color", -13388315);
            }
        }
    }

    public static class Application {

    }
}
