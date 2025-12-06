/*
 *   Copyright (C) 2022 Ratul Hasan
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.ratul.topactivity.utils;

import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Can be used up to Java 8
 */
public class NullSafety {
    @NonNull
    public static <T> T requireNonNullElse(@Nullable T obj, @NonNull T defaultObj) {
        return (obj != null) ? obj : Objects.requireNonNull(defaultObj, "defaultObj");
    }

    /**
     * Returns {@code true} if the given {@link CharSequence} is {@code null} or
     * consists only of whitespace characters.
     */
    public static boolean isNullOrEmpty(@Nullable CharSequence charSequence) {
        return charSequence == null || isBlank(charSequence);
    }

    /**
     * Returns {@code true} if the given array is {@code null} or has no elements.
     */
    public static <T> boolean isNullOrEmpty(@Nullable T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns {@code true} if the given array is {@code null} or has no elements.
     */
    public static <T> boolean isNullOrEmpty(@Nullable int[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns {@code true} if the given array is {@code null} or has no elements.
     */
    public static <T> boolean isNullOrEmpty(@Nullable char[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns {@code true} if the given array is {@code null} or has no elements.
     */
    public static <T> boolean isNullOrEmpty(@Nullable byte[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns {@code true} if the given array is {@code null} or has no elements.
     */
    public static <T> boolean isNullOrEmpty(@Nullable short[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns {@code true} if the given array is {@code null} or has no elements.
     */
    public static <T> boolean isNullOrEmpty(@Nullable long[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns {@code true} if the given array is {@code null} or has no elements.
     */
    public static <T> boolean isNullOrEmpty(@Nullable double[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns {@code true} if the given array is {@code null} or has no elements.
     */
    public static <T> boolean isNullOrEmpty(@Nullable float[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns {@code true} if the given {@link Collection} is {@code null} or empty.
     */
    public static <T> boolean isNullOrEmpty(@Nullable Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Returns {@code true} if the given {@link SparseArray} is {@code null} or has no
     * elements.
     */
    public static <T> boolean isNullOrEmpty(@Nullable SparseArray<T> sparseArray) {
        return sparseArray == null || sparseArray.size() == 0;
    }

    /**
     * Returns {@code true} if the given {@link Map} is {@code null} or empty.
     */
    public static <K, V> boolean isNullOrEmpty(@Nullable Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Returns {@code true} if the given {@link Bundle} is {@code null} or empty.
     */
    public static boolean isNullOrEmpty(@Nullable Bundle bundle) {
        return bundle == null || bundle.isEmpty();
    }

    /**
     * Returns {@code true} if the given {@link PersistableBundle} is {@code null} or empty.
     */
    public static boolean isNullOrEmpty(@Nullable PersistableBundle bundle) {
        return bundle == null || bundle.isEmpty();
    }

    /**
     * Returns {@code true} if the given {@link Cursor} is {@code null}, closed, or has no
     * rows.
     */
    public static boolean isNullOrEmpty(@Nullable Cursor cursor) {
        return cursor == null || cursor.isClosed() || cursor.getCount() == 0;
    }

    private static boolean isBlank(@NonNull CharSequence charSequence) {
        int length = charSequence.length();
        int left = 0;

        while (left < length) {
            char ch = charSequence.charAt(left);
            if (ch != ' ' && ch != '\t' && !Character.isWhitespace(ch)) {
                return false;
            }
            left++;
        }
        return true;
    }
}
