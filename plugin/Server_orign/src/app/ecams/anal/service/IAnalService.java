package app.ecams.anal.service;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;

public interface IAnalService {
	ReturnMsg getAnalList_detail(EcamsMessage ecamsmsg);
	ReturnMsg getMethod(EcamsMessage ecamsmsg);
}
