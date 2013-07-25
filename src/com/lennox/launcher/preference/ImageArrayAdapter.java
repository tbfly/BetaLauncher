/**
 * Copyright CMW Mobile.com, 2010. 
 */
package com.lennox.launcher.preference;

import android.app.Activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.lennox.launcher.R;
/**
 * The ImageArrayAdapter is the array adapter used for displaying an additional
 * image to a list preference item.
 * @author Casper Wakkers
 */
public class ImageArrayAdapter extends ArrayAdapter<CharSequence> {
	private int index = 0;
	private Drawable[] mEntryIcons = null;

	/**
	 * ImageArrayAdapter constructor.
	 * @param context the context.
	 * @param textViewResourceId resource id of the text view.
	 * @param objects to be displayed.
	 * @param ids resource id of the images to be displayed.
	 * @param i index of the previous selected item.
	 */
	public ImageArrayAdapter(Context context, int textViewResourceId,
			CharSequence[] objects, Drawable[] icons, int i) {
		super(context, textViewResourceId, objects);

		index = i;
		mEntryIcons = icons;
	}
	/**
	 * {@inheritDoc}
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
		View row = inflater.inflate(R.layout.preference_list_image, parent, false);

		ImageView imageView = (ImageView)row.findViewById(R.id.image);
		imageView.setImageDrawable(mEntryIcons[position]);

		CheckedTextView checkedTextView = (CheckedTextView)row.findViewById(
			R.id.check);

		checkedTextView.setText(getItem(position));

		if (position == index) {
			checkedTextView.setChecked(true);
		}

		return row;
	}
}
