package com.eaglesakura.material.widget;

import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.R;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.ViewGroup;

/**
 * Material Design support Dialog
 */
public class MaterialDialogBase extends AppCompatDialog {

    protected View root;

    public MaterialDialogBase(Context context) {
        super(context);

        root = View.inflate(context, R.layout.esm_material_dialog, null);
        setContentView(root);
    }

    /**
     * コンテンツ本体を指定する
     */
    public void setDialogContent(int layout) {
        setDialogContent(View.inflate(getContext(), layout, null));
    }

    public View getPositiveButton() {
        return findViewById(R.id.EsMaterial_Dialog_BasicButtons_Positive);
    }

    public View getNegativeButton() {
        return findViewById(R.id.EsMaterial_Dialog_BasicButtons_Negative);
    }

    public View getNeutralButton() {
        return findViewById(R.id.EsMaterial_Dialog_BasicButtons_Neutral);
    }

    /**
     * コンテンツ本体を指定する
     */
    public void setDialogContent(View view) {
        AQuery q = new AQuery(root);
        ((ViewGroup) q.id(R.id.EsMaterial_Dialog_Content_Root).getView()).addView(view);
    }
}
