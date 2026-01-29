package app.ecams.resourcetype.service;



import java.util.HashMap;
import java.util.List;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.resourcetype.model.ResourceType;

public interface IResourceTypeService {
	ReturnMsg getRsrcInfo (EcamsMessage ecamsmsg) ;
	HashMap getRsrcInfo_detail(String syscd,String rsrccd);
}
