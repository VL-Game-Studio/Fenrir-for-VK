/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.preference;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Checkable;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.content.res.TypedArrayUtils;

import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * A {@link Preference} that provides a two-state toggleable option.
 *
 * <p>This preference will save a boolean value to {@link android.content.SharedPreferences}.
 *
 * @attr name android:summaryOff
 * @attr name android:summaryOn
 * @attr name android:switchTextOff
 * @attr name android:switchTextOn
 * @attr name android:disableDependentsState
 */
public class SwitchPreference extends TwoStatePreference {
    private final Listener mListener = new Listener();

    // Switch text for on and off states
    private CharSequence mSwitchOn;
    private CharSequence mSwitchOff;

    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context      The {@link Context} that will style this preference
     * @param attrs        Style attributes that differ from the default
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style
     *                     resource that supplies default values for the view. Can be 0 to not
     *                     look for defaults.
     * @param defStyleRes  A resource identifier of a style resource that supplies default values
     *                     for the view, used only if defStyleAttr is 0 or can not be found in the
     *                     theme. Can be 0 to not look for defaults.
     */
    public SwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SwitchPreference, defStyleAttr, defStyleRes);

        setSummaryOn(TypedArrayUtils.getString(a, R.styleable.SwitchPreference_summaryOn,
                R.styleable.SwitchPreference_android_summaryOn));

        setSummaryOff(TypedArrayUtils.getString(a, R.styleable.SwitchPreference_summaryOff,
                R.styleable.SwitchPreference_android_summaryOff));

        setSwitchTextOn(TypedArrayUtils.getString(a,
                R.styleable.SwitchPreference_switchTextOn,
                R.styleable.SwitchPreference_android_switchTextOn));

        setSwitchTextOff(TypedArrayUtils.getString(a,
                R.styleable.SwitchPreference_switchTextOff,
                R.styleable.SwitchPreference_android_switchTextOff));

        setDisableDependentsState(TypedArrayUtils.getBoolean(a,
                R.styleable.SwitchPreference_disableDependentsState,
                R.styleable.SwitchPreference_android_disableDependentsState, false));

        a.recycle();
    }

    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context      The {@link Context} that will style this preference
     * @param attrs        Style attributes that differ from the default
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style
     *                     resource that supplies default values for the view. Can be 0 to not
     *                     look for defaults.
     */
    public SwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /**
     * Construct a new SwitchPreference with the given style options.
     *
     * @param context The {@link Context} that will style this preference
     * @param attrs   Style attributes that differ from the default
     */
    public SwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context,
                androidx.preference.R.attr.switchPreferenceStyle,
                android.R.attr.switchPreferenceStyle));
    }

    /**
     * Construct a new SwitchPreference with default style options.
     *
     * @param context The {@link Context} that will style this preference
     */
    public SwitchPreference(@NonNull Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View switchView = holder.findViewById(AndroidResources.ANDROID_R_SWITCH_WIDGET);
        syncSwitchView(switchView);
        syncSummaryView(holder);
    }

    /**
     * @return The text that will be displayed on the switch widget in the on state
     */
    @Nullable
    public CharSequence getSwitchTextOn() {
        return mSwitchOn;
    }

    /**
     * Set the text displayed on the switch widget in the on state.
     * This should be a very short string, one word if possible.
     *
     * @param onText Text to display in the on state
     */
    public void setSwitchTextOn(@Nullable CharSequence onText) {
        mSwitchOn = onText;
        notifyChanged();
    }

    /**
     * Set the text displayed on the switch widget in the on state.
     * This should be a very short string, one word if possible.
     *
     * @param resId The text as a string resource ID
     */
    public void setSwitchTextOn(int resId) {
        setSwitchTextOn(getContext().getString(resId));
    }

    /**
     * @return The text that will be displayed on the switch widget in the off state
     */
    @Nullable
    public CharSequence getSwitchTextOff() {
        return mSwitchOff;
    }

    /**
     * Set the text displayed on the switch widget in the off state.
     * This should be a very short string, one word if possible.
     *
     * @param offText Text to display in the off state
     */
    public void setSwitchTextOff(@Nullable CharSequence offText) {
        mSwitchOff = offText;
        notifyChanged();
    }

    /**
     * Set the text displayed on the switch widget in the off state.
     * This should be a very short string, one word if possible.
     *
     * @param resId The text as a string resource ID
     */
    public void setSwitchTextOff(int resId) {
        setSwitchTextOff(getContext().getString(resId));
    }

    /**
     * @param view
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    protected void performClick(@NonNull View view) {
        super.performClick(view);
        syncViewIfAccessibilityEnabled(view);
    }

    private void syncViewIfAccessibilityEnabled(View view) {
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!accessibilityManager.isEnabled()) {
            return;
        }

        View switchView = view.findViewById(AndroidResources.ANDROID_R_SWITCH_WIDGET);
        syncSwitchView(switchView);

        View summaryView = view.findViewById(android.R.id.summary);
        syncSummaryView(summaryView);
    }

    private void syncSwitchView(View view) {
        if (view instanceof SwitchMaterial) {
            SwitchMaterial switchView = (SwitchMaterial) view;
            switchView.setOnCheckedChangeListener(null);
        }
        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(mChecked);
        }
        if (view instanceof SwitchMaterial) {
            SwitchMaterial switchView = (SwitchMaterial) view;
            switchView.setTextOn(mSwitchOn);
            switchView.setTextOff(mSwitchOff);
            switchView.setOnCheckedChangeListener(mListener);
        }
    }

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        Listener() {
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.setChecked(!isChecked);
                return;
            }

            setChecked(isChecked);
        }
    }
}
