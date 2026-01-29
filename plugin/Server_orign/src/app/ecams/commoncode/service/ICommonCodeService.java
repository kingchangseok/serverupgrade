package app.ecams.commoncode.service;

import java.util.List;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.ecams.commoncode.model.CommonCode;
import app.core.proto.ProtoEcams.ReturnMsg;

public interface ICommonCodeService {
	public ReturnMsg getCodes(EcamsMessage ecamsmsg);
	public CommonCode getCode(String macode, String micode);
}
