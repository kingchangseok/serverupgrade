package com.azsoft.ecams.ui.show;

import java.util.ResourceBundle;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.ContentMergeViewer;
import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class FileMergeView extends ContentMergeViewer implements IMergeViewerContentProvider{

	public FileMergeView(int style, ResourceBundle bundle,
			CompareConfiguration cc) {
		super(style, bundle, cc);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void copy(boolean leftToRight) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void createControls(Composite composite) {
		// TODO Auto-generated method stub
		
	}

	@Override 
	protected byte[] getContents(boolean left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void handleResizeAncestor(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleResizeLeftRight(int x, int y, int leftWidth,
			int centerWidth, int rightWidth, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateContent(Object ancestor, Object left, Object right) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getAncestorContent(Object input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getAncestorImage(Object input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAncestorLabel(Object input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getLeftContent(Object input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getLeftImage(Object input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLeftLabel(Object input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getRightContent(Object input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getRightImage(Object input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRightLabel(Object input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLeftEditable(Object input) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRightEditable(Object input) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void saveLeftContent(Object input, byte[] bytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveRightContent(Object input, byte[] bytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean showAncestor(Object input) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
		
	}
	
}
