package com.azsoft.ecams.image;

import org.eclipse.jface.resource.ImageDescriptor;

public class EcamsImages {
	  /**
	   * Lock Image Descriptor
	   */ 
	
	  public EcamsImages(){
		  super();
	  }
	  
	  public static ImageDescriptor getImageDescriptor(String filename){
		  if (filename.equals("Lock")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"lock.gif");
		  }
		  
		  if (filename.equals("Modified")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"filemodified.gif");
		  }
		  
		  if (filename.equals("Authority")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"authority.gif");
		  }

		  if (filename.equals("AllDel")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"AllDel.gif");
		  }
		  
		  if (filename.equals("SelDel")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"SelDel.gif");
		  }
		  
		  if (filename.equals("Add")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"AddFile.gif");
		  }
		  
		  if (filename.equals("Refresh")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"Refresh.gif");
		  }
		  
		  if (filename.equals("ExpandAll")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"Expand.gif");
		  }

		  if (filename.equals("CollapseAll")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"Collapse.gif");
		  }

		  if (filename.equals("History")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"showhistory.gif");
		  }

		  if (filename.equals("ChkImg")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"chkimg.gif");
		  }
		  
		  if (filename.equals("Both")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"both.gif");
		  }

		  if (filename.equals("Incoming")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"incoming.gif");
		  }

		  if (filename.equals("Outgoing")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"outgoing.gif");
		  }

		  if (filename.equals("Conflicts")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"conflicts.gif");
		  }

		  if (filename.equals("In")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"in.gif");
		  }
		  
		  if (filename.equals("Inplus")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"inplus.gif");
		  }

		  if (filename.equals("Out")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"out.gif");
		  }

		  if (filename.equals("Outplus")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"outplus.gif");
		  }
		  
		  if (filename.equals("Outminus")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"outminus.gif");
		  }

		  if (filename.equals("Different")){
			  return ImageDescriptor.createFromFile(EcamsImages.class,"different.gif");
		  }

		  return null;
	  }
	  	  	  
}
