package net.optifine.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorClass;
import net.optifine.reflect.ReflectorField;

public class ReflectorRaw {
    public static Field getField(Class cls, Class fieldType) {
        try {
            Field[] afield = cls.getDeclaredFields();
            for (int i = 0; i < afield.length; ++i) {
                Field field = afield[i];
                if (field.getType() != fieldType) continue;
                field.setAccessible(true);
                return field;
            }
            return null;
        }
        catch (Exception var5) {
            return null;
        }
    }

    public static Field[] getFields(Class cls, Class fieldType) {
        try {
            Field[] afield = cls.getDeclaredFields();
            return ReflectorRaw.getFields(afield, fieldType);
        }
        catch (Exception var3) {
            return null;
        }
    }

    public static Field[] getFields(Field[] fields, Class fieldType) {
        try {
            ArrayList list = new ArrayList();
            for (int i = 0; i < fields.length; ++i) {
                Field field = fields[i];
                if (field.getType() != fieldType) continue;
                field.setAccessible(true);
                list.add((Object)field);
            }
            Field[] afield = (Field[])list.toArray((Object[])new Field[list.size()]);
            return afield;
        }
        catch (Exception var5) {
            return null;
        }
    }

    public static Field[] getFieldsAfter(Class cls, Field field, Class fieldType) {
        try {
            Object[] afield = cls.getDeclaredFields();
            List list = Arrays.asList((Object[])afield);
            int i = list.indexOf((Object)field);
            if (i < 0) {
                return new Field[0];
            }
            List list1 = list.subList(i + 1, list.size());
            Field[] afield1 = (Field[])list1.toArray((Object[])new Field[list1.size()]);
            return ReflectorRaw.getFields(afield1, fieldType);
        }
        catch (Exception var8) {
            return null;
        }
    }

    public static Field[] getFields(Object obj, Field[] fields, Class fieldType, Object value) {
        try {
            ArrayList list = new ArrayList();
            for (int i = 0; i < fields.length; ++i) {
                Field field = fields[i];
                if (field.getType() != fieldType) continue;
                boolean flag = Modifier.isStatic((int)field.getModifiers());
                if (obj == null && !flag || obj != null && flag) continue;
                field.setAccessible(true);
                Object object = field.get(obj);
                if (object == value) {
                    list.add((Object)field);
                    continue;
                }
                if (object == null || value == null || !object.equals(value)) continue;
                list.add((Object)field);
            }
            Field[] afield = (Field[])list.toArray((Object[])new Field[list.size()]);
            return afield;
        }
        catch (Exception var9) {
            return null;
        }
    }

    public static Field getField(Class cls, Class fieldType, int index) {
        Field[] afield = ReflectorRaw.getFields(cls, fieldType);
        return index >= 0 && index < afield.length ? afield[index] : null;
    }

    public static Field getFieldAfter(Class cls, Field field, Class fieldType, int index) {
        Field[] afield = ReflectorRaw.getFieldsAfter(cls, field, fieldType);
        return index >= 0 && index < afield.length ? afield[index] : null;
    }

    public static Object getFieldValue(Object obj, Class cls, Class fieldType) {
        ReflectorField reflectorfield = ReflectorRaw.getReflectorField(cls, fieldType);
        return reflectorfield == null ? null : (!reflectorfield.exists() ? null : Reflector.getFieldValue((Object)obj, (ReflectorField)reflectorfield));
    }

    public static Object getFieldValue(Object obj, Class cls, Class fieldType, int index) {
        ReflectorField reflectorfield = ReflectorRaw.getReflectorField(cls, fieldType, index);
        return reflectorfield == null ? null : (!reflectorfield.exists() ? null : Reflector.getFieldValue((Object)obj, (ReflectorField)reflectorfield));
    }

    public static boolean setFieldValue(Object obj, Class cls, Class fieldType, Object value) {
        ReflectorField reflectorfield = ReflectorRaw.getReflectorField(cls, fieldType);
        return reflectorfield == null ? false : (!reflectorfield.exists() ? false : Reflector.setFieldValue((Object)obj, (ReflectorField)reflectorfield, (Object)value));
    }

    public static boolean setFieldValue(Object obj, Class cls, Class fieldType, int index, Object value) {
        ReflectorField reflectorfield = ReflectorRaw.getReflectorField(cls, fieldType, index);
        return reflectorfield == null ? false : (!reflectorfield.exists() ? false : Reflector.setFieldValue((Object)obj, (ReflectorField)reflectorfield, (Object)value));
    }

    public static ReflectorField getReflectorField(Class cls, Class fieldType) {
        Field field = ReflectorRaw.getField(cls, fieldType);
        if (field == null) {
            return null;
        }
        ReflectorClass reflectorclass = new ReflectorClass(cls);
        return new ReflectorField(reflectorclass, field.getName());
    }

    public static ReflectorField getReflectorField(Class cls, Class fieldType, int index) {
        Field field = ReflectorRaw.getField(cls, fieldType, index);
        if (field == null) {
            return null;
        }
        ReflectorClass reflectorclass = new ReflectorClass(cls);
        return new ReflectorField(reflectorclass, field.getName());
    }
}
