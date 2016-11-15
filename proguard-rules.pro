## Genericsは必須
-keepattributes Signature

## enum系はJSONパース等に頻繁に使われるため、保護する
-keep enum * { *; }

## AppCompat系は内部情報を保護する
-keepclassmembers class android.support.** { *; }

## otto対策
-keep class com.squareup.otto.Subscribe { *; }
-keep class com.squareup.otto.Produce { *; }

## Annotation付きメソッドの保護
-keepclassmembers class * {
    @com.eaglesakura.android.framework.ui.support.annotation.BindInterface *;
    @com.squareup.otto.Subscribe *;
    @com.squareup.otto.Produce *;
}