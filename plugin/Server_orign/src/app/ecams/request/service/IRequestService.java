package app.ecams.request.service;

import java.util.HashMap;
import java.util.List;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileDataList;
import app.core.proto.ProtoEcams.ReturnMsg;

public interface IRequestService {
	public ReturnMsg request(EcamsMessage ecamsmsg);
	public ReturnMsg request_complete(EcamsMessage ecamsmsg);
	public ReturnMsg request_setcncl(EcamsMessage ecamsmsg);
	public ReturnMsg request_allcncl(EcamsMessage message);
}
