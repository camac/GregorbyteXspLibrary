package com.gregorbyte.bundleinspector.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.gregorbyte.xsp.plugin.Activator;
import com.ibm.commons.util.StringUtil;

public class CopySampleXPageToClipboardAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	@Override
	public void run(IAction arg0) {

		Clipboard clipboard = new Clipboard(Display.getCurrent());

		String res = "resources/bundleinspector.xsp";
		String contents = readResource(res);

		if (StringUtil.isEmpty(contents)) {
			MessageDialog.openError(window.getShell(), "Error loading Sample Page",
					"The Sample page was not loaded correctly ...sorry!");
		} else {

			clipboard.setContents(new Object[] { contents }, new Transfer[] { TextTransfer.getInstance() });

			String title = "Sample XPage Copied";
			String message = "The Sample XPage has been copied to the clipboard. Create a new XPage, Go to the Source Tab and paste the clipboard contents over the top";

			MessageDialog.openInformation(window.getShell(), title, message);
		}

	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public void init(IWorkbenchWindow arg0) {
		this.window = arg0;

	}

	public static String readResource(String resourcePath) {

		StringBuilder result = new StringBuilder();

		try {

			URL url = Activator.getContext().getBundle().getResource(resourcePath);
			InputStream inputStream = url.openConnection().getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				result.append(inputLine);
				result.append(System.getProperty("line.separator"));
			}

			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result.toString();
	}

}
