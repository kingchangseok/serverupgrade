package app.ecams.request.checkincnl.service;

import java.util.HashMap;

import app.ecams.request.service.IRequestService;

public interface ICheckInCnlRequestService extends IRequestService {
	String selectCmr1010_acptno(HashMap param);
}
