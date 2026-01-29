package app.ecams.request.confirm.service;

import java.util.HashMap;
import java.util.List;

import app.core.proto.ProtoEcams.ReturnMsg;

public interface IConfirmService {
	public boolean request_confirm(String acptno, String syscd, String qrycd, String userid, List<HashMap> confData);
	public String confselect (HashMap params);
	public boolean cmr9900_str(HashMap param);
}
