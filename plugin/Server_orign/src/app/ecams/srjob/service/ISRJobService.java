package app.ecams.srjob.service;

import java.util.HashMap;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;

public interface ISRJobService {
	ReturnMsg getSRInfo(EcamsMessage ecamsmsg);
	ReturnMsg getSResource(EcamsMessage ecamsmsg);
	ReturnMsg getSRAcess(EcamsMessage ecamsmsg);
	ReturnMsg getSResource2(EcamsMessage ecamsmsg); // 20210106 SR사용안할때도 리소스목록 새로고침 가능하게..
}
