/*
 * The MIT License
 * 
 * Copyright (c) 2012, Cisco Systems, Inc., Max Spring
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cisco.surf.jenkow.ide.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.UserLibrary;
import org.eclipse.jdt.internal.core.UserLibraryManager;

public class UserLibConfigurator {

	public void configure(final String name, final File jarFileOrDir) throws CoreException {
		
		ClasspathContainerInitializer initializer = JavaCore.getClasspathContainerInitializer(JavaCore.USER_LIBRARY_CONTAINER_ID);
		IPath containerPath = new Path(JavaCore.USER_LIBRARY_CONTAINER_ID);
		initializer.requestClasspathContainerUpdate(
				containerPath.append(name), null,
				getClasspathContainer(name, jarFileOrDir));
	}
			
	private IClasspathContainer getClasspathContainer(final String name, final File jarFileOrDir) throws JavaModelException {
		List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		if(isDefined(name))
		{
			UserLibrary userLibrary = new UserLibraryManager().getUserLibrary(name);
			Collections.addAll(list, userLibrary.getEntries());
		}
		return createClasspathContainer(name, getClasspathEntries(jarFileOrDir,list));
	}
	
	private IClasspathContainer createClasspathContainer(final String name, final IClasspathEntry[] entries)
	{
		return new IClasspathContainer() {
			public IPath getPath() 
			{
				return new Path(JavaCore.USER_LIBRARY_CONTAINER_ID).append(name);
			}

			public int getKind() 
			{
				return K_APPLICATION;
			}

			public String getDescription() 
			{
				return name;
			}

			public IClasspathEntry[] getClasspathEntries() 
			{					
				return entries;
			}
		};
	}
	
	private IClasspathEntry[] getClasspathEntries(File jarFileOrDir, List<IClasspathEntry> entryList)
	{
		if(jarFileOrDir.isDirectory())
		{
			for (File entry : jarFileOrDir.listFiles()) 
			{
				if(entry.isFile())
				{
					addEntryIfNeeded(entryList, entry);
				}
			}
		}
		else if(jarFileOrDir.exists())
		{
			addEntryIfNeeded(entryList, jarFileOrDir);
		}
		return entryList.toArray(new IClasspathEntry[entryList.size()]);
	}

	private void addEntryIfNeeded(List<IClasspathEntry> entryList, File entry) {
		IPath path = new Path(entry.getAbsolutePath());
		if(!hasEntry(entryList, path))
		{
			entryList.add(JavaCore.newLibraryEntry(path, null, null) );
		}
	}

	private boolean hasEntry(List<IClasspathEntry> list, IPath path) {
		for (IClasspathEntry iClasspathEntry : list) {
			if(iClasspathEntry.getPath().equals(path)) return true;
		}
		return false;
	}

	public boolean isDefined(String userLib)
	{
		for (String name : JavaCore.getUserLibraryNames()) 
		{
			if(name.equals(userLib)) return true;
		}
		return false;
	}
}
