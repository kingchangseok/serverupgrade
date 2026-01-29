package app.ecams.history.service;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;

public interface IHistoryService {
	ReturnMsg getHistoryList_detail(EcamsMessage ecamsmsg);
}
