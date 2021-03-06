package com.google.p021a.p023b;

import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class C0588j {

    static class C05924 extends C0588j {
        C05924() {
        }

        public <T> T mo836a(Class<T> cls) {
            throw new UnsupportedOperationException("Cannot allocate " + cls);
        }
    }

    public static C0588j m1181a() {
        final Method method;
        try {
            Class cls = Class.forName("sun.misc.Unsafe");
            Field declaredField = cls.getDeclaredField("theUnsafe");
            declaredField.setAccessible(true);
            final Object obj = declaredField.get(null);
            method = cls.getMethod("allocateInstance", new Class[]{Class.class});
            return new C0588j() {
                public <T> T mo836a(Class<T> cls) {
                    return method.invoke(obj, new Object[]{cls});
                }
            };
        } catch (Exception e) {
            try {
                final Method declaredMethod = ObjectInputStream.class.getDeclaredMethod("newInstance", new Class[]{Class.class, Class.class});
                declaredMethod.setAccessible(true);
                return new C0588j() {
                    public <T> T mo836a(Class<T> cls) {
                        return declaredMethod.invoke(null, new Object[]{cls, Object.class});
                    }
                };
            } catch (Exception e2) {
                try {
                    Method declaredMethod2 = ObjectStreamClass.class.getDeclaredMethod("getConstructorId", new Class[]{Class.class});
                    declaredMethod2.setAccessible(true);
                    final int intValue = ((Integer) declaredMethod2.invoke(null, new Object[]{Object.class})).intValue();
                    method = ObjectStreamClass.class.getDeclaredMethod("newInstance", new Class[]{Class.class, Integer.TYPE});
                    method.setAccessible(true);
                    return new C0588j() {
                        public <T> T mo836a(Class<T> cls) {
                            return method.invoke(null, new Object[]{cls, Integer.valueOf(intValue)});
                        }
                    };
                } catch (Exception e3) {
                    return new C05924();
                }
            }
        }
    }

    public abstract <T> T mo836a(Class<T> cls);
}
