package app.ecams.lang.service;



import java.util.HashMap;
import java.util.List;

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.lang.model.Lang;

public interface ILangService {
	ReturnMsg getLang (EcamsMessage message) ;
	List<Lang> getLangInfo (HashMap params) ;
	String getRsrcCD(String syscd);
}
