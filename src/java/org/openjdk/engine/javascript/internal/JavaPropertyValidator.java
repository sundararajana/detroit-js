/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.openjdk.engine.javascript.internal;

final class JavaPropertyValidator {
    private JavaPropertyValidator() {}

    static boolean isValid(String prop) {
        if (prop == null || prop.isEmpty()) {
            return false;
        }

        int openParen = prop.indexOf('(');
        if (openParen == -1) {
            // Not a method. Make sure that it is a valid Java identifier.
            return isValidIdentifier(prop);
        }

        int closeParen = prop.lastIndexOf(')');
        // Basic structure check
        if (closeParen == -1 || closeParen != prop.length() - 1) {
            return false;
        }

        // Validate method name. Empty method name okay. For eg.
        // we may get constructor property by just supplying signature.
        String methodName = prop.substring(0, openParen);
        if (!methodName.isEmpty() && !isValidIdentifier(methodName)) {
            return false;
        }

        // Validate Arguments
        String argsPart = prop.substring(openParen + 1, closeParen);
        if (argsPart.isEmpty()) {
            // No-arg method is valid
            return true;
        }

        String[] args = argsPart.split(",");
        for (String arg : args) {
            if (!isValidTypeName(arg.trim())) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidIdentifier(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        int cp = id.codePointAt(0);
        if (!Character.isJavaIdentifierStart(cp)) {
            return false;
        }
        for (int i = Character.charCount(cp);
                i < id.length();
                i += Character.charCount(cp)) {
            cp = id.codePointAt(i);
            if (!Character.isJavaIdentifierPart(cp)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidTypeName(String s) {
        if (s.isEmpty()) {
            return false;
        }

        // Handle arrays (e.g., int[][])
        while (s.endsWith("[]")) {
            s = s.substring(0, s.length() - 2).trim();
        }

        // Handle Qualified Names (e.g., java.lang.String)
        String[] parts = s.split("\\.", -1);
        for (String part : parts) {
            if (!isValidIdentifier(part)) {
                return false;
            }
        }
        return true;
    }
}
