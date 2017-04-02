/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.keyboardextensions;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.AttributeSet;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.R;

import java.util.Locale;

public class KeyboardExtensionFactory extends AddOnsFactory<KeyboardExtension> {

    private static final String XML_EXT_KEYBOARD_RES_ID_ATTRIBUTE = "extensionKeyboardResId";
    private static final String XML_EXT_KEYBOARD_TYPE_ATTRIBUTE = "extensionKeyboardType";

    @KeyboardExtension.KeyboardExtensionType
    private final int mExtensionType;

    public KeyboardExtensionFactory(@NonNull Context context, @StringRes int defaultAddOnId, @StringRes int enabledAddOnIdPrefId, int mExtensionType) {
        super(context, "ASK_EKF", "com.anysoftkeyboard.plugin.EXTENSION_KEYBOARD",
                "com.anysoftkeyboard.plugindata.extensionkeyboard",
                "ExtensionKeyboards", "ExtensionKeyboard",
                R.xml.extension_keyboards, defaultAddOnId, enabledAddOnIdPrefId, true);
        this.mExtensionType = mExtensionType;
    }

    @Override
    protected KeyboardExtension createConcreteAddOn(Context askContext, Context context, CharSequence prefId, CharSequence name, CharSequence description, boolean isHidden, int sortIndex, AttributeSet attrs) {
        int keyboardResId = attrs.getAttributeResourceValue(null, XML_EXT_KEYBOARD_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
        if (keyboardResId == AddOn.INVALID_RES_ID)
            keyboardResId = attrs.getAttributeIntValue(null, XML_EXT_KEYBOARD_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
        @KeyboardExtension.KeyboardExtensionType
        int extensionType = attrs.getAttributeResourceValue(null, XML_EXT_KEYBOARD_TYPE_ATTRIBUTE, AddOn.INVALID_RES_ID);
        //noinspection WrongConstant
        if (extensionType != AddOn.INVALID_RES_ID) {
            extensionType = KeyboardExtension.ensureValidType(context.getResources().getInteger(extensionType));
        } else {
            //noinspection WrongConstant
            extensionType = attrs.getAttributeIntValue(null, XML_EXT_KEYBOARD_TYPE_ATTRIBUTE, AddOn.INVALID_RES_ID);
        }
        Logger.d(mTag, "Parsing Extension Keyboard! prefId %s, keyboardResId %d, type %d", prefId, keyboardResId, extensionType);

        //noinspection WrongConstant
        if (extensionType == AddOn.INVALID_RES_ID) {
            throw new RuntimeException(String.format(Locale.US, "Missing details for creating Extension Keyboard! prefId %s\nkeyboardResId: %d, type: %d", prefId, keyboardResId, extensionType));
        } else {
            if (extensionType == mExtensionType) {
                return new KeyboardExtension(askContext, context, prefId, name, keyboardResId, extensionType, description, isHidden, sortIndex);
            } else {
                return null;
            }
        }
    }

    @Override
    protected boolean isEventRequiresViewReset(Intent eventIntent) {
        KeyboardExtension selectedExtension = getEnabledAddOn();
        if (selectedExtension != null && selectedExtension.getPackageContext().getPackageName().equals(eventIntent.getData().getSchemeSpecificPart())) {
            Logger.d(mTag, "It seems that selected keyboard extension has been changed. I need to reload view!");
            return true;
        } else {
            return false;
        }
    }

    @KeyboardExtension.KeyboardExtensionType
    public int getExtensionType() {
        return mExtensionType;
    }
}
