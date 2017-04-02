package com.anysoftkeyboard.addons;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.AttributeSet;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AddOnsFactoryTest {

    private static final int STABLE_THEMES_COUNT = 10;

    @Test
    public void testGetAllAddOns() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(true);
        List<TestAddOn> list = factory.getAllAddOns();
        Assert.assertTrue(list.size() > 0);

        HashSet<CharSequence> seenIds = new HashSet<>();
        for (AddOn addOn : list) {
            Assert.assertNotNull(addOn);
            Assert.assertFalse(seenIds.contains(addOn.getId()));
            seenIds.add(addOn.getId());
        }
    }

    @Test
    public void testFiltersDebugAddOnOnReleaseBuilds() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(false);
        List<TestAddOn> list = factory.getAllAddOns();
        Assert.assertEquals(STABLE_THEMES_COUNT, list.size());
    }

    @Test
    public void testDoesNotFiltersDebugAddOnOnDebugBuilds() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(true);
        List<TestAddOn> list = factory.getAllAddOns();
        //right now, we have 3 themes that are marked as dev.
        Assert.assertEquals(STABLE_THEMES_COUNT + 3, list.size());
    }

    @Test
    public void testHiddenAddOnsAreNotReturned() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(false);
        List<TestAddOn> list = factory.getAllAddOns();
        final String hiddenThemeId = "2a94cf8c-266c-47fd-8c8c-c9c57d28d7dc";
        Assert.assertEquals(hiddenThemeId, RuntimeEnvironment.application.getString(R.string.fallback_keyboard_theme_id));
        //ensuring we can get this hidden theme by calling it specifically
        final AddOn hiddenAddOn = factory.getAddOnById(hiddenThemeId);
        Assert.assertNotNull(hiddenAddOn);
        Assert.assertEquals(hiddenThemeId, hiddenAddOn.getId());
        //ensuring the hidden theme is not in the list of all themes
        for (TestAddOn addOn : list) {
            Assert.assertNotEquals(hiddenThemeId, addOn.getId());
            Assert.assertNotSame(hiddenAddOn, addOn);
            Assert.assertNotEquals(hiddenAddOn.getId(), addOn.getId());
        }

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetAllAddOnsReturnsUnmodifiableList() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(true);
        List<TestAddOn> list = factory.getAllAddOns();

        list.remove(0);
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsExceptionWhenGettingEnabledAddOnAndNoPrefIdProvided() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(0, 0, true);
        factory.getOrderedEnabledAddOns();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsExceptionWhenSettingEnabledAddOnAndNoPrefIdProvided() throws Exception {
        TestableAddOnsFactory factory = new TestableAddOnsFactory(0, 0, true);
        factory.setOrderedEnabledIds(Collections.<CharSequence>singleton("test"));
    }

    private static class TestAddOn extends AddOnImpl {
        TestAddOn(Context askContext, Context packageContext, CharSequence id, CharSequence name, CharSequence description, boolean isHidden, int sortIndex) {
            super(askContext, packageContext, id, name, description, isHidden, sortIndex);
        }
    }

    private static class TestableAddOnsFactory extends AddOnsFactory<TestAddOn> {

        private TestableAddOnsFactory(boolean isDevBuild) {
            this(R.string.settings_default_keyboard_theme_key, R.string.settings_key_keyboard_theme_key, isDevBuild);
        }

        private TestableAddOnsFactory(@StringRes int defaultAddOnId, @StringRes int prefId, boolean isDevBuild) {
            super(RuntimeEnvironment.application, "ASK_KT", "com.anysoftkeyboard.plugin.KEYBOARD_THEME", "com.anysoftkeyboard.plugindata.keyboardtheme",
                    "KeyboardThemes", "KeyboardTheme",
                    R.xml.keyboard_themes, defaultAddOnId, prefId, true, isDevBuild);
        }

        @Override
        protected TestAddOn createConcreteAddOn(Context askContext, Context context, CharSequence prefId, CharSequence name, CharSequence description, boolean isHidden, int sortIndex, AttributeSet attrs) {
            return new TestAddOn(askContext, context, prefId, name, description, isHidden, sortIndex);
        }
    }
}