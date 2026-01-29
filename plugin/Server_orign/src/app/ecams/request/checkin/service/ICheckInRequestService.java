package app.ecams.request.checkin.service;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.request.service.IRequestService;

public interface ICheckInRequestService extends IRequestService {

	ReturnMsg transfile(EcamsMessage message);
	ReturnMsg splitFileSend(EcamsMessage message);
	ReturnMsg getDownFileList(EcamsMessage ecamsmsg);
}
