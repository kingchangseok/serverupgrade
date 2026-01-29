package app.ecams.syncwith.service;


import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;

public interface ISyncWithService {
	ReturnMsg diffSvr(EcamsMessage ecamsmsg);
}
