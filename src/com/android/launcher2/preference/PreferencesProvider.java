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

    private static Map<String, ?> sKeyValues;

    public static void load(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_KEY, 0);
        sKeyValues = preferences.getAll();
    }

    private static int getInt(String key, int def) {
        return sKeyValues.containsKey(key) && sKeyValues.get(key) instanceof Integer ?
                (Integer) sKeyValues.get(key) : def;
    }

    private static boolean getBoolean(String key, boolean def) {
        return sKeyValues.containsKey(key) && sKeyValues.get(key) instanceof Boolean ?
                (Boolean) sKeyValues.get(key) : def;
    }

    private static String getString(String key, String def) {
        return sKeyValues.containsKey(key) && sKeyValues.get(key) instanceof String ?
                (String) sKeyValues.get(key) : def;
    }

    public static class Interface {
        public static class Homescreen {
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
            public static boolean getShowSearchBar() {
                return getBoolean("ui_homescreen_general_search", true);
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
            }
            public static class FolderIconStyle {
                public static int getFolderIconStyle(Context context) {
                    return Integer.parseInt(getString("ui_homescreen_folder_style", "0"));
                }
            }
        }

        public static class Drawer {
            public static int getDrawerTransparency() {
                return Integer.parseInt(getString("ui_drawer_transparency", "128"));
            }
            public static String getHiddenApps() {
                return getString("ui_drawer_hidden_apps", "");
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
            }
        }

        public static class Dock {
            public static int getNumberIcons(int def) {
                return getInt("ui_dock_icons", def);
            }
            public static int getIconScale(int def) {
                //return getInt("ui_dock_icon_scale", def);
                return getInt("ui_general_icon_scale", def);
            }
            public static boolean getShowDivider() {
                return getBoolean("ui_dock_divider", true);
            }
        }

        public static class General {
            public static int getIconScale(int def) {
                return getInt("ui_general_icon_scale", def);
            }
        }
    }

    public static class Application {

    }
}
