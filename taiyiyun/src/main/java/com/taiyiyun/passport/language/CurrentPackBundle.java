package com.taiyiyun.passport.language;

/**
 * Created by nina on 2017/12/5.
 */
public class CurrentPackBundle {
    private static final ThreadLocal<PackBundle> local = new ThreadLocal<>();

    public static void set(PackBundle bundle) {
        local.set(bundle);
    }

    public static final PackBundle get() {
        return local.get();
    }

    public static final void remove() {
        local.remove();
    }

}
