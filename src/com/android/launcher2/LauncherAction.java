package com.android.launcher2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.launcher.R;

public class LauncherAction {

    public enum Action {
        AllApps,
        RecentApps,
        Search,
        VoiceSearch,
        ExpandNotifications,
        ToggleFullscreen,
        DefaultScreen,
        ShowPreview,
        LockUnlock,
        ToggleDock,
        QuickSettings,
        LauncherSettings;
        int getString() {
            return getString(this);
        }
        int getDrawable() {
            return getDrawable(this);
        }
        static int getString(Action action) {
            switch (action) {
                case AllApps:
                    return R.string.all_apps_button_label;
                case RecentApps:
                    return R.string.action_recent_apps;
                case Search:
                    return R.string.action_search;
                case VoiceSearch:
                    return R.string.action_voice_search;
                case ExpandNotifications:
                    return R.string.action_expand_notifications;
                case ToggleFullscreen:
                    return R.string.action_toggle_fullscreen;
                case DefaultScreen:
                    return R.string.action_default_screen;
                case ShowPreview:
                    return R.string.action_show_preview;
                case LockUnlock:
                    return R.string.action_lock_unlock;
                case ToggleDock:
                    return R.string.action_toggle_dock;
                case QuickSettings:
                    return R.string.action_quick_settings;
                case LauncherSettings:
                    return R.string.action_launcher_settings;
                default:
                    return -1;
            }
        }
        static int getDrawable(Action action) {
            switch (action) {
                case AllApps:
                    return R.drawable.ic_allapps;
                case RecentApps:
                    return R.drawable.action_recent_apps;
                case Search:
                    return R.drawable.action_search;
                case VoiceSearch:
                    return R.drawable.action_voice_search;
                case ExpandNotifications:
                    return R.drawable.action_expand_notifications;
                case ToggleFullscreen:
                    return R.drawable.action_toggle_fullscreen;
                case DefaultScreen:
                    return R.drawable.action_default_screen;
                case ShowPreview:
                    return R.drawable.action_show_preview;
                case LockUnlock:
                    return R.drawable.action_lock_unlock;
                case ToggleDock:
                    return R.drawable.action_toggle_dock;
                case QuickSettings:
                    return R.drawable.action_quick_settings;
                case LauncherSettings:
                    return R.drawable.action_launcher_settings;
                default:
                    return -1;
            }
        }
    }

    public static List<Action> getAllActions() {
        final List<Action> items = Arrays.asList(Action.values());
        List<Action> returnItems = new ArrayList<Action>();
        for (Action item : items) {
            if (!(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 && item == LauncherAction.Action.QuickSettings)) {
                returnItems.add(item);
            }
        }
        return returnItems;
    }

    public static class AddAdapter extends BaseAdapter {

        public class ItemInfo {
            public Action action;
            public Drawable drawable;
            public String title;
            public ItemInfo(Action info, Resources res) {
                action = info;
                drawable = res.getDrawable(info.getDrawable());
                title = res.getString(info.getString());
            }
        }

        private final LayoutInflater mInflater;

        private final List<ItemInfo> mItems = new ArrayList<ItemInfo>();

        public AddAdapter(Launcher launcher) {
            super();

            mInflater = (LayoutInflater) launcher.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Create default actions
            Resources res = launcher.getResources();

            List<Action> items = LauncherAction.getAllActions();
            for (Action item : items) {
                mItems.add(new ItemInfo(item, res));
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ItemInfo item = (ItemInfo) getItem(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.add_list_item, parent, false);
            }

            TextView textView = (TextView) convertView;
            textView.setTag(item);
            textView.setText(item.title);
            textView.setCompoundDrawablesWithIntrinsicBounds(item.drawable, null, null, null);

            return convertView;
        }

        public int getCount() {
            return mItems.size();
        }

        public Object getItem(int position) {
            return mItems.get(position);
        }

        public long getItemId(int position) {
            return position;
        }
    }
}