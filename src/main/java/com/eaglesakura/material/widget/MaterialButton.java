package com.eaglesakura.material.widget;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.util.ResourceUtil;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.Gravity;


/**
 *
 */
public class MaterialButton extends AppCompatButton {
    public static final int TEXTCOLOR_MODE_LIGHT = 0;
    public static final int TEXTCOLOR_MODE_DARK = 1;
    public static final int TEXTCOLOR_MODE_PALETTE = 2;
    public static final int TEXTCOLOR_MODE_AUTO = 3;
    public static final int TEXTCOLOR_MODE_IGNORE = 4;

    private int styleBaseColor;

    public MaterialButton(Context context) {
        super(context);
        initMaterialButton(context, null, 0);
    }

    public MaterialButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMaterialButton(context, attrs, 0);
    }

    public MaterialButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initMaterialButton(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("ResourceType")
    private void initMaterialButton(Context context, AttributeSet attrs, int defStyleAttr) {
        if (isInEditMode()) {
            return;
        }

        if (attrs != null) {
            Resources res = context.getResources();
            TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[]{
                    R.attr.buttonBaseColor,
                    R.attr.buttonHighlightWeight,
                    R.attr.buttonTextColorMode,
            });
            int baseColor = typedArray.getColor(0, ResourcesCompat.getColor(res, R.color.EsMaterial_Grey_500, context.getTheme()));
            this.styleBaseColor = baseColor;
            {
                final float weight = typedArray.getFloat(1, 0.9f);
                // Kitkatは独自に色合いを変えてあげる必要がある
                int a = (int) (weight * Color.alpha(baseColor));
                int r = (int) (weight * Color.red(baseColor));
                int g = (int) (weight * Color.green(baseColor));
                int b = (int) (weight * Color.blue(baseColor));

                setSupportBackgroundTintList(createButtonColorStateList(context, baseColor, Color.argb(a, r, g, b)));
            }

            // base colorから各種TextColorを生成する
            {
                int textBaseColor = 0;
                int textHighlightColor = 0;
                int colorMode = typedArray.getInt(2, TEXTCOLOR_MODE_AUTO);

                if (colorMode == TEXTCOLOR_MODE_AUTO || colorMode == TEXTCOLOR_MODE_PALETTE) {
                    // Paletteから色を指定する
                    Palette.Swatch swatch = new Palette.Swatch(baseColor, 100);
                    textBaseColor = swatch.getTitleTextColor();
                    textHighlightColor = swatch.getBodyTextColor();

                    // 自動選別の場合、HSVからLight / Darkを選定する
                    if (colorMode == TEXTCOLOR_MODE_AUTO) {
                        float[] hsv = new float[3];
                        Color.RGBToHSV(Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor), hsv);
                        if (hsv[2] > 0.5f) {
                            // 明るい色なので、フォントを暗くする
                            colorMode = TEXTCOLOR_MODE_DARK;
                        } else {
                            // 暗い色なので、フォントを明るくする
                            colorMode = TEXTCOLOR_MODE_LIGHT;
                        }
                    }
                }

                // TextColor
                // ignoreの場合はsetTextColorを行わない
                if (colorMode != TEXTCOLOR_MODE_IGNORE) {
                    if (colorMode == TEXTCOLOR_MODE_LIGHT) {
                        // フォントを明るくする
                        textBaseColor = ResourceUtil.argb(context, R.color.EsMaterial_Grey_50);
                        textHighlightColor = ResourceUtil.argb(context, R.color.EsMaterial_Grey_100);
                    } else if (colorMode == TEXTCOLOR_MODE_DARK) {
                        // フォントを暗くする
                        textBaseColor = ResourceUtil.argb(context, R.color.EsMaterial_Grey_800);
                        textHighlightColor = ResourceUtil.argb(context, R.color.EsMaterial_Grey_900);
                    }

                    setTextColor(createButtonColorStateList(context, textBaseColor, textHighlightColor));
                }
            }
        }
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        if (isInEditMode()) {
//            Graphics g = new Graphics(canvas);
//            g.setColorARGB(0xFF00FFFF);
//            g.fillRoundRect(0, 0, getWidth(), getHeight(), (float) Math.min(getWidth(), getHeight()) / 10.0f);
//            g.setColorARGB(0xFFFF0000);
//            g.drawRoundRect(0, 0, getWidth(), getHeight(), (float) Math.min(getWidth(), getHeight()) / 10.0f);
//        }
//        super.onDraw(canvas);
//    }

    static final int[] DISABLED_STATE_SET = new int[]{-android.R.attr.state_enabled};
    static final int[] FOCUSED_STATE_SET = new int[]{android.R.attr.state_focused};
    static final int[] PRESSED_STATE_SET = new int[]{android.R.attr.state_pressed};

    private ColorStateList createButtonColorStateList(Context context, int colorButtonNormal, int colorControlHighlight) {
        final int[][] states = new int[4][];
        final int[] colors = new int[4];
        int i = 0;

        // Disabled state
        states[i] = DISABLED_STATE_SET;
        // TODO disable colorを直す
//        colors[i] = ThemeUtils.getDisabledThemeAttrColor(context, R.attr.colorButtonNormal);
        colors[i] = ColorUtils.compositeColors(colorControlHighlight, colorButtonNormal);
        i++;

        states[i] = PRESSED_STATE_SET;
        colors[i] = ColorUtils.compositeColors(colorControlHighlight, colorButtonNormal);
        i++;

        states[i] = FOCUSED_STATE_SET;
        colors[i] = ColorUtils.compositeColors(colorControlHighlight, colorButtonNormal);
        i++;

        // Default enabled state
        states[i] = EMPTY_STATE_SET;
        colors[i] = colorButtonNormal;
        i++;

        return new ColorStateList(states, colors);
    }
}
