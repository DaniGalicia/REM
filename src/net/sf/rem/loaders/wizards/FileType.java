/*
 *   REM - A NetBeans Module for ZK
 *   Copyright (C) 2006, 2007  Minjie Zha, Frederic Jean
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License along
 *   with this program; if not, write to the Free Software Foundation, Inc.,
 *   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package net.sf.rem.loaders.wizards;

/**
 * This file is copied from module org/netbeans/web/core.
 * @author magic
 */
class FileType { 
    private String name, suffix; 

    private FileType(String name, String suffix) { 
	this.name = name; 
	this.suffix = suffix; 
    } 

    public String toString() { return name; } 

    public String getSuffix() { return suffix; }

    public static final FileType ZUL = 
	new FileType("zul", "zul"); 

    public static final FileType ZS = 
	new FileType("zs", "zs"); 

} 
