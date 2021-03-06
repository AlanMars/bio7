/*******************************************************************************
 * Copyright (c) 2007-2014 M. Austenfeld
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     M. Austenfeld
 *******************************************************************************/

package com.eco.bio7.rbridge.debug;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import com.eco.bio7.Bio7Plugin;
import com.eco.bio7.rcp.StartBio7Utils;

public class DebugInfoAction extends Action {

	public DebugInfoAction() {
		super("DebugInfo");

		setId("DebugInfo");
		setText("(?)");

		// ImageDescriptor desc = ImageDescriptor.createFromImage(new
		// Image(Display.getCurrent(),
		// getClass().getResourceAsStream("/pics/help.gif")));
		ImageDescriptor desc = Bio7Plugin.getImageDescriptor("/icons/rdebug/help_contents.png");
		this.setImageDescriptor(desc);
	}

	public void run() {
		StartBio7Utils utils = StartBio7Utils.getConsoleInstance();
		utils.cons.clearConsole();
		StringBuffer buff = new StringBuffer();
		buff.append("Debug key commands:\n");
		buff.append("\n");
		buff.append("n          -       next\n");
		buff.append("s          -       step into\n");
		buff.append("f          -       finish\n");
		buff.append("c or cont  -       continue\n");
		buff.append("Q          -       quit\n");
		buff.append("where      -       show stack\n");
		buff.append("help       -       show help\n");
		buff.append("<expr>     -       evaluate expression");
		utils.cons.print(buff.toString());
	}

}