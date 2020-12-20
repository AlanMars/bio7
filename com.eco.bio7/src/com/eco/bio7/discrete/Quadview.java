package com.eco.bio7.discrete;

import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JApplet;
import javax.swing.JRootPane;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import com.eco.bio7.Bio7Plugin;
import com.eco.bio7.batch.Bio7Dialog;
import com.eco.bio7.image.Util;
import com.eco.bio7.jobs.LoadData;
import com.eco.bio7.methods.CurrentStates;
import com.eco.bio7.rbridge.RServe;
import com.eco.bio7.rbridge.RServeUtil;
import com.eco.bio7.rbridge.RState;
import com.eco.bio7.swt.SwtAwt;
import com.eco.bio7.time.Time;

/*This Quad2d class is instantiated in the Spreadview class!*/
public class Quadview extends ViewPart {
	public static final String ID = "com.eco.bio7.quadgrid";

	private IPartListener2 partListener;

	private JApplet panel;

	private Composite top;

	private java.awt.Frame frame;

	private JRootPane root;

	protected String[] fileList;

	private static Quadview quadview;

	private Action transferCounts;

	private Action transferPattern;

	private Shell parentShell;

	public Quadview() {

		super();
		quadview = this;

	}

	public void createPartControl(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.eco.bio7.quadgrid");
		createActions();
		initializeToolBar();
		partListener=new IPartListener2() {

			public void partActivated(IWorkbenchPartReference partRef) {
				if (partRef instanceof Quadview) {
					setFocus();

				}
				if (partRef.getId().equals("com.eco.bio7.quadgrid")) {
					if (Util.getOS().equals("Mac")) {

						Display dis = Util.getDisplay();
						dis.asyncExec(new Runnable() {

							public void run() {

								top.setVisible(false);
								top.setVisible(true);
							}
						});

					}

				}

			}

			public void partBroughtToTop(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals("com.eco.bio7.quadgrid")) {
					if (Util.getOS().equals("Mac")) {
						Display dis = Util.getDisplay();
						dis.asyncExec(new Runnable() {

							public void run() {

								top.setVisible(false);
								top.setVisible(true);
							}
						});

					}

				}

			}

			public void partClosed(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals("com.eco.bio7.quadgrid")) {
					Quad2d.getQuad2dInstance().quadviewopenend = false;
				}

			}

			public void partDeactivated(IWorkbenchPartReference partRef) {

			}

			public void partHidden(IWorkbenchPartReference partRef) {

			}

			public void partInputChanged(IWorkbenchPartReference partRef) {

			}

			public void partOpened(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals("com.eco.bio7.quadgrid")) {
					Quad2d.getQuad2dInstance().quadviewopenend = true;
				}

			}

			public void partVisible(IWorkbenchPartReference partRef) {

			}
		};
		getViewSite().getPage().addPartListener(partListener);

		/*Workaround for Mac error!*/
		parentShell = new Shell(Util.getDisplay());
		this.top = new Composite(parentShell, SWT.EMBEDDED);
		top.setLayout(new FillLayout());
		DropTarget dt = new DropTarget(top, DND.DROP_DEFAULT | DND.DROP_MOVE);
		dt.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dt.addDropListener(new DropTargetAdapter() {
			//private LoadWorkspaceJob ab;

			public void drop(DropTargetEvent event) {

				FileTransfer ft = FileTransfer.getInstance();
				if (ft.isSupportedType(event.currentDataType)) {
					fileList = (String[]) event.data;
					if (fileList[0].endsWith("exml")) {
						/* The same call as in Spreadview! */
						LoadData.load(fileList[0].toString());

					} else {
						MessageBox messageBox = new MessageBox(new Shell(),

								SWT.ICON_WARNING);
						messageBox.setMessage("File is not an *.exml file!");
						messageBox.open();

					}

				}

			}
		});

		try {
			System.setProperty("sun.awt.noerasebackground", "true");
		} catch (NoSuchMethodError error) {
		}

		frame = SWT_AWT.new_Frame(top);
		SwtAwt.setSwtAwtFocus(frame, top);
		

		panel = new JApplet();

		frame.add(panel);
		root = new JRootPane();
		panel.add(root);
		java.awt.Container contentPane = root.getContentPane();

		Time.setPause(true);
		Quad2d quad = Quad2d.getQuad2dInstance();
		quad.setBackground(getSystemColour(parent));
		contentPane.add(Quad2d.getQuad2dInstance().jScrollPane);
		top.setParent(parent);
		/*HighDPI fix for Windows frame layout which reverts the DPIUtil settings of the default implementation of the SWT_AWT class!*/
		if (Util.getOS().equals("Windows")) {
			if (Util.getZoom() >= 175) {
				Composite parent2 = top.getParent();
				parent2.getDisplay().asyncExec(() -> {
					if (parent2.isDisposed())
						return;
					final Rectangle clientArea = parent2.getClientArea(); // To Pixels
					EventQueue.invokeLater(() -> {
						frame.setSize(clientArea.width, clientArea.height);
						frame.validate();
					});
				});
			}

		}
		/*Time.setPause(true);
		SwingFxSwtView view = new SwingFxSwtView();
		Quad2d quad = Quad2d.getQuad2dInstance();
		quad.setBackground(getSystemColour(parent));
		view.embedd(top, quad.jScrollPane);*/

	}

	public java.awt.Color getSystemColour(Composite parent) {
		Color col = null;
		org.eclipse.swt.graphics.Color colswt = parent.getBackground();
		int r = colswt.getRed();
		int g = colswt.getGreen();
		int b = colswt.getBlue();
		col = new Color(r, g, b);

		return col;
	}

	public void setFocus() {

	}

	public void setstatusline(String message) {
		IActionBars bars = getViewSite().getActionBars();
		//Image im;

		// im = new Image(Display.getCurrent(),
		// getClass().getResourceAsStream("/icons/views/2dperspview.png"));

		bars.getStatusLineManager().setMessage(message);

	}

	public void dispose() {
		if (partListener != null) {
			IWorkbenchPage page = getViewSite().getPage();
			page.removePartListener(partListener);
		}

		super.dispose();
	}

	public static Quadview getQuadview() {
		return quadview;
	}

	public Composite getTop() {
		return top;
	}

	public void setTop(Composite top) {
		this.top = top;
	}

	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();

		toolbarManager.add(transferPattern);
		toolbarManager.add(transferCounts);

	}

	private void createActions() {

		transferPattern = new Action("Transfer pattern to R") {
			public void run() {
				if (RServe.isAliveDialog()) {
					if (RState.isBusy() == false) {
						RState.setBusy(true);
						Job job = new Job("Transfer Pattern...") {
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								monitor.beginTask("Transfer ...", IProgressMonitor.UNKNOWN);

								RConnection c = RServe.getConnection();
								int h = Field.getHeight();
								int w = Field.getWidth();
								String name = "quadgrid";

								int[] pInt = new int[w * h];
								int y = 0;
								int x = 0;
								for (int z = 0; z < h * w; z++) {

									if (x > (w - 1)) {
										y++;
										x = 0;
									}
									pInt[z] = Field.getState(x, y);

									if (x < w) {
										x++;
									}
								}

								/* We transfer the values to R! */

								try {
									c.assign(name, pInt);
								} catch (REngineException e) {

									e.printStackTrace();
								}

								try {
									c.eval("try(" + name + "<-matrix(" + name + "," + w + "," + h + "))");

								} catch (RserveException e) {

									e.printStackTrace();
								}
								pInt = null;
								monitor.done();
								return Status.OK_STATUS;
							}

						};
						job.addJobChangeListener(new JobChangeAdapter() {
							public void done(IJobChangeEvent event) {
								if (event.getResult().isOK()) {

									RState.setBusy(false);
									RServeUtil.listRObjects();

								} else {

									RState.setBusy(false);
								}
							}
						});
						// job.setSystem(true);
						job.schedule();
						Bio7Dialog.message("Transferred data to R!");
					} else {
						Bio7Dialog.message("Rserve is busy!");
					}
				}

			}
		};
		transferPattern.setImageDescriptor(Bio7Plugin.getImageDescriptor("/icons/views/2dperspview.png"));
		transferCounts = new Action("Transfer population count to R") {
			public void run() {
				if (RServe.isAliveDialog()) {
					if (RState.isBusy() == false) {
						RState.setBusy(true);
						Job job = new Job("Transfer Population Count...") {
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								monitor.beginTask("Transfer ...", IProgressMonitor.UNKNOWN);

								RConnection c = RServe.getConnection();
								for (int i = 0; i < CurrentStates.getStateList().size(); i++) {

									CounterModel zahl = (CounterModel) Quad2d.getQuad2dInstance().zaehlerlist.get(i);

									int z[] = zahl.getCounterListasArray();

									try {
										c.assign("State_" + CurrentStates.getStateName(i), z);
									} catch (REngineException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								}

								monitor.done();
								return Status.OK_STATUS;
							}

						};
						job.addJobChangeListener(new JobChangeAdapter() {
							public void done(IJobChangeEvent event) {
								if (event.getResult().isOK()) {

									RState.setBusy(false);
									RServeUtil.listRObjects();

								} else {

									RState.setBusy(false);
								}
							}
						});
						// job.setSystem(true);
						job.schedule();
						Bio7Dialog.message("Transferred data to R!");
					} else {
						Bio7Dialog.message("Rserve is busy!");
					}
				}

			}
		};
		transferCounts.setImageDescriptor(com.eco.bio7.Bio7Plugin.getImageDescriptor("/icons/views/linechartview.png"));

	}

}
