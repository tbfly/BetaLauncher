package com.android.launcher2.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.launcher.R;

/**
 * @author nebkat
 */
public class DoubleSeekBarDialogPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
    private int mMax1, mMin1, mDefault1, mMax2, mMin2, mDefault2;

    private String mPrefix1, mSuffix1, mPrefix2, mSuffix2;

    private TextView mValueText1, mValueText2;
    private SeekBar mSeekBar1, mSeekBar2;

    public DoubleSeekBarDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray doubleSeekBarType = context.obtainStyledAttributes(attrs,
                R.styleable.DoubleSeekBarDialogPreference, 0, 0);

        mMax1 = doubleSeekBarType.getInt(R.styleable.DoubleSeekBarDialogPreference_max1, 100);
        mMin1 = doubleSeekBarType.getInt(R.styleable.DoubleSeekBarDialogPreference_min1, 0);
        mMax2 = doubleSeekBarType.getInt(R.styleable.DoubleSeekBarDialogPreference_max2, 100);
        mMin2 = doubleSeekBarType.getInt(R.styleable.DoubleSeekBarDialogPreference_min2, 0);

        mDefault1 = doubleSeekBarType.getInt(R.styleable.DoubleSeekBarDialogPreference_defaultValue1, mMin1);
        mDefault2 = doubleSeekBarType.getInt(R.styleable.DoubleSeekBarDialogPreference_defaultValue2, mMin2);

        mPrefix1 = doubleSeekBarType.getString(R.styleable.DoubleSeekBarDialogPreference_prefix1);
        mPrefix2 = doubleSeekBarType.getString(R.styleable.DoubleSeekBarDialogPreference_prefix2);
        mSuffix1 = doubleSeekBarType.getString(R.styleable.DoubleSeekBarDialogPreference_suffix1);
        mSuffix2 = doubleSeekBarType.getString(R.styleable.DoubleSeekBarDialogPreference_suffix2);
        if (mPrefix1 == null) {
            mPrefix1 = "";
        }
        if (mPrefix2 == null) {
            mPrefix2 = "";
        }
        if (mSuffix1 == null) {
            mSuffix1 = "%";
        }
        if (mSuffix2 == null) {
            mSuffix2 = "%";
        }

        doubleSeekBarType.recycle();
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.double_seekbar_dialog, null);

        mValueText1 = (TextView) view.findViewById(R.id.value_1);
        mValueText2 = (TextView) view.findViewById(R.id.value_2);

        mSeekBar1 = (SeekBar) view.findViewById(R.id.seekbar_1);
        mSeekBar1.setOnSeekBarChangeListener(this);
        mSeekBar1.setMax(mMax1 - mMin1);
        mSeekBar1.setProgress(getPersistedValue(1) - mMin1);

        mSeekBar2 = (SeekBar) view.findViewById(R.id.seekbar_2);
        mSeekBar2.setOnSeekBarChangeListener(this);
        mSeekBar2.setMax(mMax2 - mMin2);
        mSeekBar2.setProgress(getPersistedValue(2) - mMin2);

        mValueText1.setText(mPrefix1 + getPersistedValue(1) + mSuffix1);
        mValueText2.setText(mPrefix2 + getPersistedValue(2) + mSuffix2);

        return view;
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == mSeekBar1) {
            mValueText1.setText(mPrefix1 + (progress + mMin1) + mSuffix1);
        } else {
            mValueText2.setText(mPrefix2 + (progress + mMin2) + mSuffix2);
        }
    }

    private int getPersistedValue(int value) {
        String[] values = getPersistedString(mDefault1 + "|" + mDefault2).split("\\|");
        if (value == 1) {
            try {
                return Integer.parseInt(values[0]);
            } catch (NumberFormatException e) {
                return mDefault1;
            }
        } else {
            try {
                return Integer.parseInt(values[1]);
            } catch (NumberFormatException e) {
                return mDefault2;
            }
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {}
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            persistString((mSeekBar1.getProgress() + mMin1) + "|" + (mSeekBar2.getProgress() + mMin2));
        }
    }

}
