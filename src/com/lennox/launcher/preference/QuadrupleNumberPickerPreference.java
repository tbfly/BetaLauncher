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
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.NumberPicker;
import com.lennox.launcher.R;

/*
 * @author Danesh
 * @author nebkat
 */

public class QuadrupleNumberPickerPreference extends DialogPreference {
    private int mMin1, mMax1, mDefault1;
    private int mMin2, mMax2, mDefault2;
    private int mMin3, mMax3, mDefault3;
    private int mMin4, mMax4, mDefault4;

    private String mMaxExternalKey1, mMinExternalKey1;
    private String mMaxExternalKey2, mMinExternalKey2;
    private String mMaxExternalKey3, mMinExternalKey3;
    private String mMaxExternalKey4, mMinExternalKey4;

    private String mPickerTitle1, mPickerTitle2;
    private String mPickerRowTitle12, mPickerRowTitle34;

    private NumberPicker mNumberPicker1, mNumberPicker2, mNumberPicker3, mNumberPicker4;

    public QuadrupleNumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray QuadrupleNumberPickerType = context.obtainStyledAttributes(attrs,
                R.styleable.QuadrupleNumberPickerPreference, 0, 0);

        mMaxExternalKey1 = QuadrupleNumberPickerType.getString(R.styleable.QuadrupleNumberPickerPreference_maxExternal1);
        mMinExternalKey1 = QuadrupleNumberPickerType.getString(R.styleable.QuadrupleNumberPickerPreference_minExternal1);
        mMaxExternalKey2 = QuadrupleNumberPickerType.getString(R.styleable.QuadrupleNumberPickerPreference_maxExternal2);
        mMinExternalKey2 = QuadrupleNumberPickerType.getString(R.styleable.QuadrupleNumberPickerPreference_minExternal2);
        mMaxExternalKey3 = QuadrupleNumberPickerType.getString(R.styleable.QuadrupleNumberPickerPreference_maxExternal3);
        mMinExternalKey3 = QuadrupleNumberPickerType.getString(R.styleable.QuadrupleNumberPickerPreference_minExternal3);
        mMaxExternalKey4 = QuadrupleNumberPickerType.getString(R.styleable.QuadrupleNumberPickerPreference_maxExternal4);
        mMinExternalKey4 = QuadrupleNumberPickerType.getString(R.styleable.QuadrupleNumberPickerPreference_minExternal4);

        mPickerTitle1 = QuadrupleNumberPickerType.getString(R.styleable.QuadrupleNumberPickerPreference_pickerTitle1);
        mPickerTitle2 = QuadrupleNumberPickerType.getString(R.styleable.QuadrupleNumberPickerPreference_pickerTitle2);

        mPickerRowTitle12 = QuadrupleNumberPickerType.getString(R.styleable.QuadrupleNumberPickerPreference_pickerRowTitle12);
        mPickerRowTitle34 = QuadrupleNumberPickerType.getString(R.styleable.QuadrupleNumberPickerPreference_pickerRowTitle34);

        mMax1 = QuadrupleNumberPickerType.getInt(R.styleable.QuadrupleNumberPickerPreference_max1, 5);
        mMin1 = QuadrupleNumberPickerType.getInt(R.styleable.QuadrupleNumberPickerPreference_min1, 0);
        mMax2 = QuadrupleNumberPickerType.getInt(R.styleable.QuadrupleNumberPickerPreference_max2, 5);
        mMin2 = QuadrupleNumberPickerType.getInt(R.styleable.QuadrupleNumberPickerPreference_min2, 0);
        mMax3 = QuadrupleNumberPickerType.getInt(R.styleable.QuadrupleNumberPickerPreference_max3, 5);
        mMin3 = QuadrupleNumberPickerType.getInt(R.styleable.QuadrupleNumberPickerPreference_min3, 0);
        mMax4 = QuadrupleNumberPickerType.getInt(R.styleable.QuadrupleNumberPickerPreference_max4, 5);
        mMin4 = QuadrupleNumberPickerType.getInt(R.styleable.QuadrupleNumberPickerPreference_min4, 0);

        mDefault1 = QuadrupleNumberPickerType.getInt(R.styleable.QuadrupleNumberPickerPreference_defaultValue1, mMin1);
        mDefault2 = QuadrupleNumberPickerType.getInt(R.styleable.QuadrupleNumberPickerPreference_defaultValue2, mMin2);
        mDefault3 = QuadrupleNumberPickerType.getInt(R.styleable.QuadrupleNumberPickerPreference_defaultValue3, mMin3);
        mDefault4 = QuadrupleNumberPickerType.getInt(R.styleable.QuadrupleNumberPickerPreference_defaultValue4, mMin4);

        QuadrupleNumberPickerType.recycle();
    }

    @Override
    protected View onCreateDialogView() {
        int max1 = mMax1;
        int min1 = mMin1;
        int max2 = mMax2;
        int min2 = mMin2;
        int max3 = mMax3;
        int min3 = mMin3;
        int max4 = mMax4;
        int min4 = mMin4;

        // External values
        if (mMaxExternalKey1 != null) {
            max1 = getSharedPreferences().getInt(mMaxExternalKey1, mMax1);
        }
        if (mMinExternalKey1 != null) {
            min1 = getSharedPreferences().getInt(mMinExternalKey1, mMin1);
        }
        if (mMaxExternalKey2 != null) {
            max2 = getSharedPreferences().getInt(mMaxExternalKey2, mMax2);
        }
        if (mMinExternalKey2 != null) {
            min2 = getSharedPreferences().getInt(mMinExternalKey2, mMin2);
        }
        if (mMaxExternalKey3 != null) {
            max3 = getSharedPreferences().getInt(mMaxExternalKey3, mMax3);
        }
        if (mMinExternalKey3 != null) {
            min3 = getSharedPreferences().getInt(mMinExternalKey3, mMin3);
        }
        if (mMaxExternalKey4 != null) {
            max4 = getSharedPreferences().getInt(mMaxExternalKey4, mMax4);
        }
        if (mMinExternalKey4 != null) {
            min4 = getSharedPreferences().getInt(mMinExternalKey4, mMin4);
        }

        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.quadruple_number_picker_dialog, null);

        mNumberPicker1 = (NumberPicker) view.findViewById(R.id.number_picker_1);
        mNumberPicker2 = (NumberPicker) view.findViewById(R.id.number_picker_2);
        mNumberPicker3 = (NumberPicker) view.findViewById(R.id.number_picker_3);
        mNumberPicker4 = (NumberPicker) view.findViewById(R.id.number_picker_4);

        if (mNumberPicker1 == null || mNumberPicker2 == null
             || mNumberPicker3 == null || mNumberPicker4 == null) {
            throw new RuntimeException("mNumberPicker1,2,3 or 4 is null!");
        }

        // Initialize state
        mNumberPicker1.setMaxValue(max1);
        mNumberPicker1.setMinValue(min1);
        mNumberPicker1.setValue(getPersistedValue(1));
        mNumberPicker1.setWrapSelectorWheel(false);
        mNumberPicker2.setMaxValue(max2);
        mNumberPicker2.setMinValue(min2);
        mNumberPicker2.setValue(getPersistedValue(2));
        mNumberPicker2.setWrapSelectorWheel(false);
        mNumberPicker3.setMaxValue(max3);
        mNumberPicker3.setMinValue(min3);
        mNumberPicker3.setValue(getPersistedValue(3));
        mNumberPicker3.setWrapSelectorWheel(false);
        mNumberPicker4.setMaxValue(max4);
        mNumberPicker4.setMinValue(min4);
        mNumberPicker4.setValue(getPersistedValue(4));
        mNumberPicker4.setWrapSelectorWheel(false);

        // Titles
        TextView pickerTitle1 = (TextView) view.findViewById(R.id.picker_title_1);
        TextView pickerTitle2 = (TextView) view.findViewById(R.id.picker_title_2);

        if (pickerTitle1 != null && pickerTitle2 != null) {
            pickerTitle1.setText(mPickerTitle1);
            pickerTitle2.setText(mPickerTitle2);
        }

        TextView pickerRowTitle12 = (TextView) view.findViewById(R.id.picker_row_title_12);
        TextView pickerRowTitle34 = (TextView) view.findViewById(R.id.picker_row_title_34);

        if (pickerRowTitle12 != null && pickerRowTitle34 != null) {
            if (mPickerRowTitle12.equals("") && mPickerRowTitle34.equals("")) {
                pickerRowTitle12.setVisibility(View.GONE);
                pickerRowTitle34.setVisibility(View.GONE);
            } else {
                pickerRowTitle12.setText(mPickerRowTitle12);
                pickerRowTitle34.setText(mPickerRowTitle34);
            }
        }

        // No keyboard popup
        mNumberPicker1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mNumberPicker2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mNumberPicker3.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mNumberPicker4.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        return view;
    }

    private int getPersistedValue(int value) {
        String[] values = getPersistedString(mDefault1 + "|"
                           + mDefault2 + "|"+ mDefault3 + "|"+ mDefault4).split("\\|");
        if (value == 1) {
            try {
                return Integer.parseInt(values[0]);
            } catch (NumberFormatException e) {
                return mDefault1;
            }
        } else if (value == 2) {
            try {
                return Integer.parseInt(values[1]);
            } catch (NumberFormatException e) {
                return mDefault2;
            }
        } else if (value == 3) {
            try {
                return Integer.parseInt(values[2]);
            } catch (NumberFormatException e) {
                return mDefault3;
            }
        } else {
            try {
                return Integer.parseInt(values[3]);
            } catch (NumberFormatException e) {
                return mDefault4;
            }
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        final int value1 = mNumberPicker1.getValue();
        final int value2 = mNumberPicker2.getValue();
        final int value3 = mNumberPicker3.getValue();
        final int value4 = mNumberPicker4.getValue();

        if (positiveResult) {
            if (callChangeListener(value1 + "|" + value2 + "|" + value3 + "|" + value4)) {
                saveValue(value1 + "|" + value2 + "|" + value3 + "|" + value4);
            }
        }
    }

    public void setMin1(int min) {
        mMin1 = min;
    }
    public void setMax1(int max) {
        mMax1 = max;
    }
    public void setMin2(int min) {
        mMin2 = min;
    }
    public void setMax2(int max) {
        mMax2 = max;
    }
    public void setMin3(int min) {
        mMin3 = min;
    }
    public void setMax3(int max) {
        mMax3 = max;
    }
    public void setMin4(int min) {
        mMin4 = min;
    }
    public void setMax4(int max) {
        mMax4 = max;
    }
    public void setDefault1(int def) {
        mDefault1 = def;
    }
    public void setDefault2(int def) {
        mDefault2 = def;
    }
    public void setDefault3(int def) {
        mDefault3 = def;
    }
    public void setDefault4(int def) {
        mDefault4 = def;
    }

    public int getMin1() {
        return mMin1;
    }
    public int getMax1() {
        return mMax1;
    }
    public int getMin2() {
        return mMin2;
    }
    public int getMax2() {
        return mMax2;
    }
    public int getMin3() {
        return mMin3;
    }
    public int getMax3() {
        return mMax3;
    }
    public int getMin4() {
        return mMin4;
    }
    public int getMax4() {
        return mMax4;
    }
    public int getDefault1() {
        return mDefault1;
    }
    public int getDefault2() {
        return mDefault2;
    }
    public int getDefault3() {
        return mDefault3;
    }
    public int getDefault4() {
        return mDefault4;
    }

    private boolean saveValue(String newValue) {
        return persistString(newValue);
    }

}
