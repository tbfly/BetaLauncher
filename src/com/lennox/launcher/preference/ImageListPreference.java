/**
 * Copyright CMW Mobile.com, 2010. 
 */
package com.lennox.launcher.preference;

import android.app.AlertDialog.Builder;

import android.content.Context;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.ListPreference;

import android.util.AttributeSet;

import android.widget.ListAdapter;

import com.lennox.launcher.R;
/**
 * The ImageListPreference class responsible for displaying an image for each
 * item within the list.
 * @author Casper Wakkers
 */
public class ImageListPreference extends ListPreference {
	private Drawable[] mEntryIcons = null;

	/**
	 * Constructor of the ImageListPreference. Initializes the custom images.
	 * @param context application context.
	 * @param attrs custom xml attributes.
	 */
	public ImageListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

        public void setEntryIcons(Drawable[] icons) {
            mEntryIcons = icons;
        }
	/**
	 * {@inheritDoc}
	 */
	protected void onPrepareDialogBuilder(Builder builder) {
		int index = findIndexOfValue(getSharedPreferences().getString(
			getKey(), "1"));

		ListAdapter listAdapter = new ImageArrayAdapter(getContext(),
			R.layout.preference_list_image, getEntries(), mEntryIcons, index);

		// Order matters.
		builder.setAdapter(listAdapter, this);
		super.onPrepareDialogBuilder(builder);
	}
}
