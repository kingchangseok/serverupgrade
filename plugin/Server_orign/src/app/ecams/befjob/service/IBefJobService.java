package app.ecams.befjob.service;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;

public interface IBefJobService {
	ReturnMsg getBefJobList_detail(EcamsMessage ecamsmsg);
}
